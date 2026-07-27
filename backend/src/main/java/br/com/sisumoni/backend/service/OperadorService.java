package br.com.sisumoni.backend.service;

import br.com.sisumoni.backend.domain.Turma;
import br.com.sisumoni.backend.domain.Usuario;
import br.com.sisumoni.backend.dto.OperadorRequest;
import br.com.sisumoni.backend.dto.OperadorResponse;
import br.com.sisumoni.backend.exception.RecursoNaoEncontradoException;
import br.com.sisumoni.backend.exception.RegraDeNegocioException;
import br.com.sisumoni.backend.repository.TurmaRepository;
import br.com.sisumoni.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class OperadorService {

    private final UsuarioRepository usuarioRepository;
    private final TurmaRepository turmaRepository;
    private final PasswordEncoder passwordEncoder;

    public OperadorService(UsuarioRepository usuarioRepository,
                           TurmaRepository turmaRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.turmaRepository = turmaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<OperadorResponse> listar() {
        return usuarioRepository.findByPerfil(Usuario.Perfil.OPERADOR)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public OperadorResponse cadastrar(OperadorRequest request) {
        if (usuarioRepository.findByEmail(request.email()).isPresent()) {
            throw new RegraDeNegocioException(
                    "Já existe um usuário com o e-mail: " + request.email());
        }

        Set<Turma> turmas = new HashSet<>();
        for (UUID turmaId : request.turmasIds()) {
            Turma turma = turmaRepository.findById(turmaId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Turma não encontrada com id: " + turmaId));
            turmas.add(turma);
        }

        Usuario operador = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senhaHash(passwordEncoder.encode(request.senha()))
                .perfil(Usuario.Perfil.OPERADOR)
                .ativo(true)
                .build();
        operador.setTurmas(turmas);

        return toResponse(usuarioRepository.save(operador));
    }

    public void deletar(UUID id) {
        Usuario operador = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Operador não encontrado com id: " + id));

        if (operador.getPerfil() == Usuario.Perfil.ADMIN) {
            throw new RegraDeNegocioException(
                    "Não é possível remover um administrador por aqui");
        }

        usuarioRepository.delete(operador);
    }

    private OperadorResponse toResponse(Usuario u) {
        List<String> nomesTurmas = u.getTurmas().stream()
                .map(Turma::getNome)
                .toList();
        return new OperadorResponse(
                u.getId(), u.getNome(), u.getEmail(), u.isAtivo(), nomesTurmas);
    }
}