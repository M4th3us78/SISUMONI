package br.com.sisumoni.backend.controller;

import br.com.sisumoni.backend.dto.AtualizarConfiguracaoRequest;
import br.com.sisumoni.backend.dto.ConfiguracaoResponse;
import br.com.sisumoni.backend.service.ConfiguracaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configuracoes")
public class ConfiguracaoController {

    private final ConfiguracaoService configuracaoService;

    public ConfiguracaoController(ConfiguracaoService configuracaoService) {
        this.configuracaoService = configuracaoService;
    }

    @GetMapping
    public ResponseEntity<ConfiguracaoResponse> obter() {
        return ResponseEntity.ok(
                new ConfiguracaoResponse(configuracaoService.obter().isCadastroEstudanteBloqueado()));
    }

    @PutMapping("/cadastro-estudante")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConfiguracaoResponse> atualizarCadastroEstudante(
            @Valid @RequestBody AtualizarConfiguracaoRequest request) {
        var configuracao = configuracaoService.atualizarCadastroEstudanteBloqueado(request.bloqueado());
        return ResponseEntity.ok(new ConfiguracaoResponse(configuracao.isCadastroEstudanteBloqueado()));
    }
}
