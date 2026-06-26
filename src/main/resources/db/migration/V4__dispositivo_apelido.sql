-- ===========================================================
-- Apelidos locais para dispositivos Tuya (menu Dispositivos)
-- Mapeia o id do device na Tuya -> nome customizado no nosso app.
-- ===========================================================

CREATE TABLE dispositivo_apelidos (
    device_id   VARCHAR(64)     PRIMARY KEY,
    apelido     VARCHAR(255)    NOT NULL,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);
