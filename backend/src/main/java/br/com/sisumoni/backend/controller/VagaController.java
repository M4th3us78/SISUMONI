package br.com.sisumoni.backend.controller;

import br.com.sisumoni.backend.domain.Vaga;
import br.com.sisumoni.backend.dto.VagaRequest;
import br.com.sisumoni.backend.service.VagaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import br.com.sisumoni.backend.dto.ClassificacaoResponse;
import br.com.sisumoni.backend.dto.ResolverEmpateRequest;
import br.com.sisumoni.backend.service.ClassificacaoService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vagas")
public class VagaController {

    private final VagaService vagaService;
    private final ClassificacaoService classificacaoService;

    public VagaController(VagaService vagaService,
                            ClassificacaoService classificacaoService) {
        this.vagaService = vagaService;
        this.classificacaoService = classificacaoService;
    }

    @GetMapping
    public ResponseEntity<List<Vaga>> listar() {
        return ResponseEntity.ok(vagaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vaga> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(vagaService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Vaga> cadastrar(@Valid @RequestBody VagaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vagaService.cadastrar(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        vagaService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/classificacao")
    public ResponseEntity<List<ClassificacaoResponse>> classificacao(@PathVariable UUID id) {
        return ResponseEntity.ok(classificacaoService.listarPorVaga(id));
    }

    @PostMapping("/{id}/resolver-empate")
    public ResponseEntity<List<ClassificacaoResponse>> resolverEmpate(
            @PathVariable UUID id,
            @Valid @RequestBody ResolverEmpateRequest request) {
        classificacaoService.resolverEmpate(id, request.vencedorId(), request.perdedorId());
        return ResponseEntity.ok(classificacaoService.listarPorVaga(id));
    }
}