-- ===========================================================
-- Execucao de um fluxo de tratamento para um animal.
-- Ao iniciar, os pontos/arestas do fluxo sao "fotografados" (snapshot)
-- aqui, com a data prevista = data_inicio + dia. Assim editar o template
-- depois nao afeta execucoes em andamento.
-- ===========================================================

CREATE TABLE fluxo_execucoes (
    id          BIGSERIAL    PRIMARY KEY,
    animal_id   BIGINT       NOT NULL REFERENCES animais(id),
    fluxo_id    BIGINT       REFERENCES fluxos(id) ON DELETE SET NULL,
    fluxo_nome  VARCHAR(255) NOT NULL,
    data_inicio DATE         NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'EM_ANDAMENTO',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_fluxo_exec_animal ON fluxo_execucoes(animal_id);

CREATE TABLE fluxo_execucao_pontos (
    id              BIGSERIAL    PRIMARY KEY,
    execucao_id     BIGINT       NOT NULL REFERENCES fluxo_execucoes(id) ON DELETE CASCADE,
    node_key        VARCHAR(50)  NOT NULL,
    tipo            VARCHAR(20)  NOT NULL,
    titulo          VARCHAR(255) NOT NULL,
    dia             INTEGER,
    produto         VARCHAR(255),
    dose            VARCHAR(100),
    descricao       TEXT,
    data_prevista   DATE,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDENTE',  -- PENDENTE, CONCLUIDO, PULADO
    data_conclusao  DATE,
    observacao      TEXT
);
CREATE INDEX idx_fluxo_exec_pontos_exec ON fluxo_execucao_pontos(execucao_id);

CREATE TABLE fluxo_execucao_arestas (
    id           BIGSERIAL   PRIMARY KEY,
    execucao_id  BIGINT      NOT NULL REFERENCES fluxo_execucoes(id) ON DELETE CASCADE,
    origem_key   VARCHAR(50) NOT NULL,
    destino_key  VARCHAR(50) NOT NULL,
    condicao     VARCHAR(255)
);
CREATE INDEX idx_fluxo_exec_arestas_exec ON fluxo_execucao_arestas(execucao_id);
