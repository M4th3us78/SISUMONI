package br.com.sisumoni.backend.dto;

import java.util.List;
import java.util.UUID;

public record OperadorResponse(
        UUID id,
        String nome,
        String email,
        boolean ativo,
        List<String> turmas
) {}