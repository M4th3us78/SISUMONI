CREATE TABLE configuracao_sistema (
    id                           INT PRIMARY KEY DEFAULT 1,
    cadastro_estudante_bloqueado BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT configuracao_sistema_singleton CHECK (id = 1)
);

INSERT INTO configuracao_sistema (id, cadastro_estudante_bloqueado) VALUES (1, false);
