package com.rancho.api.fluxo.execucao;

import com.rancho.api.fluxo.execucao.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/fluxo-execucoes")
@RequiredArgsConstructor
public class FluxoExecucaoController {

    private final FluxoExecucaoService service;

    @PostMapping
    public ResponseEntity<ExecucaoResumoDTO> iniciar(@Valid @RequestBody IniciarExecucaoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.iniciar(dto));
    }

    @GetMapping("/animal/{animalId}")
    public ResponseEntity<List<ExecucaoResumoDTO>> listarPorAnimal(@PathVariable Long animalId) {
        return ResponseEntity.ok(service.listarPorAnimal(animalId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExecucaoDetalheDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PatchMapping("/pontos/{pontoId}/concluir")
    public ResponseEntity<ExecPontoDTO> concluir(
            @PathVariable Long pontoId, @RequestBody(required = false) ConcluirPontoDTO dto) {
        return ResponseEntity.ok(service.concluirPonto(pontoId, dto != null ? dto.observacao() : null));
    }

    @PatchMapping("/pontos/{pontoId}/pular")
    public ResponseEntity<ExecPontoDTO> pular(@PathVariable Long pontoId) {
        return ResponseEntity.ok(service.pularPonto(pontoId));
    }

    @PatchMapping("/pontos/{pontoId}/reabrir")
    public ResponseEntity<ExecPontoDTO> reabrir(@PathVariable Long pontoId) {
        return ResponseEntity.ok(service.reabrirPonto(pontoId));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        service.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}
