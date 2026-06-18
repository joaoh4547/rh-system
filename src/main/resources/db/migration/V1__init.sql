-- Migration inicial do RH System.
-- Estrutura base: nenhuma tabela de negócio ainda.
-- As tabelas serão adicionadas nas próximas migrations (V2__, V3__, ...).

CREATE TABLE IF NOT EXISTS schema_metadata (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

INSERT INTO schema_metadata (description) VALUES ('Estrutura base inicial');
