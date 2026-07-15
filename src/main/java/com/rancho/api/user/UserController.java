package com.rancho.api.user;

import com.rancho.api.user.dto.ChangePasswordDTO;
import com.rancho.api.user.dto.GeneratedCredentialsDTO;
import com.rancho.api.user.dto.ResetPasswordDTO;
import com.rancho.api.user.dto.UserCreateDTO;
import com.rancho.api.user.dto.UserResponseDTO;
import com.rancho.api.user.dto.UserUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody UserCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(dto));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.findById(user.getId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDTO>> findAll(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(search, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO dto) {
        return ResponseEntity.ok(userService.update(id, dto));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordDTO dto) {
        if (!user.getId().equals(id) && user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        userService.changePassword(id, dto.currentPassword(), dto.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordDTO dto) {
        userService.resetPassword(id, dto.newPassword());
        return ResponseEntity.noContent().build();
    }

    /** Gera um usuario (perfil CLIENTE) para o cliente informado e devolve as credenciais. */
    @PostMapping("/from-cliente/{clienteId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GeneratedCredentialsDTO> gerarParaCliente(@PathVariable Long clienteId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.gerarParaCliente(clienteId));
    }

    /** Redefine a senha do usuario do cliente e devolve as novas credenciais. */
    @PostMapping("/from-cliente/{clienteId}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GeneratedCredentialsDTO> redefinirCredenciaisCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(userService.redefinirCredenciaisCliente(clienteId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
