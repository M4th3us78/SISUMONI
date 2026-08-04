package br.com.sisumoni.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReativarOperadorRequest(
        @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres")
        @NotBlank(message = "A senha é obrigatória")
        String senha
) {}
