package br.com.sisumoni.backend.service;

import br.com.sisumoni.backend.domain.Estudante;
import br.com.sisumoni.backend.domain.Turma;
import br.com.sisumoni.backend.domain.Usuario;
import br.com.sisumoni.backend.domain.Vaga;
import br.com.sisumoni.backend.dto.EstudanteRequest;
import br.com.sisumoni.backend.exception.RecursoNaoEncontradoException;
import br.com.sisumoni.backend.exception.RegraDeNegocioException;
import br.com.sisumoni.backend.repository.EstudanteRepository;
import br.com.sisumoni.backend.repository.TurmaRepository;
import br.com.sisumoni.backend.repository.VagaRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class EstudanteService {

    private final EstudanteRepository estudanteRepository;
    private final TurmaRepository turmaRepository;
    private final VagaRepository vagaRepository;
    // ClassificacaoService será injetado na issue #18
    // private final ClassificacaoService classificacaoService;

    public EstudanteService(EstudanteRepository estudanteRepository,
                            TurmaRepository turmaRepository,
                            VagaRepository vagaRepository) {
        this.estudanteRepository = estudanteRepository;
        this.turmaRepository = turmaRepository;
        this.vagaRepository = vagaRepository;
    }

    // ── Listagem filtrada por perfil ──────────────────────────────
    public List<Estudante> listar() {
        Usuario logado = usuarioLogado();

        if (logado.getPerfil() == Usuario.Perfil.ADMIN) {
            return estudanteRepository.findAllComRelacionamentos();
        }

        // Operador vê só os estudantes das turmas dele
        return logado.getTurmas().stream()
                .flatMap(turma -> estudanteRepository.findByTurmaIdComRelacionamentos(turma.getId()).stream())
                .toList();
    }

    public Estudante buscarPorId(UUID id) {
        Estudante estudante = estudanteRepository.findByIdComRelacionamentos(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Estudante não encontrado com id: " + id));
        verificarAcessoATurma(estudante.getTurma());
        return estudante;
    }

    // ── Cadastro ──────────────────────────────────────────────────
    @Transactional
    public Estudante cadastrar(EstudanteRequest request) {
        Turma turma = turmaRepository.findById(request.turmaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Turma não encontrada com id: " + request.turmaId()));

        // Verifica se o operador pode mexer nesta turma
        verificarAcessoATurma(turma);

        if (estudanteRepository.existsByMatricula(request.matricula())) {
            throw new RegraDeNegocioException(
                    "Já existe um estudante com a matrícula: " + request.matricula());
        }

        Estudante estudante = new Estudante();
        preencher(estudante, request, turma);
        Estudante salvo = estudanteRepository.save(estudante);

        // Gancho do recálculo — issue #18
        // classificacaoService.recalcular(salvo.getOpcao1().getId());
        // if (salvo.getOpcao2() != null) classificacaoService.recalcular(salvo.getOpcao2().getId());

        return salvo;
    }

    // ── Edição ────────────────────────────────────────────────────
    @Transactional
    public Estudante atualizar(UUID id, EstudanteRequest request) {
        Estudante estudante = estudanteRepository.findByIdComRelacionamentos(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Estudante não encontrado com id: " + id));

        // Verifica acesso à turma atual do estudante
        verificarAcessoATurma(estudante.getTurma());

        Turma novaTurma = turmaRepository.findById(request.turmaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Turma não encontrada com id: " + request.turmaId()));

        // Se está mudando de turma, verifica acesso à nova também
        verificarAcessoATurma(novaTurma);

        // Matrícula única (exceto a do próprio estudante)
        if (!estudante.getMatricula().equals(request.matricula())
                && estudanteRepository.existsByMatricula(request.matricula())) {
            throw new RegraDeNegocioException(
                    "Já existe um estudante com a matrícula: " + request.matricula());
        }

        preencher(estudante, request, novaTurma);
        Estudante salvo = estudanteRepository.save(estudante);

        // Gancho do recálculo — issue #18
        // classificacaoService.recalcular(salvo.getOpcao1().getId());
        // if (salvo.getOpcao2() != null) classificacaoService.recalcular(salvo.getOpcao2().getId());

        return salvo;
    }

    // ── Deleção ───────────────────────────────────────────────────
    @Transactional
    public void deletar(UUID id) {
        Estudante estudante = estudanteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Estudante não encontrado com id: " + id));

        verificarAcessoATurma(estudante.getTurma());

        UUID opcao1Id = estudante.getOpcao1() != null ? estudante.getOpcao1().getId() : null;
        UUID opcao2Id = estudante.getOpcao2() != null ? estudante.getOpcao2().getId() : null;

        estudanteRepository.delete(estudante);

        // Gancho do recálculo — issue #18
        // if (opcao1Id != null) classificacaoService.recalcular(opcao1Id);
        // if (opcao2Id != null) classificacaoService.recalcular(opcao2Id);
    }

    // ── Helpers ───────────────────────────────────────────────────

    private void preencher(Estudante estudante, EstudanteRequest request, Turma turma) {
        Vaga opcao1 = vagaRepository.findByIdComRelacionamentos(request.opcao1Id())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Vaga da 1ª opção não encontrada"));

        estudante.setNome(request.nome());
        estudante.setMatricula(request.matricula());
        estudante.setNomeFantasia(request.nomeFantasia());
        estudante.setTurma(turma);
        estudante.setIra(request.ira().setScale(4, RoundingMode.HALF_UP));
        estudante.setOpcao1(opcao1);
        estudante.setMediaOpcao1(BigDecimal.valueOf(request.mediaOpcao1()));

        if (request.opcao2Id() != null) {
            Vaga opcao2 = vagaRepository.findByIdComRelacionamentos(request.opcao2Id())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Vaga da 2ª opção não encontrada"));
            estudante.setOpcao2(opcao2);
            estudante.setMediaOpcao2(BigDecimal.valueOf(request.mediaOpcao2()));
        } else {
            estudante.setOpcao2(null);
            estudante.setMediaOpcao2(null);
        }
    }

    private Usuario usuarioLogado() {
        return (Usuario) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }

    private void verificarAcessoATurma(Turma turma) {
        Usuario logado = usuarioLogado();

        if (logado.getPerfil() == Usuario.Perfil.ADMIN) {
            return; // admin acessa qualquer turma
        }

        boolean ehResponsavel = logado.getTurmas().stream()
                .anyMatch(t -> t.getId().equals(turma.getId()));

        if (!ehResponsavel) {
            throw new AccessDeniedException(
                    "Você não é responsável pela turma deste estudante");
        }
    }
}