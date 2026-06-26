package com.rancho.api.exame;

import com.rancho.api.exame.dto.ExameRequestDTO;
import com.rancho.api.exame.dto.ExameResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/exames")
@RequiredArgsConstructor
public class ExameController {

    private final ExameService exameService;

    @PostMapping
    public ResponseEntity<ExameResponseDTO> create(@Valid @RequestBody ExameRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(exameService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExameResponseDTO> update(
            @PathVariable Long id, @Valid @RequestBody ExameRequestDTO dto) {
        return ResponseEntity.ok(exameService.update(id, dto));
    }

    @GetMapping("/animal/{animalId}")
    public ResponseEntity<List<ExameResponseDTO>> findByAnimal(@PathVariable Long animalId) {
        return ResponseEntity.ok(exameService.findByAnimal(animalId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        exameService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
