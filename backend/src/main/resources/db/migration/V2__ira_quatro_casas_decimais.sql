-- ============================================================
-- SISUMONI — V2
-- IRA passa a ser armazenado obrigatoriamente com 4 casas decimais
-- ============================================================

ALTER TABLE estudante
    ALTER COLUMN ira TYPE NUMERIC(6,4);
