package com.rancho.api.cliente.dto;

import com.rancho.api.common.validation.CpfCnpj;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteRequestDTO(
        @NotBlank(message = "Nome e obrigatorio")
        @Size(min = 2, max = 255, message = "Nome deve ter entre 2 e 255 caracteres")
        String nome,

        @CpfCnpj(message = "CPF/CNPJ invalido")
        @Size(max = 20, message = "CPF/CNPJ deve ter no maximo 20 caracteres")
        String cpfCnpj,

        @Pattern(
                regexp = "^$|^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}$",
                message = "Telefone invalido. Use o formato (99) 99999-9999")
        @Size(max = 20, message = "Telefone deve ter no maximo 20 caracteres")
        String telefone,

        @Email(message = "Email invalido")
        @Size(max = 255, message = "Email deve ter no maximo 255 caracteres")
        String email,

        String endereco,

        String observacoes
) {}
