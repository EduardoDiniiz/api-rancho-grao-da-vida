package com.rancho.api.pagamento.dto;

import com.rancho.api.pagamento.FormaPagamento;
import com.rancho.api.pagamento.PagamentoStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PagamentoResponseDTO(
        Long id,
        Long animalServicoId,
        Long animalId,
        String animalNome,
        Long clienteId,
        String clienteNome,
        String servicoNome,
        String descricao,
        BigDecimal valor,
        LocalDate vencimento,
        LocalDate dataPagamento,
        FormaPagamento formaPagamento,
        PagamentoStatus status
) {}
