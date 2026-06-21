-- ============================================================
-- Limpa TODA a base e deixa apenas 2 usuarios administradores.
-- ATENCAO: apaga todos os dados (clientes, animais, baias,
-- hospedagens, servicos, cobrancas, etc). Acao irreversivel.
--
-- Usuarios resultantes (login / senha):
--   eduardodiniz / Gestao01%
--   carlinhosjc  / senha0123%
-- (hashes BCrypt gerados e verificados com a lib do projeto)
-- ============================================================

BEGIN;

-- Zera todas as tabelas de dados (mantem flyway_schema_history).
TRUNCATE TABLE
    pagamentos,
    animal_servicos,
    hospedagens,
    vacinas,
    animais,
    servicos,
    baias,
    clientes,
    users
RESTART IDENTITY CASCADE;

-- Recria os 2 usuarios administradores.
INSERT INTO users (name, email, login, password, role, active) VALUES
('Eduardo Diniz', 'eduardodiniz@graodavida.com.br', 'eduardodiniz',
 '$2a$10$rSBqEnx0LyHpf1M.Tuj5eulbJGMHmkj8.D6xDkBV309zEAvu9Xv7K', 'ADMIN', true),
('Carlinhos JC', 'carlinhosjc@graodavida.com.br', 'carlinhosjc',
 '$2a$10$KgqUamiRP.j4fepXtQ2Chu6AA.OhkC7ASiCIaM.Wx5SKOfPQhIh56', 'ADMIN', true);

COMMIT;
