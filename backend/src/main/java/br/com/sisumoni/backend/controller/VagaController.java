package br.com.sisumoni.backend.controller;

import br.com.sisumoni.backend.domain.Vaga;
import br.com.sisumoni.backend.dto.VagaRequest;
import br.com.sisumoni.backend.service.VagaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vagas")
public class VagaController {

    private final VagaService vagaService;

    public VagaController(VagaService vagaService) {
        this.vagaService = vagaService;
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
}