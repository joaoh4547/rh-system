-- Aceite dos termos de uso pelo usuário
ALTER TABLE usuario
    ADD COLUMN termos_aceito_em TIMESTAMP;
