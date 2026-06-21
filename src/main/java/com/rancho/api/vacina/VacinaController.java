package com.rancho.api.vacina;

import com.rancho.api.vacina.dto.VacinaRequestDTO;
import com.rancho.api.vacina.dto.VacinaResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/vacinas")
@RequiredArgsConstructor
public class VacinaController {

    private final VacinaService vacinaService;

    @PostMapping
    public ResponseEntity<VacinaResponseDTO> create(@Valid @RequestBody VacinaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vacinaService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VacinaResponseDTO> update(
            @PathVariable Long id, @Valid @RequestBody VacinaRequestDTO dto) {
        return ResponseEntity.ok(vacinaService.update(id, dto));
    }

    @GetMapping("/animal/{animalId}")
    public ResponseEntity<List<VacinaResponseDTO>> findByAnimal(@PathVariable Long animalId) {
        return ResponseEntity.ok(vacinaService.findByAnimal(animalId));
    }

    @GetMapping("/alertas")
    public ResponseEntity<List<VacinaResponseDTO>> findAlertas() {
        return ResponseEntity.ok(vacinaService.findAlertas());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vacinaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
