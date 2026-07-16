package com.rancho.api.pagamento;

import com.rancho.api.cliente.Cliente;
import com.rancho.api.user.Role;
import com.rancho.api.user.User;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Garante que o perfil CLIENTE so enxerga as proprias faturas. */
class PagamentoControllerSecurityTest {

    private final PagamentoService pagamentoService = mock(PagamentoService.class);
    private final PagamentoController controller = new PagamentoController(pagamentoService);

    private static final long DONO = 1L;
    private static final long OUTRO = 2L;

    private User cliente(long clienteId) {
        return User.builder().role(Role.CLIENTE)
                .cliente(Cliente.builder().id(clienteId).build()).build();
    }

    @Test
    void clienteListaIgnoraFiltroEForcaOProprioEscopo() {
        when(pagamentoService.findAll(any(), any(), any(), any(), any(), any())).thenReturn(Page.empty());

        // CLIENTE dono tenta filtrar pelas faturas de OUTRO cliente
        controller.findAll(null, null, OUTRO, null, null, cliente(DONO), null);

        ArgumentCaptor<Long> escopo = ArgumentCaptor.forClass(Long.class);
        verify(pagamentoService).findAll(any(), any(), escopo.capture(), any(), any(), any());
        assertEquals(DONO, escopo.getValue(), "CLIENTE deve ser forcado ao proprio clienteId");
    }

    @Test
    void adminPodeFiltrarPorQualquerCliente() {
        when(pagamentoService.findAll(any(), any(), any(), any(), any(), any())).thenReturn(Page.empty());

        controller.findAll(null, null, OUTRO, null, null, User.builder().role(Role.ADMIN).build(), null);

        verify(pagamentoService).findAll(any(), eq(null), eq(OUTRO), any(), any(), any());
    }
}
