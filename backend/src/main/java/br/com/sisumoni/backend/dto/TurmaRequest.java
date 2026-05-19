package br.com.sisumoni.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record TurmaRequest(
        @NotBlank(message = "Nome é obrigatório")
        String nome
) {}