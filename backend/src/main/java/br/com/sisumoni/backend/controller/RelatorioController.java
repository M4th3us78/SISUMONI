package br.com.sisumoni.backend.controller;

import br.com.sisumoni.backend.service.RelatorioService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/classificacao/nomes-fantasia")
    public ResponseEntity<byte[]> nomesFantasia() {
        return responderPdf(relatorioService.gerarRelatorio(false), "Classificacao-parcial-" + dataHoje() + ".pdf");
    }

    @GetMapping("/classificacao/nomes-reais")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> nomesReais() {
        return responderPdf(relatorioService.gerarRelatorio(true), "Classificacao-final-" + dataHoje() + ".pdf");
    }

    private String dataHoje() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    private ResponseEntity<byte[]> responderPdf(byte[] pdf, String nomeArquivo) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"")
                .body(pdf);
    }
}
