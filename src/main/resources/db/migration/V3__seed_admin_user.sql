-- Usuário admin de teste (já ativo).
-- Login: admin.teste  |  Senha: admin123
-- A senha está com hash BCrypt; troque depois em ambiente real.

INSERT INTO usuario (nome, sobrenome, username, email, senha, status, cpf, rg)
VALUES (
    'Admin',
    'Teste',
    'admin.teste',
    'admin@rhsystem.com',
    '$2b$10$U2e/uzO2hgJ9n50702vA3.bFuqIhV1i/wJRoq31vKJkqTAAgxhFKq',
    'ATIVO',
    '52998224725',
    '123456789'
);
