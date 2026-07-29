package br.com.sisumoni.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

public record VagaRequest(
        @NotBlank(message = "A disciplina é obrigatória")
        String disciplina,

        @NotBlank(message = "O professor é obrigatório")
        String professor,

        @NotNull(message = "O departamento é obrigatório")
        UUID departamentoId,

        @Min(value = 0, message = "A quantidade de bolsistas não pode ser negativa")
        Integer qtdBolsistas,

        @Min(value = 0, message = "A quantidade de voluntários não pode ser negativa")
        Integer qtdVoluntarios,

        @Min(value = 1, message = "A lista de espera é sempre 1")
        @Max(value = 1, message = "A lista de espera é sempre 1")
        Integer qtdListaEspera,

        Set<UUID> turmasIds
) {}
