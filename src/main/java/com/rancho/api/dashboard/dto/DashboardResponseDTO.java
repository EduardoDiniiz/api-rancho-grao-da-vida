package com.rancho.api.dashboard.dto;

import java.math.BigDecimal;

public record DashboardResponseDTO(
        BigDecimal totalAReceber,
        BigDecimal totalRecebidoMes,
        long cobrancasVencidas,
        long animaisHospedados,
        long baiasOcupadas,
        long baiasLivres,
        long baiasManutencao,
        long totalClientes
) {}
