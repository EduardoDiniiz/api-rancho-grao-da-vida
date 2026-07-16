package com.rancho.api.pix;

import com.rancho.api.animal.Animal;
import com.rancho.api.cliente.Cliente;
import com.rancho.api.pagamento.Pagamento;
import com.rancho.api.pagamento.PagamentoRepository;
import com.rancho.api.pagamento.PagamentoService;
import com.rancho.api.pagamento.PagamentoStatus;
import com.rancho.api.user.Role;
import com.rancho.api.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Garante o isolamento entre clientes nas cobrancas: um CLIENTE nunca acessa
 * (consultar ou pagar) uma cobranca que nao seja sua. Regressao de seguranca.
 */
class PixServiceSecurityTest {

    private final PagamentoRepository pagamentoRepository = mock(PagamentoRepository.class);
    private final AsaasClient asaasClient = mock(AsaasClient.class);

    private final PixService service = new PixService(
            pagamentoRepository,
            mock(com.rancho.api.cliente.ClienteRepository.class),
            mock(PagamentoService.class),
            asaasClient,
            new AsaasProperties());

    private static final long PAG_ID = 10L;
    private static final long DONO = 1L;   // cliente dono da cobranca
    private static final long OUTRO = 2L;   // outro cliente

    private Pagamento cobrancaDoCliente(long clienteId) {
        return Pagamento.builder()
                .id(PAG_ID)
                .valor(BigDecimal.TEN)
                .status(PagamentoStatus.PENDENTE)
                .animal(Animal.builder().cliente(Cliente.builder().id(clienteId).build()).build())
                .build();
    }

    private User cliente(long clienteId) {
        return User.builder().role(Role.CLIENTE)
                .cliente(Cliente.builder().id(clienteId).build()).build();
    }

    @Test
    void clienteNaoConsultaStatusDeCobrancaDeOutro() {
        when(pagamentoRepository.findById(PAG_ID)).thenReturn(Optional.of(cobrancaDoCliente(DONO)));

        assertThrows(AccessDeniedException.class,
                () -> service.consultarStatus(PAG_ID, cliente(OUTRO)));
    }

    @Test
    void clienteConsultaStatusDaPropriaCobranca() {
        when(pagamentoRepository.findById(PAG_ID)).thenReturn(Optional.of(cobrancaDoCliente(DONO)));

        assertDoesNotThrow(() -> service.consultarStatus(PAG_ID, cliente(DONO)));
    }

    @Test
    void adminConsultaStatusDeQualquerCobranca() {
        when(pagamentoRepository.findById(PAG_ID)).thenReturn(Optional.of(cobrancaDoCliente(DONO)));

        assertDoesNotThrow(() -> service.consultarStatus(PAG_ID, User.builder().role(Role.ADMIN).build()));
    }

    @Test
    void clienteNaoGeraPixDeCobrancaDeOutro() {
        when(pagamentoRepository.findById(PAG_ID)).thenReturn(Optional.of(cobrancaDoCliente(DONO)));

        assertThrows(AccessDeniedException.class,
                () -> service.gerarPix(PAG_ID, cliente(OUTRO)));
    }

    @Test
    void clienteNaoPagaComCartaoCobrancaDeOutro() {
        when(pagamentoRepository.findById(PAG_ID)).thenReturn(Optional.of(cobrancaDoCliente(DONO)));

        assertThrows(AccessDeniedException.class,
                () -> service.gerarCartao(PAG_ID, null, "1.2.3.4", cliente(OUTRO)));
    }
}
