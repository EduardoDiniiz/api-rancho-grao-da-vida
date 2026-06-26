-- ===========================================================
-- Agendamento diario de liga/desliga dos dispositivos Tuya.
-- Uma linha por device: hora de ligar e/ou desligar (todos os dias).
-- O disparo e feito pelo scheduler do backend (fuso America/Sao_Paulo).
-- ===========================================================

CREATE TABLE dispositivo_agendamentos (
    device_id       VARCHAR(64)     PRIMARY KEY,
    hora_ligar      TIME,
    hora_desligar   TIME,
    ativo           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);
