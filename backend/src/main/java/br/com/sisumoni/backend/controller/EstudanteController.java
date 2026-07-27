package br.com.sisumoni.backend.controller;

import br.com.sisumoni.backend.domain.Estudante;
import br.com.sisumoni.backend.dto.EstudanteRequest;
import br.com.sisumoni.backend.service.EstudanteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/estudantes")
public class EstudanteController {

    private final EstudanteService estudanteService;

    public EstudanteController(EstudanteService estudanteService) {
        this.estudanteService = estudanteService;
    }

    @GetMapping
    public ResponseEntity<List<Estudante>> listar() {
        return ResponseEntity.ok(estudanteService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Estudante> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(estudanteService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Estudante> cadastrar(
            @Valid @RequestBody EstudanteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(estudanteService.cadastrar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Estudante> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody EstudanteRequest request) {
        return ResponseEntity.ok(estudanteService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        estudanteService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}