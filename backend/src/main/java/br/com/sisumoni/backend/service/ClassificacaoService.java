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
import java.util.List;
import java.util.Map;
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

    // ══════════════════════════════════════════════════════════════
    //  RECÁLCULO
    // ══════════════════════════════════════════════════════════════
    @Transactional
    public void recalcular(UUID vagaId) {
        Vaga vaga = vagaRepository.findById(vagaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Vaga não encontrada com id: " + vagaId));

        // 1. Buscar candidatos (1ª ou 2ª opção apontando para esta vaga)
        List<Candidato> candidatos = buscarCandidatos(vagaId);

        // 2. Carregar resoluções de empate existentes desta vaga
        List<ResolucaoEmpate> resolucoes = resolucaoRepository.findByVagaIdComEstudantes(vagaId);

        // 3. Ordenar: 1º por pontuação (desc); 2º pelo desempate decidido pelo operador
        candidatos.sort(construirComparador(resolucoes));

        // 4. Atribuir posição e tipo
        int totalBolsista = vaga.getQtdBolsistas();
        int totalVoluntario = vaga.getQtdVoluntarios();
        int totalEspera = vaga.getQtdListaEspera();

        for (int i = 0; i < candidatos.size(); i++) {
            Candidato c = candidatos.get(i);
            int posicao = i + 1;
            c.posicao = posicao;
            if (posicao <= totalBolsista) {
                c.tipo = Classificacao.Tipo.BOLSISTA;
            } else if (posicao <= totalBolsista + totalVoluntario) {
                c.tipo = Classificacao.Tipo.VOLUNTARIO;
            } else if (posicao <= totalBolsista + totalVoluntario + totalEspera) {
                c.tipo = Classificacao.Tipo.LISTA_ESPERA;
            } else {
                c.tipo = null;
            }
        }

        // 5. Faxina: descartar resoluções que não fazem mais sentido (regra de reabertura)
        limparResolucoesObsoletas(candidatos, resolucoes,
                totalBolsista, totalVoluntario, totalEspera);

        // 6. Detectar empate de dois na fronteira — ignorando pares já resolvidos
        detectarEmpateNaFronteira(candidatos, resolucoes,
                totalBolsista, totalVoluntario, totalEspera);

        // 7. Persistir (atualizar o que mudou)
        sincronizar(vaga, candidatos);
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

        // Recalcular já aplicando a decisão
        recalcular(vagaId);
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

    private List<Candidato> buscarCandidatos(UUID vagaId) {
        List<Candidato> candidatos = new ArrayList<>();
        for (Estudante e : estudanteRepository.findAll()) {
            if (e.getOpcao1() != null && e.getOpcao1().getId().equals(vagaId)
                    && e.getMediaOpcao1() != null) {
                candidatos.add(new Candidato(e, e.getIra().add(e.getMediaOpcao1())));
            } else if (e.getOpcao2() != null && e.getOpcao2().getId().equals(vagaId)
                    && e.getMediaOpcao2() != null) {
                candidatos.add(new Candidato(e, e.getIra().add(e.getMediaOpcao2())));
            }
        }
        return candidatos;
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
        // Mapa: para um par empatado, quem é o vencedor
        // chave "menorId|maiorId" -> id do vencedor
        Map<String, UUID> vencedores = new HashMap<>();
        for (ResolucaoEmpate r : resolucoes) {
            vencedores.put(chavePar(r.getVencedor().getId(), r.getPerdedor().getId()),
                    r.getVencedor().getId());
        }

        return (a, b) -> {
            int porPontuacao = b.pontuacao.compareTo(a.pontuacao); // desc
            if (porPontuacao != 0) return porPontuacao;

            // Empate real: existe decisão para este par?
            UUID vencedor = vencedores.get(chavePar(a.estudante.getId(), b.estudante.getId()));
            if (vencedor != null) {
                if (vencedor.equals(a.estudante.getId())) return -1; // a vem antes
                if (vencedor.equals(b.estudante.getId())) return 1;  // b vem antes
            }
            // Sem decisão: mantém estável por id (determinístico)
            return a.estudante.getId().compareTo(b.estudante.getId());
        };
    }

    /**
     * Regra de reabertura: descarta resoluções cujo par não empata mais
     * ou não disputa mais uma fronteira.
     */
    private void limparResolucoesObsoletas(List<Candidato> candidatos,
                                           List<ResolucaoEmpate> resolucoes,
                                           int tb, int tv, int te) {
        Map<UUID, Candidato> porEstudante = new HashMap<>();
        for (Candidato c : candidatos) porEstudante.put(c.estudante.getId(), c);

        int[] fronteiras = { tb, tb + tv, tb + tv + te };

        List<ResolucaoEmpate> obsoletas = new ArrayList<>();
        for (ResolucaoEmpate r : resolucoes) {
            Candidato v = porEstudante.get(r.getVencedor().getId());
            Candidato p = porEstudante.get(r.getPerdedor().getId());

            // Um dos dois saiu da vaga
            if (v == null || p == null) { obsoletas.add(r); continue; }

            // Não empatam mais em pontuação
            if (v.pontuacao.compareTo(p.pontuacao) != 0) { obsoletas.add(r); continue; }

            // O par não está mais atravessando nenhuma fronteira
            boolean naFronteira = false;
            for (int f : fronteiras) {
                if (f <= 0 || f >= candidatos.size()) continue;
                Candidato dentro = candidatos.get(f - 1);
                Candidato fora = candidatos.get(f);
                boolean envolveOPar =
                        (ehDoPar(dentro, r) && ehDoPar(fora, r));
                if (envolveOPar) { naFronteira = true; break; }
            }
            if (!naFronteira) obsoletas.add(r);
        }

        for (ResolucaoEmpate r : obsoletas) {
            resolucoes.remove(r);
            resolucaoRepository.delete(r);
        }
    }

    private boolean ehDoPar(Candidato c, ResolucaoEmpate r) {
        UUID id = c.estudante.getId();
        return id.equals(r.getVencedor().getId()) || id.equals(r.getPerdedor().getId());
    }

    private void detectarEmpateNaFronteira(List<Candidato> candidatos,
                                           List<ResolucaoEmpate> resolucoes,
                                           int tb, int tv, int te) {
        // Pares já resolvidos não geram alerta
        Map<String, Boolean> resolvidos = new HashMap<>();
        for (ResolucaoEmpate r : resolucoes) {
            resolvidos.put(chavePar(r.getVencedor().getId(), r.getPerdedor().getId()), true);
        }

        int[] fronteiras = { tb, tb + tv, tb + tv + te };
        for (int fronteira : fronteiras) {
            if (fronteira <= 0 || fronteira >= candidatos.size()) continue;

            Candidato dentro = candidatos.get(fronteira - 1);
            Candidato fora = candidatos.get(fronteira);

            if (dentro.pontuacao.compareTo(fora.pontuacao) == 0) {
                // Se este par já foi resolvido, não marca empate
                boolean jaResolvido = resolvidos.containsKey(
                        chavePar(dentro.estudante.getId(), fora.estudante.getId()));
                if (!jaResolvido) {
                    dentro.empate = true;
                    fora.empate = true;
                }
            }
            // TODO: empate múltiplo (3+ na mesma fronteira) — pós-MVP
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