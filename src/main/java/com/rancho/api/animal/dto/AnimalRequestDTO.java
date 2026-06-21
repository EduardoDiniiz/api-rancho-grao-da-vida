package com.rancho.api.animal.dto;

import com.rancho.api.animal.Esporte;
import com.rancho.api.animal.Sexo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AnimalRequestDTO(
        @NotNull(message = "Cliente e obrigatorio")
        Long clienteId,

        @NotBlank(message = "Nome e obrigatorio")
        @Size(min = 2, max = 255, message = "Nome deve ter entre 2 e 255 caracteres")
        String nome,

        @PastOrPresent(message = "Data de nascimento nao pode ser futura")
        LocalDate dataNascimento,

        Sexo sexo,

        Esporte esporte,

        @Size(max = 100, message = "Registro deve ter no maximo 100 caracteres")
        String registro,

        String enfermidades,

        String observacoes
) {}
