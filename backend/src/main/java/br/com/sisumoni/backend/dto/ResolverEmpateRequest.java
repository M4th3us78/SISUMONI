package br.com.sisumoni.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ResolverEmpateRequest(
        @NotNull(message = "Vencedor é obrigatório")
        UUID vencedorId,
        @NotNull(message = "Perdedor é obrigatório")
        UUID perdedorId
) {}
