-- Seed de produção: substitui o admin inicial pelos 2 administradores oficiais.
-- Só roda no perfil "prod" (location classpath:db/seed-prod).
--
-- Usuarios (login / senha):
--   eduardodiniz / Gestao01%
--   carlinhosjc  / senha0123%
-- Hashes BCrypt gerados e verificados com a lib do projeto.

DELETE FROM users WHERE login = 'admin';

INSERT INTO users (name, email, login, password, role, active) VALUES
('Eduardo Diniz', 'eduardodiniz@graodavida.com.br', 'eduardodiniz',
 '$2a$10$rSBqEnx0LyHpf1M.Tuj5eulbJGMHmkj8.D6xDkBV309zEAvu9Xv7K', 'ADMIN', true),
('Carlinhos JC', 'carlinhosjc@graodavida.com.br', 'carlinhosjc',
 '$2a$10$KgqUamiRP.j4fepXtQ2Chu6AA.OhkC7ASiCIaM.Wx5SKOfPQhIh56', 'ADMIN', true)
ON CONFLICT (login) DO NOTHING;
