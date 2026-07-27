package br.com.sisumoni.backend.service;

import br.com.sisumoni.backend.domain.Classificacao;
import br.com.sisumoni.backend.domain.Estudante;
import br.com.sisumoni.backend.domain.Vaga;
import br.com.sisumoni.backend.exception.RecursoNaoEncontradoException;
import br.com.sisumoni.backend.repository.ClassificacaoRepository;
import br.com.sisumoni.backend.repository.EstudanteRepository;
import br.com.sisumoni.backend.repository.VagaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.sisumoni.backend.domain.Usuario;
import br.com.sisumoni.backend.dto.ClassificacaoResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ClassificacaoService {

    private final ClassificacaoRepository classificacaoRepository;
    private final EstudanteRepository estudanteRepository;
    private final VagaRepository vagaRepository;

    public ClassificacaoService(ClassificacaoRepository classificacaoRepository,
                                EstudanteRepository estudanteRepository,
                                VagaRepository vagaRepository) {
        this.classificacaoRepository = classificacaoRepository;
        this.estudanteRepository = estudanteRepository;
        this.vagaRepository = vagaRepository;
    }

    /**
     * Recalcula a classificação de UMA vaga.
     * Chamado pelo EstudanteService sempre que um estudante daquela vaga muda.
     */
    @Transactional
    public void recalcular(UUID vagaId) {
        Vaga vaga = vagaRepository.findById(vagaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Vaga não encontrada com id: " + vagaId));

        // 1. Buscar candidatos: quem escolheu esta vaga na 1ª OU 2ª opção
        List<Candidato> candidatos = buscarCandidatos(vagaId);

        // 2. Ordenar por pontuação decrescente
        candidatos.sort(Comparator.comparing((Candidato c) -> c.pontuacao).reversed());

        // 3. Atribuir posição e tipo conforme as quantidades da vaga
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
                c.tipo = null; // fora de qualquer vaga
            }
        }

        // 4. Detectar empate de dois na fronteira
        detectarEmpateNaFronteira(candidatos, totalBolsista, totalVoluntario, totalEspera);

        // 5. Persistir: atualizar o que mudou, criar o que falta, remover o que saiu
        sincronizar(vaga, candidatos);
    }

    // ── Buscar candidatos considerando 1ª e 2ª opção ──────────────
    private List<Candidato> buscarCandidatos(UUID vagaId) {
        List<Candidato> candidatos = new ArrayList<>();

        for (Estudante e : estudanteRepository.findAll()) {
            // Concorre por 1ª opção?
            if (e.getOpcao1() != null && e.getOpcao1().getId().equals(vagaId)
                    && e.getMediaOpcao1() != null) {
                BigDecimal pontuacao = e.getIra().add(e.getMediaOpcao1());
                candidatos.add(new Candidato(e, pontuacao));
            }
            // Concorre por 2ª opção?
            else if (e.getOpcao2() != null && e.getOpcao2().getId().equals(vagaId)
                    && e.getMediaOpcao2() != null) {
                BigDecimal pontuacao = e.getIra().add(e.getMediaOpcao2());
                candidatos.add(new Candidato(e, pontuacao));
            }
        }
        return candidatos;
    }

    // ── Detectar empate de dois na fronteira ──────────────────────
    private void detectarEmpateNaFronteira(List<Candidato> candidatos,
                                           int totalBolsista, int totalVoluntario, int totalEspera) {
        // Fronteiras = últimas posições de cada tipo
        int[] fronteiras = {
                totalBolsista,
                totalBolsista + totalVoluntario,
                totalBolsista + totalVoluntario + totalEspera
        };

        for (int fronteira : fronteiras) {
            if (fronteira <= 0 || fronteira >= candidatos.size()) continue;

            // Candidato na última posição do tipo (índice fronteira-1)
            // e o primeiro fora (índice fronteira)
            Candidato dentro = candidatos.get(fronteira - 1);
            Candidato fora = candidatos.get(fronteira);

            // Empate se as pontuações são idênticas
            if (dentro.pontuacao.compareTo(fora.pontuacao) == 0) {
                dentro.empate = true;
                fora.empate = true;
            }
            // TODO: empate múltiplo (3+ empatados na mesma fronteira) — RF012
        }
    }

    // ── Sincronizar com o banco (atualizar o que mudou) ───────────
    private void sincronizar(Vaga vaga, List<Candidato> candidatos) {
        List<Classificacao> existentes = classificacaoRepository.findByVagaId(vaga.getId());

        // Mapa por estudanteId para achar rápido
        Map<UUID, Classificacao> mapaExistente = new HashMap<>();
        for (Classificacao c : existentes) {
            mapaExistente.put(c.getEstudante().getId(), c);
        }

        List<Classificacao> paraSalvar = new ArrayList<>();
        List<UUID> idsAtuais = new ArrayList<>();

        for (Candidato cand : candidatos) {
            idsAtuais.add(cand.estudante.getId());
            Classificacao c = mapaExistente.get(cand.estudante.getId());

            if (c == null) {
                // Novo registro
                c = new Classificacao();
                c.setEstudante(cand.estudante);
                c.setVaga(vaga);
            }
            // Atualiza os campos (novo ou existente)
            c.setPontuacao(cand.pontuacao);
            c.setPosicao(cand.posicao);
            c.setTipo(cand.tipo);
            c.setEmpate(cand.empate);
            paraSalvar.add(c);
        }

        // Remove classificações de estudantes que não concorrem mais a esta vaga
        for (Classificacao c : existentes) {
            if (!idsAtuais.contains(c.getEstudante().getId())) {
                classificacaoRepository.delete(c);
            }
        }

        classificacaoRepository.saveAll(paraSalvar);
    }

    // ── Classe auxiliar interna ───────────────────────────────────
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

        // novo método público no ClassificacaoService
    @Transactional(readOnly = true)
    public List<ClassificacaoResponse> listarPorVaga(UUID vagaId) {
        Usuario logado = (Usuario) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
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
}