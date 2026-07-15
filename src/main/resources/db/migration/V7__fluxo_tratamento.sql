-- ===========================================================
-- Fluxos de tratamento (protocolos) modelados como grafo.
-- Um fluxo tem pontos (nos) tipados e arestas (ligacoes, com condicao
-- opcional para ramificacoes). A execucao por animal vem em migration futura.
-- ===========================================================

CREATE TABLE fluxos (
    id          BIGSERIAL    PRIMARY KEY,
    nome        VARCHAR(255) NOT NULL,
    descricao   TEXT,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE fluxo_pontos (
    id          BIGSERIAL        PRIMARY KEY,
    fluxo_id    BIGINT           NOT NULL REFERENCES fluxos(id) ON DELETE CASCADE,
    node_key    VARCHAR(50)      NOT NULL,   -- id do no usado pelo editor (React Flow)
    tipo        VARCHAR(20)      NOT NULL,   -- MEDICAMENTO, ALIMENTACAO, EXAME, VACINA
    titulo      VARCHAR(255)     NOT NULL,
    dia         INTEGER,                     -- offset em dias a partir do inicio
    produto     VARCHAR(255),                -- ex: Terramicina
    dose        VARCHAR(100),                -- ex: 5 ml
    descricao   TEXT,
    inicial     BOOLEAN          NOT NULL DEFAULT FALSE,  -- ponto de partida
    pos_x       DOUBLE PRECISION NOT NULL DEFAULT 0,
    pos_y       DOUBLE PRECISION NOT NULL DEFAULT 0
);
CREATE INDEX idx_fluxo_pontos_fluxo ON fluxo_pontos(fluxo_id);

CREATE TABLE fluxo_arestas (
    id          BIGSERIAL    PRIMARY KEY,
    fluxo_id    BIGINT       NOT NULL REFERENCES fluxos(id) ON DELETE CASCADE,
    origem_id   BIGINT       NOT NULL REFERENCES fluxo_pontos(id) ON DELETE CASCADE,
    destino_id  BIGINT       NOT NULL REFERENCES fluxo_pontos(id) ON DELETE CASCADE,
    condicao    VARCHAR(255)             -- rotulo da ramificacao (ex: "resultado positivo")
);
CREATE INDEX idx_fluxo_arestas_fluxo ON fluxo_arestas(fluxo_id);
