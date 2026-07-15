package com.rancho.api.fluxo;

import com.rancho.api.fluxo.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/fluxos")
@RequiredArgsConstructor
public class FluxoController {

    private final FluxoService fluxoService;

    @GetMapping
    public ResponseEntity<List<FluxoResumoDTO>> listar(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(fluxoService.listar(search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FluxoGrafoResponseDTO> buscarGrafo(@PathVariable Long id) {
        return ResponseEntity.ok(fluxoService.buscarGrafo(id));
    }

    @PostMapping
    public ResponseEntity<FluxoResumoDTO> criar(@Valid @RequestBody FluxoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fluxoService.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FluxoResumoDTO> atualizarMeta(
            @PathVariable Long id, @Valid @RequestBody FluxoRequestDTO dto) {
        return ResponseEntity.ok(fluxoService.atualizarMeta(id, dto));
    }

    @PutMapping("/{id}/grafo")
    public ResponseEntity<FluxoGrafoResponseDTO> salvarGrafo(
            @PathVariable Long id, @Valid @RequestBody GrafoRequestDTO dto) {
        return ResponseEntity.ok(fluxoService.salvarGrafo(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        fluxoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
