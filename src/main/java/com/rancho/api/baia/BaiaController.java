package com.rancho.api.baia;

import com.rancho.api.baia.dto.BaiaRequestDTO;
import com.rancho.api.baia.dto.BaiaResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/baias")
@RequiredArgsConstructor
public class BaiaController {

    private final BaiaService baiaService;

    @PostMapping
    public ResponseEntity<BaiaResponseDTO> create(@Valid @RequestBody BaiaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(baiaService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaiaResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(baiaService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<BaiaResponseDTO>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BaiaStatus status,
            @PageableDefault(size = 50, sort = "identificacao") Pageable pageable) {
        return ResponseEntity.ok(baiaService.findAll(search, status, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaiaResponseDTO> update(
            @PathVariable Long id, @Valid @RequestBody BaiaRequestDTO dto) {
        return ResponseEntity.ok(baiaService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        baiaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
