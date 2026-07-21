package br.com.sisumoni.backend.service;


import br.com.sisumoni.backend.domain.Turma;
import br.com.sisumoni.backend.domain.Vaga;
import br.com.sisumoni.backend.dto.VagaRequest;
import br.com.sisumoni.backend.exception.RecursoNaoEncontradoException;
import br.com.sisumoni.backend.exception.RegraDeNegocioException;
import br.com.sisumoni.backend.repository.DepartamentoRepository;
import br.com.sisumoni.backend.repository.TurmaRepository;
import br.com.sisumoni.backend.repository.VagaRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class VagaService {
    
    private final VagaRepository vagaRepository;
    private final DepartamentoRepository departamentoRepository;
    private final TurmaRepository turmaRepository;

    public VagaService(VagaRepository vagaRepository, DepartamentoRepository departamentoRepository, TurmaRepository turmaRepository) {
        this.vagaRepository = vagaRepository;
        this.departamentoRepository = departamentoRepository;
        this.turmaRepository = turmaRepository;
    }

    public List<Vaga> listar() {
        return vagaRepository.findAllComRelacionamentos();
    }

    public Vaga buscarPorId(UUID id) {
        return vagaRepository.findByIdComRelacionamentos(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Vaga não encontrada com o ID: " + id));
    }

    public Vaga cadastrar(VagaRequest request) {
        var departamento = departamentoRepository.findById(request.departamentoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Departamento não encontrado com o ID: " + request.departamentoId()));
        
        Set<Turma> turmas = new HashSet<>();
        if (request.turmasIds() != null) {
            for (UUID turmaId : request.turmasIds()) {
                Turma turma = turmaRepository.findById(turmaId)
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Turma não encontrada com o ID: " + turmaId));
                turmas.add(turma);
            }
        }

        Vaga vaga = new Vaga();
        vaga.setDisciplina(request.disciplina());
        vaga.setProfessor(request.professor());
        vaga.setDepartamento(departamento);
        vaga.setQtdBolsistas(request.qtdBolsistas());
        vaga.setQtdVoluntarios(request.qtdVoluntarios());
        vaga.setQtdListaEspera(request.qtdListaEspera());
        vaga.setTurmas(turmas);

        return vagaRepository.save(vaga);
    }

    public void deletar(UUID id) {
        Vaga vaga = buscarPorId(id);
        
        vagaRepository.delete(vaga);
    }

}
