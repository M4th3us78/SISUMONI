package br.com.sisumoni.backend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public record EstudanteRequest(
        @NotBlank(message = "Nome é obrigatório")
        String nome,

        @NotBlank(message = "Matrícula é obrigatória")
        String matricula,

        @NotBlank(message = "Nome fantasia é obrigatório")
        String nomeFantasia,

        @NotNull(message = "Turma é obrigatória")
        UUID turmaId,

        @NotNull(message = "IRA é obrigatório")
        @DecimalMin(value = "0.0", message = "IRA mínimo é 0")
        @DecimalMax(value = "10.0", message = "IRA máximo é 10")
        @Digits(integer = 2, fraction = 4, message = "IRA deve ter no máximo 4 casas decimais")
        BigDecimal ira,

        @NotNull(message = "1ª opção é obrigatória")
        UUID opcao1Id,

        @NotNull(message = "Média da 1ª opção é obrigatória")
        @DecimalMin(value = "0.0") @DecimalMax(value = "10.0")
        Double mediaOpcao1,

        UUID opcao2Id,

        @DecimalMin(value = "0.0") @DecimalMax(value = "10.0")
        Double mediaOpcao2
) {}