package br.com.sisumoni.service;

import br.com.sisumoni.model.VagaMonitoria;
import br.com.sisumoni.model.enums.StatusVaga;
import br.com.sisumoni.repository.VagaMonitoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class VagaMonitoriaService {
    @Autowired
    private VagaMonitoriaRepository vagaRepository;

    public VagaMonitoria criarVaga(VagaMonitoria vaga) {
        return vagaRepository.save(vaga);
    }

    public Optional<VagaMonitoria> obterVaga(Long id) {
        return vagaRepository.findById(id);
    }

    public List<VagaMonitoria> listarTodas() {
        return vagaRepository.findAll();
    }

    public List<VagaMonitoria> listarAberta() {
        return vagaRepository.findByStatus(StatusVaga.ABERTA);
    }

    public List<VagaMonitoria> listarPorProfessor(Long professorId) {
        return vagaRepository.findByProfessorId(professorId);
    }

    public List<VagaMonitoria> buscarPorDisciplina(String disciplina) {
        return vagaRepository.findByDisciplinaContainingIgnoreCase(disciplina);
    }

    public VagaMonitoria atualizarVaga(Long id, VagaMonitoria vagaAtualizada) {
        Optional<VagaMonitoria> vaga = vagaRepository.findById(id);
        if (vaga.isPresent()) {
            VagaMonitoria v = vaga.get();
            v.setDisciplina(vagaAtualizada.getDisciplina());
            v.setDescricao(vagaAtualizada.getDescricao());
            v.setQuantidadeVagas(vagaAtualizada.getQuantidadeVagas());
            v.setSalarioHora(vagaAtualizada.getSalarioHora());
            v.setCargaHoraria(vagaAtualizada.getCargaHoraria());
            v.setDataInicio(vagaAtualizada.getDataInicio());
            v.setDataFim(vagaAtualizada.getDataFim());
            return vagaRepository.save(v);
        }
        return null;
    }

    public void atualizarStatus(Long id, StatusVaga novoStatus) {
        Optional<VagaMonitoria> vaga = vagaRepository.findById(id);
        if (vaga.isPresent()) {
            VagaMonitoria v = vaga.get();
            v.setStatus(novoStatus);
            vagaRepository.save(v);
        }
    }

    public void deletarVaga(Long id) {
        vagaRepository.deleteById(id);
    }
}
