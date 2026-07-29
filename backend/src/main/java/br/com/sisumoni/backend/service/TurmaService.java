package br.com.sisumoni.backend.service;

import br.com.sisumoni.backend.domain.Turma;
import br.com.sisumoni.backend.domain.Usuario;
import br.com.sisumoni.backend.exception.RecursoNaoEncontradoException;
import br.com.sisumoni.backend.exception.RegraDeNegocioException;
import br.com.sisumoni.backend.repository.EstudanteRepository;
import br.com.sisumoni.backend.repository.TurmaRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TurmaService {

    private final TurmaRepository turmaRepository;
    private final EstudanteRepository estudanteRepository;

    public TurmaService(TurmaRepository turmaRepository, EstudanteRepository estudanteRepository) {
        this.turmaRepository = turmaRepository;
        this.estudanteRepository = estudanteRepository;
    }

    // ── Listagem filtrada por perfil ──────────────────────────────
    public List<Turma> listar() {
        Usuario logado = usuarioLogado();

        if (logado.getPerfil() == Usuario.Perfil.ADMIN) {
            return turmaRepository.findAll();
        }

        // Operador vê só as turmas pelas quais é responsável
        return logado.getTurmas().stream().toList();
    }

    private Usuario usuarioLogado() {
        return (Usuario) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
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
    if (estudanteRepository.existsByTurmaId(id)) {
        throw new RegraDeNegocioException(
                "Não é possível deletar uma turma com estudantes vinculados");
    }
    turmaRepository.delete(turma);
}
    
}
