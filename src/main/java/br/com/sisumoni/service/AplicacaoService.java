package br.com.sisumoni.service;

import br.com.sisumoni.model.Aplicacao;
import br.com.sisumoni.model.Estudante;
import br.com.sisumoni.model.VagaMonitoria;
import br.com.sisumoni.model.enums.StatusAplicacao;
import br.com.sisumoni.repository.AplicacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AplicacaoService {
    @Autowired
    private AplicacaoRepository aplicacaoRepository;

    @Autowired
    private EstudanteService estudanteService;

    public Aplicacao candidatar(Long estudanteId, Long vagaId) {
        Optional<Estudante> estudante = estudanteService.obterEstudante(estudanteId);
        if (!estudante.isPresent()) {
            throw new IllegalArgumentException("Estudante não encontrado");
        }

        Aplicacao novaAplicacao = new Aplicacao();
        novaAplicacao.setEstudante(estudante.get());
        VagaMonitoria vaga = new VagaMonitoria();
        vaga.setId(vagaId);
        novaAplicacao.setVaga(vaga);
        novaAplicacao.setStatus(StatusAplicacao.PENDENTE);

        return aplicacaoRepository.save(novaAplicacao);
    }

    public Optional<Aplicacao> obterAplicacao(Long id) {
        return aplicacaoRepository.findById(id);
    }

    public List<Aplicacao> listarPorEstudante(Long estudanteId) {
        return aplicacaoRepository.findByEstudanteId(estudanteId);
    }

    public List<Aplicacao> listarPorVaga(Long vagaId) {
        return aplicacaoRepository.findByVagaId(vagaId);
    }

    public List<Aplicacao> listarPendentes() {
        return aplicacaoRepository.findByStatus(StatusAplicacao.PENDENTE);
    }

    public Aplicacao aprovarAplicacao(Long id) {
        Optional<Aplicacao> aplicacao = aplicacaoRepository.findById(id);
        if (aplicacao.isPresent()) {
            Aplicacao a = aplicacao.get();
            a.setStatus(StatusAplicacao.APROVADA);
            a.setDataDecisao(LocalDateTime.now());
            return aplicacaoRepository.save(a);
        }
        return null;
    }

    public Aplicacao rejeitarAplicacao(Long id, String motivo) {
        Optional<Aplicacao> aplicacao = aplicacaoRepository.findById(id);
        if (aplicacao.isPresent()) {
            Aplicacao a = aplicacao.get();
            a.setStatus(StatusAplicacao.REJEITADA);
            a.setMotivoRejeicao(motivo);
            a.setDataDecisao(LocalDateTime.now());
            return aplicacaoRepository.save(a);
        }
        return null;
    }

    public void deletarAplicacao(Long id) {
        aplicacaoRepository.deleteById(id);
    }
}
