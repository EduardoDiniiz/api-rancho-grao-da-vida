-- ===========================================================
-- Sistema de Gestao de Baias para Cavalos - schema inicial
-- ===========================================================

-- RF001 / RF002 - Usuarios
CREATE TABLE users (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(255)    NOT NULL,
    email       VARCHAR(255)    NOT NULL UNIQUE,
    login       VARCHAR(50)     NOT NULL UNIQUE,
    password    VARCHAR(255)    NOT NULL,
    role        VARCHAR(20)     NOT NULL DEFAULT 'OPERADOR',
    active      BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_users_login ON users(login);

-- RF003 - Clientes
CREATE TABLE clientes (
    id          BIGSERIAL       PRIMARY KEY,
    nome        VARCHAR(255)    NOT NULL,
    cpf_cnpj    VARCHAR(20),
    telefone    VARCHAR(20),
    email       VARCHAR(255),
    endereco    TEXT,
    observacoes TEXT,
    active      BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_clientes_nome ON clientes(nome);

-- RF004 - Animais
CREATE TABLE animais (
    id              BIGSERIAL       PRIMARY KEY,
    cliente_id      BIGINT          NOT NULL REFERENCES clientes(id),
    nome            VARCHAR(255)    NOT NULL,
    data_nascimento DATE,
    sexo            VARCHAR(10),
    esporte         VARCHAR(20),
    registro        VARCHAR(100),
    enfermidades    TEXT,
    observacoes     TEXT,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ATIVO',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_animais_cliente ON animais(cliente_id);
CREATE INDEX idx_animais_status ON animais(status);

-- RF005 - Vacinas
CREATE TABLE vacinas (
    id              BIGSERIAL       PRIMARY KEY,
    animal_id       BIGINT          NOT NULL REFERENCES animais(id),
    nome            VARCHAR(255)    NOT NULL,
    data_aplicacao  DATE            NOT NULL,
    data_vencimento DATE,
    observacao      TEXT,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_vacinas_animal ON vacinas(animal_id);
CREATE INDEX idx_vacinas_vencimento ON vacinas(data_vencimento);

-- RF006 - Baias
CREATE TABLE baias (
    id            BIGSERIAL     PRIMARY KEY,
    identificacao VARCHAR(50)   NOT NULL UNIQUE,
    localizacao   VARCHAR(255),
    capacidade    INTEGER       NOT NULL DEFAULT 1,
    status        VARCHAR(20)   NOT NULL DEFAULT 'LIVRE',
    observacao    TEXT,
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- RF007 - Hospedagens
CREATE TABLE hospedagens (
    id           BIGSERIAL    PRIMARY KEY,
    animal_id    BIGINT       NOT NULL REFERENCES animais(id),
    cliente_id   BIGINT       NOT NULL REFERENCES clientes(id),
    baia_id      BIGINT       NOT NULL REFERENCES baias(id),
    data_entrada DATE         NOT NULL,
    data_saida   DATE,
    status       VARCHAR(20)  NOT NULL DEFAULT 'ATIVO',
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_hospedagens_baia ON hospedagens(baia_id);
CREATE INDEX idx_hospedagens_animal ON hospedagens(animal_id);
CREATE INDEX idx_hospedagens_status ON hospedagens(status);

-- RF008 - Servicos
CREATE TABLE servicos (
    id           BIGSERIAL      PRIMARY KEY,
    nome         VARCHAR(255)   NOT NULL,
    descricao    TEXT,
    valor_padrao NUMERIC(12,2)  NOT NULL,
    active       BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- RF009 - Servicos contratados por animal
CREATE TABLE animal_servicos (
    id                 BIGSERIAL      PRIMARY KEY,
    animal_id          BIGINT         NOT NULL REFERENCES animais(id),
    servico_id         BIGINT         NOT NULL REFERENCES servicos(id),
    valor              NUMERIC(12,2)  NOT NULL,
    data_inicio        DATE           NOT NULL,
    proximo_vencimento DATE,
    recorrencia_dias   INTEGER        NOT NULL,
    descricao          TEXT,
    status             VARCHAR(20)    NOT NULL DEFAULT 'ATIVO',
    created_at         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_animal_servicos_animal ON animal_servicos(animal_id);

-- RF010 / RF011 - Pagamentos / Cobrancas
CREATE TABLE pagamentos (
    id                BIGSERIAL      PRIMARY KEY,
    animal_servico_id BIGINT         REFERENCES animal_servicos(id),
    animal_id         BIGINT         REFERENCES animais(id),
    valor             NUMERIC(12,2)  NOT NULL,
    descricao         VARCHAR(255),
    vencimento        DATE           NOT NULL,
    data_pagamento    DATE,
    forma_pagamento   VARCHAR(30),
    status            VARCHAR(20)    NOT NULL DEFAULT 'PENDENTE',
    created_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_pagamentos_status ON pagamentos(status);
CREATE INDEX idx_pagamentos_vencimento ON pagamentos(vencimento);
CREATE INDEX idx_pagamentos_animal_servico ON pagamentos(animal_servico_id);
