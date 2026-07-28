package br.com.sisumoni.backend.service;

import br.com.sisumoni.backend.domain.Classificacao;
import br.com.sisumoni.backend.domain.Estudante;
import br.com.sisumoni.backend.domain.ResolucaoEmpate;
import br.com.sisumoni.backend.domain.Usuario;
import br.com.sisumoni.backend.domain.Vaga;
import br.com.sisumoni.backend.dto.ClassificacaoResponse;
import br.com.sisumoni.backend.exception.RecursoNaoEncontradoException;
import br.com.sisumoni.backend.exception.RegraDeNegocioException;
import br.com.sisumoni.backend.repository.ClassificacaoRepository;
import br.com.sisumoni.backend.repository.EstudanteRepository;
import br.com.sisumoni.backend.repository.ResolucaoEmpateRepository;
import br.com.sisumoni.backend.repository.VagaRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ClassificacaoService {

    private final ClassificacaoRepository classificacaoRepository;
    private final EstudanteRepository estudanteRepository;
    private final VagaRepository vagaRepository;
    private final ResolucaoEmpateRepository resolucaoRepository;

    public ClassificacaoService(ClassificacaoRepository classificacaoRepository,
                                EstudanteRepository estudanteRepository,
                                VagaRepository vagaRepository,
                                ResolucaoEmpateRepository resolucaoRepository) {
        this.classificacaoRepository = classificacaoRepository;
        this.estudanteRepository = estudanteRepository;
        this.vagaRepository = vagaRepository;
        this.resolucaoRepository = resolucaoRepository;
    }

    // Qual "balde" de capacidade de uma vaga um tier está preenchendo
    private enum TipoSlot { PASSANDO, ESPERA }

    // ══════════════════════════════════════════════════════════════
    //  RECÁLCULO GLOBAL
    // ══════════════════════════════════════════════════════════════
    /**
     * Recalcula a classificação de TODAS as vagas de uma vez.
     *
     * Regra de exclusividade: um estudante só pode estar classificado em
     * UMA vaga no sistema inteiro, decidida por esta escada de prioridade:
     *   1) Bolsista/Voluntário na 1ª opção
     *   2) Bolsista/Voluntário na 2ª opção
     *   3) Lista de Espera na 1ª opção
     *   4) Lista de Espera na 2ª opção
     * Assim que o estudante se classifica em qualquer nível, ele sai da
     * disputa em tudo mais (inclusive não ocupa lista de espera de duas
     * vagas ao mesmo tempo). Por isso o cálculo não pode mais ser feito
     * vaga por vaga isoladamente — o resultado de uma vaga pode liberar ou
     * ocupar vagas de outra.
     */
    @Transactional
    public void recalcularTudo() {
        List<Vaga> vagas = vagaRepository.findAll();
        List<Estudante> estudantes = estudanteRepository.findAllComRelacionamentos();

        Map<UUID, List<ResolucaoEmpate>> resolucoesPorVaga = resolucaoRepository.findAllComEstudantes().stream()
                .collect(Collectors.groupingBy(r -> r.getVaga().getId()));

        List<Candidatura> candidaturas1 = new ArrayList<>();
        List<Candidatura> candidaturas2 = new ArrayList<>();
        for (Estudante e : estudantes) {
            if (e.getOpcao1() != null && e.getMediaOpcao1() != null) {
                candidaturas1.add(new Candidatura(e, e.getOpcao1(), e.getIra().add(e.getMediaOpcao1())));
            }
            if (e.getOpcao2() != null && e.getMediaOpcao2() != null) {
                candidaturas2.add(new Candidatura(e, e.getOpcao2(), e.getIra().add(e.getMediaOpcao2())));
            }
        }

        Set<UUID> consumidos = new HashSet<>();
        Map<UUID, List<Candidato>> resultados = new HashMap<>();
        for (Vaga v : vagas) resultados.put(v.getId(), new ArrayList<>());

        // 1º) Bolsista/Voluntário na 1ª opção
        processarTier(candidaturas1, TipoSlot.PASSANDO, consumidos, resultados, resolucoesPorVaga);
        // 2º) Bolsista/Voluntário na 2ª opção (só quem sobrou do nível 1)
        processarTier(candidaturas2, TipoSlot.PASSANDO, consumidos, resultados, resolucoesPorVaga);
        // 3º) Lista de espera na 1ª opção (só quem sobrou dos níveis 1-2)
        processarTier(candidaturas1, TipoSlot.ESPERA, consumidos, resultados, resolucoesPorVaga);
        // 4º) Lista de espera na 2ª opção (só quem sobrou dos níveis 1-3)
        processarTier(candidaturas2, TipoSlot.ESPERA, consumidos, resultados, resolucoesPorVaga);

        limparResolucoesObsoletas(resultados, resolucoesPorVaga);

        for (Vaga v : vagas) {
            sincronizar(v, resultados.get(v.getId()));
        }
    }

    /**
     * Processa um nível da escada de prioridade: para cada vaga, ordena os
     * candidatos deste tier que ainda não foram consumidos por um nível
     * anterior e preenche a capacidade que ainda sobrar dessa vaga neste
     * "balde" (PASSANDO ou ESPERA).
     */
    private void processarTier(List<Candidatura> candidaturas, TipoSlot tipoSlot,
                                Set<UUID> consumidos,
                                Map<UUID, List<Candidato>> resultados,
                                Map<UUID, List<ResolucaoEmpate>> resolucoesPorVaga) {

        Map<UUID, List<Candidatura>> porVaga = candidaturas.stream()
                .filter(c -> !consumidos.contains(c.estudante.getId()))
                .collect(Collectors.groupingBy(c -> c.vaga.getId()));

        for (Map.Entry<UUID, List<Candidatura>> entry : porVaga.entrySet()) {
            UUID vagaId = entry.getKey();
            Vaga vaga = entry.getValue().get(0).vaga;
            List<ResolucaoEmpate> resolucoes = resolucoesPorVaga.getOrDefault(vagaId, List.of());
            List<Candidato> jaAlocados = resultados.get(vagaId);
            Map<String, Boolean> resolvidos = mapaResolvidos(resolucoes);

            List<Candidato> ordenados = entry.getValue().stream()
                    .map(c -> new Candidato(c.estudante, c.pontuacao))
                    .sorted(construirComparador(resolucoes))
                    .collect(Collectors.toList());

            int capacidadeTotal = (tipoSlot == TipoSlot.PASSANDO)
                    ? vaga.getQtdBolsistas() + vaga.getQtdVoluntarios()
                    : vaga.getQtdListaEspera();
            long jaOcupados = jaAlocados.stream().filter(c -> c.tipoSlot == tipoSlot).count();
            int capacidade = (int) (capacidadeTotal - jaOcupados);
            if (capacidade <= 0) continue;

            int limite = Math.min(capacidade, ordenados.size());

            // Empate na fronteira externa (entra neste tier vs. fica de fora): se
            // empatado e ainda sem resolução, os dois entram provisoriamente —
            // o operador decide depois, e a decisão dispara um novo recálculo.
            boolean empateNaFronteira = limite < ordenados.size()
                    && ordenados.get(limite - 1).pontuacao.compareTo(ordenados.get(limite).pontuacao) == 0
                    && !resolvidos.containsKey(chavePar(
                            ordenados.get(limite - 1).estudante.getId(),
                            ordenados.get(limite).estudante.getId()));
            if (empateNaFronteira) {
                ordenados.get(limite - 1).empate = true;
                ordenados.get(limite).empate = true;
            }
            int limiteEfetivo = empateNaFronteira ? limite + 1 : limite;

            long bolsistasJaAlocados = jaAlocados.stream()
                    .filter(c -> c.tipo == Classificacao.Tipo.BOLSISTA).count();
            int offsetPosicao = jaAlocados.size();
            int qtdBolsistasVaga = vaga.getQtdBolsistas();

            for (int i = 0; i < limiteEfetivo; i++) {
                Candidato c = ordenados.get(i);
                c.tipoSlot = tipoSlot;
                c.posicao = offsetPosicao + i + 1;
                if (tipoSlot == TipoSlot.ESPERA) {
                    c.tipo = Classificacao.Tipo.LISTA_ESPERA;
                } else {
                    long indiceGlobalPassando = bolsistasJaAlocados + i;
                    c.tipo = (indiceGlobalPassando < qtdBolsistasVaga)
                            ? Classificacao.Tipo.BOLSISTA
                            : Classificacao.Tipo.VOLUNTARIO;
                }
                jaAlocados.add(c);
                consumidos.add(c.estudante.getId());
            }

            // Empate interno bolsista/voluntário (só faz sentido dentro de PASSANDO)
            if (tipoSlot == TipoSlot.PASSANDO) {
                int fronteiraBolsista = (int) (qtdBolsistasVaga - bolsistasJaAlocados);
                if (fronteiraBolsista > 0 && fronteiraBolsista < limiteEfetivo) {
                    Candidato dentro = ordenados.get(fronteiraBolsista - 1);
                    Candidato fora = ordenados.get(fronteiraBolsista);
                    if (dentro.pontuacao.compareTo(fora.pontuacao) == 0
                            && !resolvidos.containsKey(chavePar(dentro.estudante.getId(), fora.estudante.getId()))) {
                        dentro.empate = true;
                        fora.empate = true;
                    }
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  RESOLUÇÃO DE EMPATE (chamado pelo operador)
    // ══════════════════════════════════════════════════════════════
    @Transactional
    public void resolverEmpate(UUID vagaId, UUID vencedorId, UUID perdedorId) {
        if (vencedorId.equals(perdedorId)) {
            throw new RegraDeNegocioException("Vencedor e perdedor não podem ser o mesmo estudante");
        }

        Vaga vaga = vagaRepository.findById(vagaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Vaga não encontrada"));
        Estudante vencedor = estudanteRepository.findById(vencedorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Estudante vencedor não encontrado"));
        Estudante perdedor = estudanteRepository.findById(perdedorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Estudante perdedor não encontrado"));

        // Recuperar as pontuações dos dois nesta vaga
        BigDecimal pVencedor = pontuacaoNaVaga(vencedor, vagaId);
        BigDecimal pPerdedor = pontuacaoNaVaga(perdedor, vagaId);
        if (pVencedor == null || pPerdedor == null) {
            throw new RegraDeNegocioException("Um dos estudantes não concorre a esta vaga");
        }

        // Só faz sentido resolver se realmente empatam
        if (pVencedor.compareTo(pPerdedor) != 0) {
            throw new RegraDeNegocioException("Estes estudantes não estão empatados nesta vaga");
        }

        // Remove qualquer resolução anterior deste par (nas duas ordens) para regravar
        List<ResolucaoEmpate> doVaga = resolucaoRepository.findByVagaId(vagaId);
        for (ResolucaoEmpate r : doVaga) {
            boolean mesmoPar =
                    (r.getVencedor().getId().equals(vencedorId) && r.getPerdedor().getId().equals(perdedorId))
                 || (r.getVencedor().getId().equals(perdedorId) && r.getPerdedor().getId().equals(vencedorId));
            if (mesmoPar) {
                resolucaoRepository.delete(r);
            }
        }

        ResolucaoEmpate resolucao = new ResolucaoEmpate();
        resolucao.setVaga(vaga);
        resolucao.setVencedor(vencedor);
        resolucao.setPerdedor(perdedor);
        resolucao.setPontuacaoEmpate(pVencedor);
        resolucao.setResolvidoPor(usuarioLogado());
        resolucaoRepository.save(resolucao);

        // Recalcular tudo já aplicando a decisão (a decisão pode liberar ou
        // ocupar vagas em cascata, não só a vaga onde o empate ocorreu)
        recalcularTudo();
    }

    // ══════════════════════════════════════════════════════════════
    //  CONSULTA
    // ══════════════════════════════════════════════════════════════
    @Transactional(readOnly = true)
    public List<ClassificacaoResponse> listarPorVaga(UUID vagaId) {
        Usuario logado = usuarioLogado();
        boolean ehAdmin = logado.getPerfil() == Usuario.Perfil.ADMIN;

        return classificacaoRepository.findByVagaIdComEstudante(vagaId).stream()
                .map(c -> new ClassificacaoResponse(
                        c.getEstudante().getId(),
                        c.getEstudante().getNomeFantasia(),
                        ehAdmin ? c.getEstudante().getNome() : null,
                        c.getPosicao(),
                        c.getPontuacao(),
                        c.getTipo() != null ? c.getTipo().name() : null,
                        c.isEmpate()
                ))
                .collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════

    private BigDecimal pontuacaoNaVaga(Estudante e, UUID vagaId) {
        if (e.getOpcao1() != null && e.getOpcao1().getId().equals(vagaId) && e.getMediaOpcao1() != null) {
            return e.getIra().add(e.getMediaOpcao1());
        }
        if (e.getOpcao2() != null && e.getOpcao2().getId().equals(vagaId) && e.getMediaOpcao2() != null) {
            return e.getIra().add(e.getMediaOpcao2());
        }
        return null;
    }

    /**
     * Comparador: 1º pontuação decrescente; 2º, em caso de empate,
     * respeita a decisão do operador (vencedor vem antes do perdedor).
     */
    private Comparator<Candidato> construirComparador(List<ResolucaoEmpate> resolucoes) {
        Map<String, UUID> vencedores = new HashMap<>();
        for (ResolucaoEmpate r : resolucoes) {
            vencedores.put(chavePar(r.getVencedor().getId(), r.getPerdedor().getId()),
                    r.getVencedor().getId());
        }

        return (a, b) -> {
            int porPontuacao = b.pontuacao.compareTo(a.pontuacao); // desc
            if (porPontuacao != 0) return porPontuacao;

            UUID vencedor = vencedores.get(chavePar(a.estudante.getId(), b.estudante.getId()));
            if (vencedor != null) {
                if (vencedor.equals(a.estudante.getId())) return -1;
                if (vencedor.equals(b.estudante.getId())) return 1;
            }
            return a.estudante.getId().compareTo(b.estudante.getId());
        };
    }

    private Map<String, Boolean> mapaResolvidos(List<ResolucaoEmpate> resolucoes) {
        Map<String, Boolean> resolvidos = new HashMap<>();
        for (ResolucaoEmpate r : resolucoes) {
            resolvidos.put(chavePar(r.getVencedor().getId(), r.getPerdedor().getId()), true);
        }
        return resolvidos;
    }

    /**
     * Regra de reabertura: descarta resoluções cujo par não está mais
     * empatado e adjacente no resultado final de nenhuma vaga (limitação
     * conhecida: se o mesmo par voltar a empatar num nível diferente da
     * mesma vaga — ex.: empataram no corte de bolsista/voluntário, e mais
     * tarde voltam a empatar no corte da lista de espera — a resolução
     * antiga pode se aplicar de novo automaticamente em vez de gerar um
     * novo alerta; a ordem decidida entre os dois continua fazendo
     * sentido, só o operador não é perguntado de novo nesse caso raro).
     */
    private void limparResolucoesObsoletas(Map<UUID, List<Candidato>> resultados,
                                           Map<UUID, List<ResolucaoEmpate>> resolucoesPorVaga) {
        for (Map.Entry<UUID, List<ResolucaoEmpate>> entry : resolucoesPorVaga.entrySet()) {
            List<Candidato> finalDaVaga = resultados.getOrDefault(entry.getKey(), List.of());
            Map<UUID, Candidato> porEstudante = new HashMap<>();
            for (Candidato c : finalDaVaga) porEstudante.put(c.estudante.getId(), c);

            for (ResolucaoEmpate r : entry.getValue()) {
                Candidato v = porEstudante.get(r.getVencedor().getId());
                Candidato p = porEstudante.get(r.getPerdedor().getId());
                boolean aindaValida = v != null && p != null
                        && v.pontuacao.compareTo(p.pontuacao) == 0
                        && Math.abs(v.posicao - p.posicao) == 1;
                if (!aindaValida) {
                    resolucaoRepository.delete(r);
                }
            }
        }
    }

    private void sincronizar(Vaga vaga, List<Candidato> candidatos) {
        List<Classificacao> existentes = classificacaoRepository.findByVagaId(vaga.getId());
        Map<UUID, Classificacao> mapa = new HashMap<>();
        for (Classificacao c : existentes) mapa.put(c.getEstudante().getId(), c);

        List<Classificacao> paraSalvar = new ArrayList<>();
        List<UUID> idsAtuais = new ArrayList<>();

        for (Candidato cand : candidatos) {
            idsAtuais.add(cand.estudante.getId());
            Classificacao c = mapa.get(cand.estudante.getId());
            if (c == null) {
                c = new Classificacao();
                c.setEstudante(cand.estudante);
                c.setVaga(vaga);
            }
            c.setPontuacao(cand.pontuacao);
            c.setPosicao(cand.posicao);
            c.setTipo(cand.tipo);
            c.setEmpate(cand.empate);
            paraSalvar.add(c);
        }

        for (Classificacao c : existentes) {
            if (!idsAtuais.contains(c.getEstudante().getId())) {
                classificacaoRepository.delete(c);
            }
        }
        classificacaoRepository.saveAll(paraSalvar);
    }

    private Usuario usuarioLogado() {
        return (Usuario) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    // Chave canônica de um par (ordem não importa)
    private String chavePar(UUID a, UUID b) {
        return (a.compareTo(b) < 0) ? a + "|" + b : b + "|" + a;
    }

    // Candidatura bruta de um estudante a uma vaga (via 1ª ou 2ª opção),
    // antes de saber se ele vai ser consumido por algum tier
    private static class Candidatura {
        Estudante estudante;
        Vaga vaga;
        BigDecimal pontuacao;

        Candidatura(Estudante estudante, Vaga vaga, BigDecimal pontuacao) {
            this.estudante = estudante;
            this.vaga = vaga;
            this.pontuacao = pontuacao;
        }
    }

    // Resultado de um candidato dentro de uma vaga específica, já com o
    // tier (tipoSlot) e o tipo final atribuídos
    private static class Candidato {
        Estudante estudante;
        BigDecimal pontuacao;
        Integer posicao;
        TipoSlot tipoSlot;
        Classificacao.Tipo tipo;
        boolean empate = false;

        Candidato(Estudante estudante, BigDecimal pontuacao) {
            this.estudante = estudante;
            this.pontuacao = pontuacao;
        }
    }
}
