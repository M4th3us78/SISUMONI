package br.com.sisumoni.controller;

import br.com.sisumoni.model.Estudante;
import br.com.sisumoni.service.EstudanteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/estudantes")
@CrossOrigin(origins = "*")
public class EstudanteController {
    @Autowired
    private EstudanteService estudanteService;

    @GetMapping
    public ResponseEntity<List<Estudante>> listarTodos() {
        return ResponseEntity.ok(estudanteService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Estudante> obter(@PathVariable Long id) {
        Optional<Estudante> estudante = estudanteService.obterEstudante(id);
        return estudante.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/matricula/{matricula}")
    public ResponseEntity<Estudante> obterPorMatricula(@PathVariable String matricula) {
        Optional<Estudante> estudante = estudanteService.obterPorMatricula(matricula);
        return estudante.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Estudante> criar(@RequestBody Estudante estudante) {
        Estudante novoEstudante = estudanteService.criarEstudante(estudante);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoEstudante);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Estudante> atualizar(@PathVariable Long id,
                                               @RequestBody Estudante estudante) {
        Estudante atualizado = estudanteService.atualizarEstudante(id, estudante);
        if (atualizado != null) {
            return ResponseEntity.ok(atualizado);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        estudanteService.deletarEstudante(id);
        return ResponseEntity.noContent().build();
    }
}
