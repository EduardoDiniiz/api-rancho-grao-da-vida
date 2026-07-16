package com.rancho.api.pix.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Dados do cartao e do titular para o checkout transparente do Asaas. O titular
 * pode ser diferente do cliente (pagamento com cartao de terceiro), por isso
 * CPF/telefone sao informados aqui e nao reaproveitados do cadastro.
 *
 * ATENCAO: numero e ccv trafegam apenas de passagem para o Asaas; nunca sao
 * persistidos nem logados.
 */
public record CartaoRequest(
        @NotBlank String number,
        @NotBlank String holderName,
        @NotBlank String expiryMonth,
        @NotBlank String expiryYear,
        @NotBlank String ccv,
        @NotBlank String cpfCnpj,
        @NotBlank String phone,
        String email,
        @NotBlank String postalCode,
        @NotBlank String addressNumber
) {}
