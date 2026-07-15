package com.rancho.api.animal;

import com.rancho.api.animal.dto.AnimalRequestDTO;
import com.rancho.api.animal.dto.AnimalResponseDTO;
import com.rancho.api.user.Role;
import com.rancho.api.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

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
    public ResponseEntity<AnimalResponseDTO> findById(@PathVariable Long id,
                                                      @AuthenticationPrincipal User user) {
        AnimalResponseDTO animal = animalService.findById(id);
        if (user.getRole() == Role.CLIENTE
                && !Objects.equals(animal.clienteId(), user.getClienteId())) {
            throw new AccessDeniedException("Acesso negado a este animal");
        }
        return ResponseEntity.ok(animal);
    }

    @GetMapping
    public ResponseEntity<Page<AnimalResponseDTO>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AnimalStatus status,
            @RequestParam(required = false) Long clienteId,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        // CLIENTE so enxerga os proprios animais, independente do filtro recebido
        Long escopoCliente = user.getRole() == Role.CLIENTE ? exigirClienteId(user) : clienteId;
        return ResponseEntity.ok(animalService.findAll(search, status, escopoCliente, pageable));
    }

    private Long exigirClienteId(User user) {
        Long clienteId = user.getClienteId();
        if (clienteId == null) {
            throw new AccessDeniedException("Usuario CLIENTE sem cliente vinculado");
        }
        return clienteId;
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
