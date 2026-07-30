package br.com.sisumoni.backend.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import java.util.UUID;

public record OperadorAtualizacaoRequest(
    @NotBlank(message = "O nome é obrigatório")
    String nome,

    Set<UUID> turmasIds
){}
