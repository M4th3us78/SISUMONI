package br.com.sisumoni.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErroResponse(
        int status,
        String erro,
        String mensagem,
        LocalDateTime timestamp,
        List<String> detalhes
) {}