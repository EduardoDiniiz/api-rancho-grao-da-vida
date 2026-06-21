package com.rancho.api.servico;

import com.rancho.api.servico.dto.ServicoRequestDTO;
import com.rancho.api.servico.dto.ServicoResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/servicos")
@RequiredArgsConstructor
public class ServicoController {

    private final ServicoService servicoService;

    @PostMapping
    public ResponseEntity<ServicoResponseDTO> create(@Valid @RequestBody ServicoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicoService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicoResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(servicoService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<ServicoResponseDTO>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean apenasAtivos,
            @PageableDefault(size = 50, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(servicoService.findAll(search, apenasAtivos, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicoResponseDTO> update(
            @PathVariable Long id, @Valid @RequestBody ServicoRequestDTO dto) {
        return ResponseEntity.ok(servicoService.update(id, dto));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<ServicoResponseDTO> toggleActive(
            @PathVariable Long id, @RequestParam boolean active) {
        return ResponseEntity.ok(servicoService.toggleActive(id, active));
    }
}
