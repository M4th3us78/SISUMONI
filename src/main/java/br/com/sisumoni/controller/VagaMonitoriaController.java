package br.com.sisumoni.controller;

import br.com.sisumoni.model.VagaMonitoria;
import br.com.sisumoni.model.enums.StatusVaga;
import br.com.sisumoni.service.VagaMonitoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vagas")
@CrossOrigin(origins = "*")
public class VagaMonitoriaController {
    @Autowired
    private VagaMonitoriaService vagaService;

    @GetMapping
    public ResponseEntity<List<VagaMonitoria>> listarTodas() {
        return ResponseEntity.ok(vagaService.listarTodas());
    }

    @GetMapping("/abertas")
    public ResponseEntity<List<VagaMonitoria>> listarAbertas() {
        return ResponseEntity.ok(vagaService.listarAberta());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VagaMonitoria> obter(@PathVariable Long id) {
        Optional<VagaMonitoria> vaga = vagaService.obterVaga(id);
        return vaga.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/professor/{professorId}")
    public ResponseEntity<List<VagaMonitoria>> listarPorProfessor(@PathVariable Long professorId) {
        return ResponseEntity.ok(vagaService.listarPorProfessor(professorId));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<VagaMonitoria>> buscarPorDisciplina(@RequestParam String disciplina) {
        return ResponseEntity.ok(vagaService.buscarPorDisciplina(disciplina));
    }

    @PostMapping
    public ResponseEntity<VagaMonitoria> criar(@RequestBody VagaMonitoria vaga) {
        VagaMonitoria novaVaga = vagaService.criarVaga(vaga);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaVaga);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VagaMonitoria> atualizar(@PathVariable Long id,
                                                   @RequestBody VagaMonitoria vaga) {
        VagaMonitoria atualizada = vagaService.atualizarVaga(id, vaga);
        if (atualizada != null) {
            return ResponseEntity.ok(atualizada);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> atualizarStatus(@PathVariable Long id,
                                                @RequestParam StatusVaga status) {
        vagaService.atualizarStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        vagaService.deletarVaga(id);
        return ResponseEntity.noContent().build();
    }
}
