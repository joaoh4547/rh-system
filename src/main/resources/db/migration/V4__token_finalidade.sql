-- Finalidade do token (ativação ou redefinição de senha)
ALTER TABLE token_ativacao
    ADD COLUMN finalidade VARCHAR(30) NOT NULL DEFAULT 'ATIVACAO';
