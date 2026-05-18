-- ============================================================
-- SISUMONI — V1 Schema inicial
-- Flyway aplica este arquivo automaticamente na primeira subida
-- ============================================================

-- Turmas
CREATE TABLE turma (
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL UNIQUE
);

-- Departamentos
CREATE TABLE departamento (
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL UNIQUE
);

-- Vagas de monitoria
CREATE TABLE vaga (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    disciplina       VARCHAR(100) NOT NULL,
    professor        VARCHAR(100) NOT NULL,
    departamento_id  UUID         NOT NULL REFERENCES departamento(id),
    qtd_bolsistas    INT          NOT NULL DEFAULT 0,
    qtd_voluntarios  INT          NOT NULL DEFAULT 0,
    qtd_lista_espera INT          NOT NULL DEFAULT 0
);

-- Relação N:N vaga ↔ turma (quais turmas podem se candidatar)
CREATE TABLE vaga_turma (
    vaga_id  UUID REFERENCES vaga(id)  ON DELETE CASCADE,
    turma_id UUID REFERENCES turma(id) ON DELETE CASCADE,
    PRIMARY KEY (vaga_id, turma_id)
);

-- Usuários (administradores e operadores)
CREATE TABLE usuario (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    nome       VARCHAR(100) NOT NULL,
    email      VARCHAR(150) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    perfil     VARCHAR(20)  NOT NULL CHECK (perfil IN ('ADMIN', 'OPERADOR')),
    ativo      BOOLEAN      NOT NULL DEFAULT TRUE
);

-- Relação N:N operador ↔ turma (turmas pelas quais é responsável)
CREATE TABLE operador_turma (
    usuario_id UUID REFERENCES usuario(id) ON DELETE CASCADE,
    turma_id   UUID REFERENCES turma(id)  ON DELETE CASCADE,
    PRIMARY KEY (usuario_id, turma_id)
);

-- Estudantes
CREATE TABLE estudante (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    nome          VARCHAR(150) NOT NULL,
    matricula     VARCHAR(20)  NOT NULL UNIQUE,
    nome_fantasia VARCHAR(50)  NOT NULL,
    turma_id      UUID         NOT NULL REFERENCES turma(id),
    ira           NUMERIC(5,3) NOT NULL CHECK (ira >= 0 AND ira <= 10),
    opcao1_id     UUID         REFERENCES vaga(id),
    media_opcao1  NUMERIC(4,1) CHECK (media_opcao1 >= 0 AND media_opcao1 <= 10),
    opcao2_id     UUID         REFERENCES vaga(id),
    media_opcao2  NUMERIC(4,1) CHECK (media_opcao2 >= 0 AND media_opcao2 <= 10)
);

-- Classificações calculadas (recalculadas a cada mudança de estudante)
CREATE TABLE classificacao (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    estudante_id UUID         NOT NULL REFERENCES estudante(id) ON DELETE CASCADE,
    vaga_id      UUID         NOT NULL REFERENCES vaga(id)      ON DELETE CASCADE,
    pontuacao    NUMERIC(6,3) NOT NULL,   -- IRA + media_opcao
    posicao      INT,                     -- 1º, 2º, 3º...
    tipo         VARCHAR(20)  CHECK (tipo IN ('BOLSISTA', 'VOLUNTARIO', 'LISTA_ESPERA')),
    empate       BOOLEAN      NOT NULL DEFAULT FALSE,
    UNIQUE (estudante_id, vaga_id)        -- um estudante, uma posição por vaga
);

-- Token de recuperação de senha (RF008 — pós-MVP)
CREATE TABLE token_recuperacao (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID         NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    token      VARCHAR(255) NOT NULL UNIQUE,
    expira_em  TIMESTAMP    NOT NULL,
    usado      BOOLEAN      NOT NULL DEFAULT FALSE
);