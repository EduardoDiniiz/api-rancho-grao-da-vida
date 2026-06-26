-- ===========================================================
-- Exames dos animais (ex: hemograma, raio-x). Sem vencimento.
-- ===========================================================

CREATE TABLE exames (
    id            BIGSERIAL    PRIMARY KEY,
    animal_id     BIGINT       NOT NULL REFERENCES animais(id),
    nome          VARCHAR(255) NOT NULL,
    data          DATE         NOT NULL,
    resultado     TEXT,
    veterinario   VARCHAR(255),
    observacao    TEXT,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_exames_animal ON exames(animal_id);
