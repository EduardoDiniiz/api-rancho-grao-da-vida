-- Seed de produção: usuário administrador inicial.
-- Esta migration NÃO roda no perfil "local" (que usa o DataSeeder); ela só é
-- carregada quando o perfil "prod" adiciona a location classpath:db/seed-prod
-- (ver application-prod.yml).
--
-- Senha inicial: Teste@123  (hash BCrypt)  -->  TROQUE após o primeiro login.
INSERT INTO users (name, email, login, password, role, active)
VALUES ('Administrador',
        'admin@rancho.com.br',
        'admin',
        '$2y$10$Nmx7HXzzqCisKCy0cHWgYOWDdr2o11ZHq7bly3713s7Mkvk5xV5mC',
        'ADMIN',
        true)
ON CONFLICT (login) DO NOTHING;
