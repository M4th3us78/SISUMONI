package br.com.sisumoni.backend.controller;

import br.com.sisumoni.backend.service.RelatorioService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/classificacao/nomes-fantasia")
    public ResponseEntity<byte[]> nomesFantasia() {
        return responderPdf(relatorioService.gerarRelatorio(false), "classificacao-nomes-fantasia.pdf");
    }

    @GetMapping("/classificacao/nomes-reais")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> nomesReais() {
        return responderPdf(relatorioService.gerarRelatorio(true), "classificacao-nomes-reais.pdf");
    }

    private ResponseEntity<byte[]> responderPdf(byte[] pdf, String nomeArquivo) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"")
                .body(pdf);
    }
}
