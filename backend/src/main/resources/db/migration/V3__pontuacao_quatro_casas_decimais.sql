-- ============================================================
-- SISUMONI — V3
-- Pontuação da classificação (IRA + Média) precisa acompanhar
-- as 4 casas decimais do IRA (V2), senão o arredondamento
-- esconde diferenças reais entre candidatos na resposta da API.
-- ============================================================

ALTER TABLE classificacao
    ALTER COLUMN pontuacao TYPE NUMERIC(6,4);
