-- V7 renamed tables/columns to English but left enum VALUES in Portuguese.
-- The V3 seed user has status 'ATIVO', which no longer matches UserStatus.ACTIVE
-- and breaks JPA enum mapping on any fresh database (caught by the H2 test suite).

UPDATE rh_user SET status = 'ACTIVE'               WHERE status = 'ATIVO';
UPDATE rh_user SET status = 'INACTIVE'             WHERE status = 'INATIVO';
UPDATE rh_user SET status = 'BLOCKED'              WHERE status = 'BLOQUEADO';
UPDATE rh_user SET status = 'PENDING_CONFIRMATION' WHERE status = 'PENDENTE_CONFIRMACAO';

-- Token purpose values (V4 used 'ATIVACAO' as default)
UPDATE rh_activation_token SET purpose = 'ACTIVATION'     WHERE purpose = 'ATIVACAO';
UPDATE rh_activation_token SET purpose = 'PASSWORD_RESET' WHERE purpose = 'REDEFINICAO_SENHA';
ALTER TABLE rh_activation_token ALTER COLUMN purpose SET DEFAULT 'ACTIVATION';
