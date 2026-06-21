package com.rancho.api.hospedagem;

import com.rancho.api.hospedagem.dto.HospedagemRequestDTO;
import com.rancho.api.hospedagem.dto.HospedagemResponseDTO;
import com.rancho.api.hospedagem.dto.RegistrarSaidaDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/hospedagens")
@RequiredArgsConstructor
public class HospedagemController {

    private final HospedagemService hospedagemService;

    @PostMapping
    public ResponseEntity<HospedagemResponseDTO> registrarEntrada(@Valid @RequestBody HospedagemRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hospedagemService.registrarEntrada(dto));
    }

    @PatchMapping("/{id}/saida")
    public ResponseEntity<HospedagemResponseDTO> registrarSaida(
            @PathVariable Long id, @RequestBody(required = false) RegistrarSaidaDTO dto) {
        return ResponseEntity.ok(hospedagemService.registrarSaida(id,
                dto != null ? dto.dataSaida() : null));
    }

    @GetMapping
    public ResponseEntity<Page<HospedagemResponseDTO>> findAll(
            @RequestParam(required = false) HospedagemStatus status,
            @RequestParam(required = false) Long animalId,
            @RequestParam(required = false) Long baiaId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(hospedagemService.findAll(status, animalId, baiaId, pageable));
    }
}
