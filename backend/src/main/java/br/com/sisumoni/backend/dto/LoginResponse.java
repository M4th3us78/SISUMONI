package br.com.sisumoni.backend.dto;

// ── Response: dados retornados após login bem-sucedido ────────
public record LoginResponse(
        String token,
        String nome,
        String email,
        String perfil,
        boolean senhaProvisoria
) {}