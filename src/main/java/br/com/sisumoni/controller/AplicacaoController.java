package br.com.sisumoni.controller;

import br.com.sisumoni.model.Aplicacao;
import br.com.sisumoni.service.AplicacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/aplicacoes")
@CrossOrigin(origins = "*")
public class AplicacaoController {
    @Autowired
    private AplicacaoService aplicacaoService;

    @GetMapping("/{id}")
    public ResponseEntity<Aplicacao> obter(@PathVariable Long id) {
        Optional<Aplicacao> aplicacao = aplicacaoService.obterAplicacao(id);
        return aplicacao.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/estudante/{estudanteId}")
    public ResponseEntity<List<Aplicacao>> listarPorEstudante(@PathVariable Long estudanteId) {
        return ResponseEntity.ok(aplicacaoService.listarPorEstudante(estudanteId));
    }

    @GetMapping("/vaga/{vagaId}")
    public ResponseEntity<List<Aplicacao>> listarPorVaga(@PathVariable Long vagaId) {
        return ResponseEntity.ok(aplicacaoService.listarPorVaga(vagaId));
    }

    @GetMapping("/pendentes")
    public ResponseEntity<List<Aplicacao>> listarPendentes() {
        return ResponseEntity.ok(aplicacaoService.listarPendentes());
    }

    @PostMapping("/candidatar")
    public ResponseEntity<Aplicacao> candidatar(@RequestBody Map<String, Long> request) {
        try {
            Long estudanteId = request.get("estudanteId");
            Long vagaId = request.get("vagaId");
            Aplicacao aplicacao = aplicacaoService.candidatar(estudanteId, vagaId);
            return ResponseEntity.status(HttpStatus.CREATED).body(aplicacao);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/aprovar")
    public ResponseEntity<Aplicacao> aprovar(@PathVariable Long id) {
        Aplicacao aprovada = aplicacaoService.aprovarAplicacao(id);
        if (aprovada != null) {
            return ResponseEntity.ok(aprovada);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/rejeitar")
    public ResponseEntity<Aplicacao> rejeitar(@PathVariable Long id,
                                             @RequestBody Map<String, String> request) {
        String motivo = request.get("motivo");
        Aplicacao rejeitada = aplicacaoService.rejeitarAplicacao(id, motivo);
        if (rejeitada != null) {
            return ResponseEntity.ok(rejeitada);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        aplicacaoService.deletarAplicacao(id);
        return ResponseEntity.noContent().build();
    }
}
