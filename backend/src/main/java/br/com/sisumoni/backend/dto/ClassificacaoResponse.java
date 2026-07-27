package br.com.sisumoni.backend.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ClassificacaoResponse(
        UUID estudanteId,
        String nomeFantasia,
        String nome,          // preenchido só para admin; null para operador
        Integer posicao,
        BigDecimal pontuacao,
        String tipo,          // BOLSISTA, VOLUNTARIO, LISTA_ESPERA ou null
        boolean empate
) {}