package br.com.sisumoni.backend.service;

import br.com.sisumoni.backend.domain.Turma;
import br.com.sisumoni.backend.exception.RecursoNaoEncontradoException;
import br.com.sisumoni.backend.exception.RegraDeNegocioException;
//import br.com.sisumoni.backend.repository.EstudanteRepository;
import br.com.sisumoni.backend.repository.TurmaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TurmaService {

    private final TurmaRepository turmaRepository;

    public TurmaService(TurmaRepository turmaRepository) {
        this.turmaRepository = turmaRepository;
    }

    public List<Turma> listar() {
        return turmaRepository.findAll();
    }

    public Turma buscarPorId(UUID id) {
        return turmaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Turma não encontrada com id: " + id));
    }

    public Turma cadastrar(String nome) {
        if (turmaRepository.existsByNome(nome)) {
            throw new RegraDeNegocioException("Já existe uma turma com esse nome");
        }
        Turma turma = new Turma();
        turma.setNome(nome);
        return turmaRepository.save(turma);
    }

    public void deletar(UUID id) {
        Turma turma = buscarPorId(id);
        //verifica se há estudantes vinculados
        //
        turmaRepository.delete(turma);
        }
    
}
