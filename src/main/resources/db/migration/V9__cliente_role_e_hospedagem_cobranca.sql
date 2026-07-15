-- Perfil CLIENTE: vincula um usuario a um cliente (proprietario dos animais)
ALTER TABLE users ADD COLUMN cliente_id BIGINT REFERENCES clientes(id);
CREATE INDEX idx_users_cliente ON users(cliente_id);

-- Cobranca mensal da hospedagem
ALTER TABLE hospedagens ADD COLUMN valor_mensal        NUMERIC(12,2);
ALTER TABLE hospedagens ADD COLUMN proximo_vencimento  DATE;

-- Vincula o pagamento a hospedagem que o originou (recorrencia mensal)
ALTER TABLE pagamentos ADD COLUMN hospedagem_id BIGINT REFERENCES hospedagens(id);
CREATE INDEX idx_pagamentos_hospedagem ON pagamentos(hospedagem_id);
