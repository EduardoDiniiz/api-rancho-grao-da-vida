package com.rancho.api.pagamento;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PagamentoScheduler {

    private final PagamentoService pagamentoService;

    /**
     * Marca diariamente (06:00) as cobrancas pendentes vencidas como ATRASADO.
     */
    @Scheduled(cron = "0 0 6 * * *")
    public void marcarAtrasados() {
        int total = pagamentoService.atualizarAtrasados();
        if (total > 0) {
            log.info("{} cobranca(s) marcada(s) como ATRASADO.", total);
        }
    }
}
