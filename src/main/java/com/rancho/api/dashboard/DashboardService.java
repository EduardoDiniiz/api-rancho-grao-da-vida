package com.rancho.api.dashboard;

import com.rancho.api.baia.BaiaRepository;
import com.rancho.api.baia.BaiaStatus;
import com.rancho.api.cliente.ClienteRepository;
import com.rancho.api.dashboard.dto.DashboardResponseDTO;
import com.rancho.api.hospedagem.HospedagemRepository;
import com.rancho.api.hospedagem.HospedagemStatus;
import com.rancho.api.pagamento.PagamentoRepository;
import com.rancho.api.pagamento.PagamentoStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PagamentoRepository pagamentoRepository;
    private final BaiaRepository baiaRepository;
    private final HospedagemRepository hospedagemRepository;
    private final ClienteRepository clienteRepository;

    @Transactional(readOnly = true)
    public DashboardResponseDTO getResumo() {
        LocalDate hoje = LocalDate.now();
        LocalDate inicioMes = hoje.withDayOfMonth(1);
        LocalDate fimMes = hoje.withDayOfMonth(hoje.lengthOfMonth());

        BigDecimal aReceberPendente = pagamentoRepository.sumByStatus(PagamentoStatus.PENDENTE);
        BigDecimal aReceberAtrasado = pagamentoRepository.sumByStatus(PagamentoStatus.ATRASADO);
        BigDecimal totalAReceber = aReceberPendente.add(aReceberAtrasado);

        BigDecimal recebidoMes = pagamentoRepository.sumPagoNoPeriodo(
                PagamentoStatus.PAGO, inicioMes, fimMes);

        return new DashboardResponseDTO(
                totalAReceber,
                recebidoMes,
                pagamentoRepository.countByStatus(PagamentoStatus.ATRASADO),
                hospedagemRepository.countByStatus(HospedagemStatus.ATIVO),
                baiaRepository.countByStatus(BaiaStatus.OCUPADA),
                baiaRepository.countByStatus(BaiaStatus.LIVRE),
                baiaRepository.countByStatus(BaiaStatus.MANUTENCAO),
                clienteRepository.countByActiveTrue());
    }
}
