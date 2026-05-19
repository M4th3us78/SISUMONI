package br.com.sisumoni.backend.controller;

import br.com.sisumoni.backend.domain.Departamento;
import br.com.sisumoni.backend.dto.TurmaRequest;
import br.com.sisumoni.backend.service.DepartamentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import br.com.sisumoni.backend.service.TurmaService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/departamentos")
public class DepartamentoController {
    
    private final DepartamentoService departamentoService;

    public DepartamentoController(DepartamentoService departamentoService){
        this.departamentoService = departamentoService;
    }

    @GetMapping
    public ResponseEntity<List<Departamento>> listar(){
        return ResponseEntity.ok(departamentoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Departamento> obterPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(departamentoService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Departamento> cadastrar(@Valid @RequestBody TurmaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departamentoService.cadastrar(request.nome()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        departamentoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

}
