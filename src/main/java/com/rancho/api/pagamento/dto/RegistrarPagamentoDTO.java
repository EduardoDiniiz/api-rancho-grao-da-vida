package com.rancho.api.pagamento.dto;

import com.rancho.api.pagamento.FormaPagamento;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RegistrarPagamentoDTO(
        LocalDate dataPagamento,

        @NotNull(message = "Forma de pagamento e obrigatoria")
        FormaPagamento formaPagamento
) {}
