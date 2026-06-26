package com.rancho.api.dispositivo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuracao da integracao com a Tuya Cloud (controle dos interruptores WiFi).
 * As chaves ficam apenas no servidor, via variaveis de ambiente.
 */
@Component
@ConfigurationProperties(prefix = "tuya")
@Getter
@Setter
public class TuyaProperties {

    /** Liga/desliga a integracao. Enquanto false, o menu Dispositivos mostra apenas o aviso de configuracao. */
    private boolean enabled = false;

    /** Endpoint da regiao do Data Center Tuya. Brasil/Smart Life normalmente usa openapi.tuyaus.com. */
    private String endpoint = "https://openapi.tuyaus.com";

    /** Access ID (Client ID) do Cloud Project no iot.tuya.com. */
    private String accessId;

    /** Access Secret (Client Secret) do Cloud Project no iot.tuya.com. */
    private String accessSecret;
}
