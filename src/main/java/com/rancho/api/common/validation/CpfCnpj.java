package com.rancho.api.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Valida um CPF ou CNPJ (com ou sem mascara). Campos nulos/vazios sao
 * considerados validos - combine com {@code @NotBlank} quando obrigatorio.
 */
@Documented
@Constraint(validatedBy = CpfCnpjValidator.class)
@Target({FIELD, PARAMETER, RECORD_COMPONENT})
@Retention(RUNTIME)
public @interface CpfCnpj {
    String message() default "CPF/CNPJ invalido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
