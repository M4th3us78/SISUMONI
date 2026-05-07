package br.com.sisumoni.service;

import br.com.sisumoni.model.Estudante;
import br.com.sisumoni.repository.EstudanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class EstudanteService {
    @Autowired
    private EstudanteRepository estudanteRepository;

    public Estudante criarEstudante(Estudante estudante) {
        return estudanteRepository.save(estudante);
    }

    public Optional<Estudante> obterEstudante(Long id) {
        return estudanteRepository.findById(id);
    }

    public Optional<Estudante> obterPorMatricula(String matricula) {
        return estudanteRepository.findByMatricula(matricula);
    }

    public Optional<Estudante> obterPorEmail(String email) {
        return estudanteRepository.findByEmail(email);
    }

    public List<Estudante> listarTodos() {
        return estudanteRepository.findAll();
    }

    public Estudante atualizarEstudante(Long id, Estudante estudanteAtualizado) {
        Optional<Estudante> estudante = estudanteRepository.findById(id);
        if (estudante.isPresent()) {
            Estudante e = estudante.get();
            e.setNome(estudanteAtualizado.getNome());
            e.setEmail(estudanteAtualizado.getEmail());
            e.setMediaGeral(estudanteAtualizado.getMediaGeral());
            e.setPeriodoAtual(estudanteAtualizado.getPeriodoAtual());
            return estudanteRepository.save(e);
        }
        return null;
    }

    public void deletarEstudante(Long id) {
        estudanteRepository.deleteById(id);
    }
}
