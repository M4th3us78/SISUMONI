package br.com.sisumoni.backend.controller;

import br.com.sisumoni.backend.dto.TurmaRequest;
import br.com.sisumoni.backend.domain.Turma;
import br.com.sisumoni.backend.service.TurmaService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/api/turmas")
public class TurmaController {
    
    private final TurmaService turmaService;

    public TurmaController(TurmaService turmaService){
        this.turmaService = turmaService;
    }

    @GetMapping
    public ResponseEntity<List<Turma>> listar(){
        return ResponseEntity.ok(turmaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Turma> obterPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(turmaService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Turma> cadastrar(@Valid @RequestBody TurmaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(turmaService.cadastrar(request.nome()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        turmaService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    
    
    

}
