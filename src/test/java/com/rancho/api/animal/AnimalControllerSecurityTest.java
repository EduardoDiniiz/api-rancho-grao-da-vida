package com.rancho.api.animal;

import com.rancho.api.animal.dto.AnimalResponseDTO;
import com.rancho.api.cliente.Cliente;
import com.rancho.api.user.Role;
import com.rancho.api.user.User;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Garante que o perfil CLIENTE so enxerga os proprios animais. */
class AnimalControllerSecurityTest {

    private final AnimalService animalService = mock(AnimalService.class);
    private final AnimalController controller = new AnimalController(animalService);

    private static final long DONO = 1L;
    private static final long OUTRO = 2L;

    private User cliente(long clienteId) {
        return User.builder().role(Role.CLIENTE)
                .cliente(Cliente.builder().id(clienteId).build()).build();
    }

    private AnimalResponseDTO animalDoCliente(long clienteId) {
        return new AnimalResponseDTO(99L, clienteId, "Cli", "Rex",
                null, null, null, null, null, null, null);
    }

    @Test
    void clienteNaoVeAnimalDeOutro() {
        when(animalService.findById(99L)).thenReturn(animalDoCliente(DONO));

        assertThrows(AccessDeniedException.class,
                () -> controller.findById(99L, cliente(OUTRO)));
    }

    @Test
    void clienteListaIgnoraFiltroEForcaOProprioEscopo() {
        when(animalService.findAll(any(), any(), any(), any())).thenReturn(Page.empty());

        // CLIENTE dono tenta filtrar pelos animais de OUTRO cliente
        controller.findAll(null, null, OUTRO, cliente(DONO), null);

        ArgumentCaptor<Long> escopo = ArgumentCaptor.forClass(Long.class);
        verify(animalService).findAll(any(), any(), escopo.capture(), any());
        assertEquals(DONO, escopo.getValue(), "CLIENTE deve ser forcado ao proprio clienteId");
    }

    @Test
    void adminPodeFiltrarPorQualquerCliente() {
        when(animalService.findAll(any(), any(), any(), any())).thenReturn(Page.empty());

        controller.findAll(null, null, OUTRO, User.builder().role(Role.ADMIN).build(), null);

        verify(animalService).findAll(any(), any(), eq(OUTRO), any());
    }
}
