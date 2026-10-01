-- RBAC: as telas e casos de uso passaram a exigir funcionalidades (@RolesAllowed).
-- Garante que exista um grupo administrador (admin = TRUE concede todas as
-- funcionalidades) e vincula o usuário seed de desenvolvimento a ele, para que
-- ninguém fique trancado fora do sistema após a atualização.
-- Idempotente: não cria nada se já houver grupo admin / vínculo.

INSERT INTO rh_group (name, description, active, admin)
SELECT 'Administradores', 'Acesso total ao sistema', TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM rh_group WHERE admin = TRUE);

INSERT INTO rh_user_group (user_id, group_id)
SELECT u.id, (SELECT MIN(g.id) FROM rh_group g WHERE g.admin = TRUE AND g.active = TRUE)
FROM rh_user u
WHERE u.username = 'admin.teste'
  AND EXISTS (SELECT 1 FROM rh_group g WHERE g.admin = TRUE AND g.active = TRUE)
  AND NOT EXISTS (SELECT 1
                  FROM rh_user_group ug
                  JOIN rh_group g ON g.id = ug.group_id
                  WHERE ug.user_id = u.id AND g.admin = TRUE);
