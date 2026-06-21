package com.rancho.api.animalservico;

import com.rancho.api.animalservico.dto.AnimalServicoRequestDTO;
import com.rancho.api.animalservico.dto.AnimalServicoResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/animal-servicos")
@RequiredArgsConstructor
public class AnimalServicoController {

    private final AnimalServicoService animalServicoService;

    @PostMapping
    public ResponseEntity<AnimalServicoResponseDTO> create(@Valid @RequestBody AnimalServicoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(animalServicoService.create(dto));
    }

    @GetMapping("/animal/{animalId}")
    public ResponseEntity<List<AnimalServicoResponseDTO>> findByAnimal(@PathVariable Long animalId) {
        return ResponseEntity.ok(animalServicoService.findByAnimal(animalId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnimalServicoResponseDTO> update(
            @PathVariable Long id, @Valid @RequestBody AnimalServicoRequestDTO dto) {
        return ResponseEntity.ok(animalServicoService.update(id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AnimalServicoResponseDTO> alterarStatus(
            @PathVariable Long id, @RequestParam AnimalServicoStatus status) {
        return ResponseEntity.ok(animalServicoService.alterarStatus(id, status));
    }
}
