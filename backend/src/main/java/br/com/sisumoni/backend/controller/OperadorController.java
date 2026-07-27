package br.com.sisumoni.backend.controller;

import br.com.sisumoni.backend.dto.OperadorRequest;
import br.com.sisumoni.backend.dto.OperadorResponse;
import br.com.sisumoni.backend.service.OperadorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/operadores")
@PreAuthorize("hasRole('ADMIN')")
public class OperadorController {

    private final OperadorService operadorService;

    public OperadorController(OperadorService operadorService) {
        this.operadorService = operadorService;
    }

    @GetMapping
    public ResponseEntity<List<OperadorResponse>> listar() {
        return ResponseEntity.ok(operadorService.listar());
    }

    @PostMapping
    public ResponseEntity<OperadorResponse> cadastrar(
            @Valid @RequestBody OperadorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(operadorService.cadastrar(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        operadorService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}