-- Integracao PIX via Asaas: identificadores externos das cobrancas/clientes
ALTER TABLE clientes   ADD COLUMN asaas_customer_id VARCHAR(40);
ALTER TABLE pagamentos ADD COLUMN asaas_payment_id  VARCHAR(40);
ALTER TABLE pagamentos ADD COLUMN pix_payload        TEXT;

CREATE INDEX idx_pagamentos_asaas ON pagamentos(asaas_payment_id);
