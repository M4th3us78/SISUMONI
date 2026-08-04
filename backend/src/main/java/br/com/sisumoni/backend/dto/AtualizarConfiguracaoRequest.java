package br.com.sisumoni.backend.dto;

import jakarta.validation.constraints.NotNull;

public record AtualizarConfiguracaoRequest(
        @NotNull(message = "O campo bloqueado é obrigatório")
        Boolean bloqueado
) {}
