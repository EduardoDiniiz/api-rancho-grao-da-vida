package com.rancho.api.dispositivo;

import com.rancho.api.dispositivo.dto.AgendamentoRequestDTO;
import com.rancho.api.dispositivo.dto.ComandoRequestDTO;
import com.rancho.api.dispositivo.dto.DispositivoResponseDTO;
import com.rancho.api.dispositivo.dto.DispositivosResponseDTO;
import com.rancho.api.dispositivo.dto.RenomearRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/dispositivos")
@RequiredArgsConstructor
public class DispositivoController {

    private final DispositivoService dispositivoService;

    @GetMapping
    public ResponseEntity<DispositivosResponseDTO> listar() {
        return ResponseEntity.ok(dispositivoService.listar());
    }

    @PostMapping("/{id}/comando")
    public ResponseEntity<DispositivoResponseDTO> comando(
            @PathVariable String id, @Valid @RequestBody ComandoRequestDTO dto) {
        return ResponseEntity.ok(dispositivoService.comando(id, dto.ligado()));
    }

    @PutMapping("/{id}/nome")
    public ResponseEntity<Void> renomear(
            @PathVariable String id, @Valid @RequestBody RenomearRequestDTO dto) {
        dispositivoService.renomear(id, dto.nome());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/agendamento")
    public ResponseEntity<Void> agendar(
            @PathVariable String id, @Valid @RequestBody AgendamentoRequestDTO dto) {
        dispositivoService.salvarAgendamento(id, dto.horaLigar(), dto.horaDesligar(), dto.ativo());
        return ResponseEntity.noContent().build();
    }
}
