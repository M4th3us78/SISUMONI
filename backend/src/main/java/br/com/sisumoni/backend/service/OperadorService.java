package br.com.sisumoni.backend.service;

import br.com.sisumoni.backend.domain.Turma;
import br.com.sisumoni.backend.domain.Usuario;
import br.com.sisumoni.backend.dto.OperadorAtualizacaoRequest;
import br.com.sisumoni.backend.dto.OperadorRequest;
import br.com.sisumoni.backend.dto.OperadorResponse;
import br.com.sisumoni.backend.dto.ReativarOperadorRequest;
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
        return usuarioRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OperadorResponse cadastrar(OperadorRequest request) {
        if (usuarioRepository.findByEmail(request.email()).isPresent()) {
            throw new RegraDeNegocioException(
                    "Já existe um usuário com o e-mail: " + request.email());
        }

        Set<Turma> turmas = resolverTurmas(request.perfil(), request.turmasIds());

        Usuario operador = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senhaHash(passwordEncoder.encode(request.senha()))
                .perfil(request.perfil())
                .ativo(true)
                .senhaProvisoria(true)
                .build();
        operador.setTurmas(turmas);

        return toResponse(usuarioRepository.save(operador));
    }

    @Transactional
    public OperadorResponse atualizar(UUID id, OperadorAtualizacaoRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário não encontrado com id: " + id));

        usuario.setNome(request.nome());

        if (usuario.getPerfil() == Usuario.Perfil.OPERADOR) {
            usuario.setTurmas(resolverTurmas(Usuario.Perfil.OPERADOR, request.turmasIds()));
        }

        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void deletar(UUID id) {
        Usuario operador = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Operador não encontrado com id: " + id));

        if (operador.getPerfil() == Usuario.Perfil.ADMIN) {
            throw new RegraDeNegocioException(
                    "Não é possível remover um administrador por aqui");
        }

        // Desativa em vez de apagar: o operador perde o acesso ao sistema
        // (login passa a ser negado com "conta inativa"), mas o histórico de
        // estudantes cadastrados por ele continua íntegro.
        operador.setAtivo(false);
        usuarioRepository.save(operador);
    }

    @Transactional
    public OperadorResponse reativar(UUID id, ReativarOperadorRequest request) {
        Usuario operador = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Operador não encontrado com id: " + id));

        if (operador.getPerfil() == Usuario.Perfil.ADMIN) {
            throw new RegraDeNegocioException(
                    "Não é possível reativar um administrador por aqui");
        }

        if (operador.isAtivo()) {
            throw new RegraDeNegocioException("O operador já está ativo");
        }

        operador.setAtivo(true);
        operador.setSenhaHash(passwordEncoder.encode(request.senha()));
        operador.setSenhaProvisoria(true);

        return toResponse(usuarioRepository.save(operador));
    }

    @Transactional
    public void excluirDefinitivamente(UUID id) {
        Usuario operador = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Operador não encontrado com id: " + id));

        if (operador.getPerfil() == Usuario.Perfil.ADMIN) {
            throw new RegraDeNegocioException(
                    "Não é possível remover um administrador por aqui");
        }

        if (operador.isAtivo()) {
            throw new RegraDeNegocioException(
                    "Só é possível excluir definitivamente contas já desativadas");
        }

        usuarioRepository.delete(operador);
    }

    private Set<Turma> resolverTurmas(Usuario.Perfil perfil, Set<UUID> turmasIds) {
        if (perfil == Usuario.Perfil.ADMIN) {
            return new HashSet<>();
        }

        if (turmasIds == null || turmasIds.isEmpty()) {
            throw new RegraDeNegocioException("Selecione ao menos uma turma");
        }

        Set<Turma> turmas = new HashSet<>();
        for (UUID turmaId : turmasIds) {
            Turma turma = turmaRepository.findById(turmaId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Turma não encontrada com id: " + turmaId));
            turmas.add(turma);
        }
        return turmas;
    }

    private OperadorResponse toResponse(Usuario u) {
        List<OperadorResponse.TurmaResumo> turmas = u.getTurmas().stream()
                .map(t -> new OperadorResponse.TurmaResumo(t.getId(), t.getNome()))
                .toList();
        return new OperadorResponse(
                u.getId(), u.getNome(), u.getEmail(), u.isAtivo(), u.getPerfil().name(), turmas);
    }
}
