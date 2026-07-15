package com.rancho.api.pagamento;

import com.rancho.api.pagamento.dto.CobrancaAvulsaDTO;
import com.rancho.api.pagamento.dto.PagamentoResponseDTO;
import com.rancho.api.pagamento.dto.RegistrarPagamentoDTO;
import com.rancho.api.user.Role;
import com.rancho.api.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/v1/pagamentos")
@RequiredArgsConstructor
public class PagamentoController {

    private final PagamentoService pagamentoService;

    @GetMapping
    public ResponseEntity<Page<PagamentoResponseDTO>> findAll(
            @RequestParam(required = false) PagamentoStatus status,
            @RequestParam(required = false) Long animalId,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20) Pageable pageable) {
        // CLIENTE so enxerga faturas dos proprios animais
        Long escopoCliente = user.getRole() == Role.CLIENTE ? exigirClienteId(user) : clienteId;
        return ResponseEntity.ok(pagamentoService.findAll(status, animalId, escopoCliente, inicio, fim, pageable));
    }

    private Long exigirClienteId(User user) {
        Long clienteId = user.getClienteId();
        if (clienteId == null) {
            throw new AccessDeniedException("Usuario CLIENTE sem cliente vinculado");
        }
        return clienteId;
    }

    @PostMapping("/avulsa")
    public ResponseEntity<PagamentoResponseDTO> cobrancaAvulsa(@Valid @RequestBody CobrancaAvulsaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagamentoService.cobrancaAvulsa(dto));
    }

    @PatchMapping("/{id}/baixa")
    public ResponseEntity<PagamentoResponseDTO> registrarPagamento(
            @PathVariable Long id, @Valid @RequestBody RegistrarPagamentoDTO dto) {
        return ResponseEntity.ok(pagamentoService.registrarPagamento(id, dto.dataPagamento(), dto.formaPagamento()));
    }

    @PatchMapping("/{id}/estorno")
    public ResponseEntity<PagamentoResponseDTO> estornar(@PathVariable Long id) {
        return ResponseEntity.ok(pagamentoService.estornar(id));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<PagamentoResponseDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(pagamentoService.cancelar(id));
    }
}
