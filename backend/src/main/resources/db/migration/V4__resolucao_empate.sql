-- Registra a decisão de desempate tomada por um operador.
-- A decisão pertence ao PAR (dois estudantes) dentro de uma VAGA.
CREATE TABLE resolucao_empate (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vaga_id             UUID NOT NULL REFERENCES vaga(id) ON DELETE CASCADE,
    estudante_vencedor  UUID NOT NULL REFERENCES estudante(id) ON DELETE CASCADE,
    estudante_perdedor  UUID NOT NULL REFERENCES estudante(id) ON DELETE CASCADE,
    pontuacao_empate    NUMERIC(6,4) NOT NULL,   -- a pontuação em que empataram
    resolvido_por       UUID REFERENCES usuario(id),
    resolvido_em        TIMESTAMP NOT NULL DEFAULT now(),
    -- Um par (vaga + os dois estudantes) só pode ter uma resolução ativa
    UNIQUE (vaga_id, estudante_vencedor, estudante_perdedor)
);

-- Índice para buscar rápido as resoluções de uma vaga durante o recálculo
CREATE INDEX idx_resolucao_vaga ON resolucao_empate (vaga_id);
