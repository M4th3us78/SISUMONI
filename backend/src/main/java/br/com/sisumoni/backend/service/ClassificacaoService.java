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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ClassificacaoService {

    private static final Logger log = LoggerFactory.getLogger(ClassificacaoService.class);

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

    // ══════════════════════════════════════════════════════════════
    //  RECÁLCULO GLOBAL
    // ══════════════════════════════════════════════════════════════
    /**
     * Recalcula a classificação de TODAS as vagas de uma vez, via aceitação
     * diferida (Gale-Shapley, lado do estudante propondo).
     *
     * Dentro de uma vaga, a disputa pelas posições é sempre só por
     * pontuação — não importa se o candidato a colocou como 1ª ou 2ª opção.
     * A ordem de opção só decide, quando um estudante teria nota para
     * passar em mais de uma vaga ao mesmo tempo, em qual delas ele fica
     * (a de 1ª opção, liberando a outra pro próximo colocado por nota).
     *
     * Como a prioridade Passando > Lista de Espera é igual para todo mundo,
     * o cálculo se decompõe em duas fases sequenciais e independentes:
     *   Fase 1 — todos disputam vagas de Passando (bolsista+voluntário).
     *   Fase 2 — só quem não passou em nenhuma vaga na Fase 1 disputa
     *            Lista de Espera, do mesmo jeito.
     * Cada fase roda o mesmo algoritmo genérico ({@link #aceitacaoDiferida}):
     * cada estudante propõe, em ordem, à vaga da 1ª opção e depois da 2ª; a
     * vaga sempre reordena por pontuação e devolve à fila quem não couber
     * mais na capacidade — inclusive alguém que já estava "dentro", se um
     * proponente melhor aparecer depois. Isso corrige o caso em que um
     * candidato de 1ª opção com nota menor travava a vaga antes mesmo de um
     * candidato de 2ª opção com nota maior ser considerado.
     */
    @Transactional
    public void recalcularTudo() {
        List<Vaga> vagas = vagaRepository.findAll();
        List<Estudante> estudantes = estudanteRepository.findAllComRelacionamentos();

        Map<UUID, List<ResolucaoEmpate>> resolucoesPorVaga = resolucaoRepository.findAllComEstudantes().stream()
                .collect(Collectors.groupingBy(r -> r.getVaga().getId()));

        Map<UUID, List<Opcao>> opcoesPorEstudante = new HashMap<>();
        List<Estudante> candidatos = new ArrayList<>();
        for (Estudante e : estudantes) {
            List<Opcao> opcoes = opcoesDoEstudante(e);
            if (!opcoes.isEmpty()) {
                opcoesPorEstudante.put(e.getId(), opcoes);
                candidatos.add(e);
            }
        }

        // Fase 1 — Passando (bolsista + voluntário)
        Map<UUID, Integer> capacidadePassando = vagas.stream()
                .collect(Collectors.toMap(Vaga::getId, v -> v.getQtdBolsistas() + v.getQtdVoluntarios()));
        Map<UUID, List<Candidato>> passandoPorVaga =
                aceitacaoDiferida(candidatos, opcoesPorEstudante, capacidadePassando, resolucoesPorVaga);

        // Fase 2 — Lista de espera (só quem não passou em nenhuma vaga na fase 1)
        Set<UUID> seatedNaFase1 = passandoPorVaga.values().stream()
                .flatMap(List::stream)
                .map(c -> c.estudante.getId())
                .collect(Collectors.toSet());
        List<Estudante> candidatosEspera = candidatos.stream()
                .filter(e -> !seatedNaFase1.contains(e.getId()))
                .toList();

        Map<UUID, Integer> capacidadeEspera = vagas.stream()
                .collect(Collectors.toMap(Vaga::getId, Vaga::getQtdListaEspera));
        Map<UUID, List<Candidato>> esperaPorVaga =
                aceitacaoDiferida(candidatosEspera, opcoesPorEstudante, capacidadeEspera, resolucoesPorVaga);

        Map<UUID, List<Candidato>> resultados = montarResultados(vagas, passandoPorVaga, esperaPorVaga, resolucoesPorVaga);

        limparResolucoesObsoletas(resultados, resolucoesPorVaga);

        for (Vaga v : vagas) {
            sincronizar(v, resultados.get(v.getId()));
        }
    }

    /**
     * Algoritmo genérico de aceitação diferida para uma fase (Passando ou
     * Espera): cada estudante em {@code candidatos} propõe, em ordem, às
     * vagas em {@code opcoesPorEstudante} (1ª opção primeiro, depois a 2ª).
     * Cada vaga aceita provisoriamente até {@code capacidadePorVaga}
     * candidatos, sempre ordenados por pontuação — um proponente melhor
     * pode desalojar quem já estava dentro, que volta pra fila e tenta a
     * própria próxima opção. Termina quando ninguém mais tem proposta a
     * fazer (cada estudante propõe no máximo 2 vezes por fase).
     */
    private Map<UUID, List<Candidato>> aceitacaoDiferida(
            List<Estudante> candidatos,
            Map<UUID, List<Opcao>> opcoesPorEstudante,
            Map<UUID, Integer> capacidadePorVaga,
            Map<UUID, List<ResolucaoEmpate>> resolucoesPorVaga) {

        Map<UUID, List<Candidato>> holds = new HashMap<>();
        Map<UUID, Integer> proximoIndice = new HashMap<>();
        Deque<Estudante> livres = new ArrayDeque<>(candidatos);

        while (!livres.isEmpty()) {
            Estudante e = livres.poll();
            List<Opcao> opcoes = opcoesPorEstudante.get(e.getId());
            int idx = proximoIndice.getOrDefault(e.getId(), 0);
            if (idx >= opcoes.size()) {
                continue; // esgotou as opções nesta fase — fica sem posição
            }
            Opcao opcao = opcoes.get(idx);
            proximoIndice.put(e.getId(), idx + 1);

            Integer capacidade = capacidadePorVaga.get(opcao.vagaId());
            if (capacidade == null || capacidade <= 0) {
                continue; // vaga sem capacidade nesta fase
            }

            List<Candidato> lista = holds.computeIfAbsent(opcao.vagaId(), k -> new ArrayList<>());
            lista.add(new Candidato(e, opcao.pontuacao()));

            if (lista.size() <= capacidade) {
                continue;
            }

            List<ResolucaoEmpate> resolucoes = resolucoesPorVaga.getOrDefault(opcao.vagaId(), List.of());
            lista.sort(construirComparador(resolucoes));

            int corte = capacidade;
            Map<String, Boolean> resolvidos = mapaResolvidos(resolucoes);

            // Empate na fronteira: se o último aceito e o primeiro rejeitado
            // empatam e não há resolução gravada, os dois entram
            // provisoriamente — um operador decide depois.
            if (corte > 0 && corte < lista.size()
                    && lista.get(corte - 1).pontuacao.compareTo(lista.get(corte).pontuacao) == 0
                    && !resolvidos.containsKey(chavePar(
                            lista.get(corte - 1).estudante.getId(), lista.get(corte).estudante.getId()))) {
                corte++;
            }

            if (corte < lista.size()
                    && lista.get(corte - 1).pontuacao.compareTo(lista.get(corte).pontuacao) == 0) {
                log.warn("Empate de 3 ou mais estudantes no limite da vaga {} — só o par mais "
                        + "próximo do corte fica provisório, os demais são desclassificados "
                        + "automaticamente (empate múltiplo não é resolvido pela interface).",
                        opcao.vagaId());
            }

            while (lista.size() > corte) {
                Candidato excedente = lista.remove(lista.size() - 1);
                livres.add(excedente.estudante);
            }
        }
        return holds;
    }

    /**
     * Junta o resultado final (já convergido) das duas fases por vaga,
     * atribuindo posição sequencial (Passando primeiro, depois Espera),
     * tipo (Bolsista/Voluntário/Lista de Espera) e as flags de empate —
     * tanto a de fronteira externa (entrar ou não na fase) quanto a interna
     * (fronteira bolsista/voluntário dentro do Passando).
     */
    private Map<UUID, List<Candidato>> montarResultados(
            List<Vaga> vagas,
            Map<UUID, List<Candidato>> passandoPorVaga,
            Map<UUID, List<Candidato>> esperaPorVaga,
            Map<UUID, List<ResolucaoEmpate>> resolucoesPorVaga) {

        Map<UUID, List<Candidato>> resultados = new HashMap<>();

        for (Vaga vaga : vagas) {
            List<ResolucaoEmpate> resolucoes = resolucoesPorVaga.getOrDefault(vaga.getId(), List.of());
            Comparator<Candidato> comparador = construirComparador(resolucoes);
            Map<String, Boolean> resolvidos = mapaResolvidos(resolucoes);

            List<Candidato> passando = new ArrayList<>(passandoPorVaga.getOrDefault(vaga.getId(), List.of()));
            List<Candidato> espera = new ArrayList<>(esperaPorVaga.getOrDefault(vaga.getId(), List.of()));
            passando.sort(comparador);
            espera.sort(comparador);

            int capacidadePassando = vaga.getQtdBolsistas() + vaga.getQtdVoluntarios();
            if (passando.size() > capacidadePassando) {
                marcarEmpateUltimosDois(passando);
            }

            int qtdBolsistasVaga = vaga.getQtdBolsistas();
            if (qtdBolsistasVaga > 0 && qtdBolsistasVaga < passando.size()) {
                Candidato dentro = passando.get(qtdBolsistasVaga - 1);
                Candidato fora = passando.get(qtdBolsistasVaga);
                if (dentro.pontuacao.compareTo(fora.pontuacao) == 0
                        && !resolvidos.containsKey(chavePar(dentro.estudante.getId(), fora.estudante.getId()))) {
                    dentro.empate = true;
                    fora.empate = true;
                }
            }

            List<Candidato> finalDaVaga = new ArrayList<>();
            for (int i = 0; i < passando.size(); i++) {
                Candidato c = passando.get(i);
                c.posicao = i + 1;
                c.tipo = (i < qtdBolsistasVaga) ? Classificacao.Tipo.BOLSISTA : Classificacao.Tipo.VOLUNTARIO;
                finalDaVaga.add(c);
            }

            if (espera.size() > vaga.getQtdListaEspera()) {
                marcarEmpateUltimosDois(espera);
            }
            int offset = passando.size();
            for (int i = 0; i < espera.size(); i++) {
                Candidato c = espera.get(i);
                c.posicao = offset + i + 1;
                c.tipo = Classificacao.Tipo.LISTA_ESPERA;
                finalDaVaga.add(c);
            }

            resultados.put(vaga.getId(), finalDaVaga);
        }

        return resultados;
    }

    private void marcarEmpateUltimosDois(List<Candidato> lista) {
        int n = lista.size();
        if (n >= 2) {
            lista.get(n - 2).empate = true;
            lista.get(n - 1).empate = true;
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

    private List<Opcao> opcoesDoEstudante(Estudante e) {
        List<Opcao> opcoes = new ArrayList<>();
        if (e.getOpcao1() != null && e.getMediaOpcao1() != null) {
            opcoes.add(new Opcao(e.getOpcao1().getId(), e.getIra().add(e.getMediaOpcao1())));
        }
        if (e.getOpcao2() != null && e.getMediaOpcao2() != null) {
            opcoes.add(new Opcao(e.getOpcao2().getId(), e.getIra().add(e.getMediaOpcao2())));
        }
        return opcoes;
    }

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

    // Uma opção de um estudante (vaga + pontuação nela), já na ordem de
    // preferência (1ª opção antes da 2ª) em que ele vai propor
    private record Opcao(UUID vagaId, BigDecimal pontuacao) {}

    // Candidato provisoriamente (ou definitivamente) alocado numa vaga,
    // dentro de uma fase (Passando ou Espera)
    private static class Candidato {
        Estudante estudante;
        BigDecimal pontuacao;
        Integer posicao;
        Classificacao.Tipo tipo;
        boolean empate = false;

        Candidato(Estudante estudante, BigDecimal pontuacao) {
            this.estudante = estudante;
            this.pontuacao = pontuacao;
        }
    }
}
