-- Metodo escolhido na cobranca online (PIX ou CARTAO). Necessario para saber
-- quando recriar a cobranca no Asaas ao trocar de metodo (o valor muda porque a
-- taxa repassada difere por metodo).
ALTER TABLE pagamentos ADD COLUMN cobranca_metodo VARCHAR(20);
