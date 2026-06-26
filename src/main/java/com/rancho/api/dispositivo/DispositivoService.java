package com.rancho.api.dispositivo;

import com.fasterxml.jackson.databind.JsonNode;
import com.rancho.api.common.exception.BusinessException;
import com.rancho.api.dispositivo.dto.DispositivoResponseDTO;
import com.rancho.api.dispositivo.dto.DispositivosResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DispositivoService {

    /** Fuso do haras, usado para disparar os agendamentos. */
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");

    private final TuyaClient tuya;
    private final DispositivoApelidoRepository apelidoRepository;
    private final DispositivoAgendamentoRepository agendamentoRepository;

    /** Lista os interruptores associados ao Cloud Project (contas Smart Life vinculadas). */
    @Transactional(readOnly = true)
    public DispositivosResponseDTO listar() {
        if (!tuya.isConfigured()) {
            return new DispositivosResponseDTO(false, List.of());
        }
        JsonNode result = tuya.get("/v1.0/iot-01/associated-users/devices?size=100");
        JsonNode devices = result.path("devices");

        List<DispositivoResponseDTO> base = new ArrayList<>();
        if (devices.isArray()) {
            for (JsonNode d : devices) {
                base.add(toDTO(d));
            }
        }

        List<String> ids = base.stream().map(DispositivoResponseDTO::id).toList();
        Map<String, String> apelidos = apelidoRepository.findByDeviceIdIn(ids).stream()
                .collect(Collectors.toMap(DispositivoApelido::getDeviceId, DispositivoApelido::getApelido));
        Map<String, DispositivoAgendamento> agendamentos = agendamentoRepository.findByDeviceIdIn(ids).stream()
                .collect(Collectors.toMap(DispositivoAgendamento::getDeviceId, Function.identity()));

        List<DispositivoResponseDTO> lista = base.stream().map(d -> {
            String apelido = apelidos.get(d.id());
            DispositivoAgendamento ag = agendamentos.get(d.id());
            return new DispositivoResponseDTO(
                    d.id(),
                    apelido != null ? apelido : d.nome(),
                    d.categoria(), d.online(), d.ligado(), d.switchCode(),
                    ag != null ? ag.getHoraLigar() : null,
                    ag != null ? ag.getHoraDesligar() : null,
                    ag != null ? ag.isAtivo() : null);
        }).toList();

        return new DispositivosResponseDTO(true, lista);
    }

    /** Define/atualiza o apelido local do dispositivo. Apelido em branco remove o apelido (volta ao nome da Tuya). */
    @Transactional
    public void renomear(String deviceId, String apelido) {
        if (!StringUtils.hasText(apelido)) {
            apelidoRepository.deleteById(deviceId);
            return;
        }
        DispositivoApelido entity = apelidoRepository.findById(deviceId)
                .orElseGet(() -> DispositivoApelido.builder().deviceId(deviceId).build());
        entity.setApelido(apelido.trim());
        apelidoRepository.save(entity);
    }

    /** Configura o agendamento diario. Sem nenhum horario, remove o agendamento. */
    @Transactional
    public void salvarAgendamento(String deviceId, LocalTime horaLigar, LocalTime horaDesligar, Boolean ativo) {
        if (horaLigar == null && horaDesligar == null) {
            agendamentoRepository.deleteById(deviceId);
            return;
        }
        DispositivoAgendamento ag = agendamentoRepository.findById(deviceId)
                .orElseGet(() -> DispositivoAgendamento.builder().deviceId(deviceId).build());
        ag.setHoraLigar(horaLigar);
        ag.setHoraDesligar(horaDesligar);
        ag.setAtivo(ativo == null || ativo);
        agendamentoRepository.save(ag);
    }

    /** Liga (true) ou desliga (false) um dispositivo e devolve o estado resultante. */
    public DispositivoResponseDTO comando(String deviceId, boolean ligar) {
        String switchCode = detectarSwitchCode(deviceId);
        if (switchCode == null) {
            throw new BusinessException("Nao foi possivel identificar o interruptor deste dispositivo.");
        }
        Map<String, Object> body = Map.of(
                "commands", List.of(Map.of("code", switchCode, "value", ligar)));
        JsonNode result = tuya.post("/v1.0/devices/" + deviceId + "/commands", body);
        if (!result.asBoolean(false)) {
            throw new BusinessException("A Tuya recusou o comando para este dispositivo.");
        }
        return new DispositivoResponseDTO(deviceId, null, null, true, ligar, switchCode, null, null, null);
    }

    /** Roda a cada minuto e dispara os agendamentos cujo horario bate com o minuto atual. */
    @Scheduled(cron = "0 * * * * *", zone = "America/Sao_Paulo")
    public void executarAgendamentos() {
        if (!tuya.isConfigured()) {
            return;
        }
        LocalTime agora = LocalTime.now(ZONA).truncatedTo(ChronoUnit.MINUTES);
        for (DispositivoAgendamento ag : agendamentoRepository.findByAtivoTrue()) {
            try {
                if (igualAoMinuto(ag.getHoraLigar(), agora)) {
                    log.info("Agendamento: ligando device {}", ag.getDeviceId());
                    comando(ag.getDeviceId(), true);
                } else if (igualAoMinuto(ag.getHoraDesligar(), agora)) {
                    log.info("Agendamento: desligando device {}", ag.getDeviceId());
                    comando(ag.getDeviceId(), false);
                }
            } catch (Exception e) {
                log.warn("Falha no agendamento do device {}: {}", ag.getDeviceId(), e.getMessage());
            }
        }
    }

    // ---- helpers ----

    private boolean igualAoMinuto(LocalTime alvo, LocalTime agora) {
        return alvo != null
                && alvo.getHour() == agora.getHour()
                && alvo.getMinute() == agora.getMinute();
    }

    private DispositivoResponseDTO toDTO(JsonNode d) {
        String id = d.path("id").asText();
        String nome = d.path("name").asText("");
        String categoria = d.path("category").asText(null);
        Boolean online = d.has("online") ? d.path("online").asBoolean() : null;

        // status pode vir embutido na listagem; senao busca sob demanda
        JsonNode status = d.path("status");
        if (!status.isArray() || status.isEmpty()) {
            status = buscarStatus(id);
        }
        String switchCode = encontrarSwitchCode(status);
        Boolean ligado = switchCode == null ? null : valorBoolean(status, switchCode);

        return new DispositivoResponseDTO(id, nome, categoria, online, ligado, switchCode, null, null, null);
    }

    private JsonNode buscarStatus(String deviceId) {
        try {
            return tuya.get("/v1.0/devices/" + deviceId + "/status");
        } catch (Exception e) {
            log.warn("Falha ao buscar status do device {}: {}", deviceId, e.getMessage());
            return null;
        }
    }

    private String detectarSwitchCode(String deviceId) {
        return encontrarSwitchCode(buscarStatus(deviceId));
    }

    /** Procura o data point de liga/desliga: prefere switch_1, depois switch, depois qualquer switch*. */
    private String encontrarSwitchCode(JsonNode status) {
        if (status == null || !status.isArray()) {
            return null;
        }
        String fallback = null;
        for (JsonNode s : status) {
            String code = s.path("code").asText("");
            if (!s.path("value").isBoolean()) {
                continue;
            }
            if ("switch_1".equals(code) || "switch".equals(code)) {
                return code;
            }
            if (fallback == null && code.startsWith("switch")) {
                fallback = code;
            }
        }
        return fallback;
    }

    private Boolean valorBoolean(JsonNode status, String code) {
        if (status == null || !status.isArray()) {
            return null;
        }
        for (JsonNode s : status) {
            if (code.equals(s.path("code").asText())) {
                return s.path("value").asBoolean();
            }
        }
        return null;
    }
}
