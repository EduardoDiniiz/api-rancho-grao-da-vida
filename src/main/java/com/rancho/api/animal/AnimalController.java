package com.rancho.api.animal;

import com.rancho.api.animal.dto.AnimalRequestDTO;
import com.rancho.api.animal.dto.AnimalResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/animais")
@RequiredArgsConstructor
public class AnimalController {

    private final AnimalService animalService;

    @PostMapping
    public ResponseEntity<AnimalResponseDTO> create(@Valid @RequestBody AnimalRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(animalService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnimalResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(animalService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<AnimalResponseDTO>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AnimalStatus status,
            @RequestParam(required = false) Long clienteId,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(animalService.findAll(search, status, clienteId, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnimalResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody AnimalRequestDTO dto) {
        return ResponseEntity.ok(animalService.update(id, dto));
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<AnimalResponseDTO> archive(@PathVariable Long id) {
        return ResponseEntity.ok(animalService.archive(id));
    }

    @PatchMapping("/{id}/unarchive")
    public ResponseEntity<AnimalResponseDTO> unarchive(@PathVariable Long id) {
        return ResponseEntity.ok(animalService.unarchive(id));
    }
}
