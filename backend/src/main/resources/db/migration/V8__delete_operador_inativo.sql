ALTER TABLE resolucao_empate DROP CONSTRAINT resolucao_empate_resolvido_por_fkey;
ALTER TABLE resolucao_empate ADD CONSTRAINT resolucao_empate_resolvido_por_fkey
    FOREIGN KEY (resolvido_por) REFERENCES usuario(id) ON DELETE SET NULL;
