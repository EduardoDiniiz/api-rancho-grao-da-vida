package com.rancho.api.dispositivo.dto;

import java.util.List;

/**
 * Envelope da listagem de dispositivos. Quando {@code configured} for false,
 * a integracao Tuya ainda nao tem credenciais e o frontend mostra o aviso de configuracao.
 */
public record DispositivosResponseDTO(
        boolean configured,
        List<DispositivoResponseDTO> dispositivos
) {}
