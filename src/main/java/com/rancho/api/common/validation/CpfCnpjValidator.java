package com.rancho.api.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfCnpjValidator implements ConstraintValidator<CpfCnpj, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // opcional; obrigatoriedade fica a cargo de @NotBlank
        }
        String digits = value.replaceAll("\\D", "");
        if (digits.length() == 11) {
            return isValidCpf(digits);
        }
        if (digits.length() == 14) {
            return isValidCnpj(digits);
        }
        return false;
    }

    private boolean isValidCpf(String cpf) {
        if (cpf.chars().distinct().count() == 1) {
            return false; // todos os digitos iguais
        }
        int d1 = checkDigit(cpf, 9, 10);
        int d2 = checkDigit(cpf, 10, 11);
        return d1 == (cpf.charAt(9) - '0') && d2 == (cpf.charAt(10) - '0');
    }

    private int checkDigit(String cpf, int length, int startWeight) {
        int sum = 0;
        int weight = startWeight;
        for (int i = 0; i < length; i++) {
            sum += (cpf.charAt(i) - '0') * weight--;
        }
        int rest = sum % 11;
        return rest < 2 ? 0 : 11 - rest;
    }

    private boolean isValidCnpj(String cnpj) {
        if (cnpj.chars().distinct().count() == 1) {
            return false;
        }
        int[] weights1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int d1 = cnpjDigit(cnpj, weights1);
        int d2 = cnpjDigit(cnpj, weights2);
        return d1 == (cnpj.charAt(12) - '0') && d2 == (cnpj.charAt(13) - '0');
    }

    private int cnpjDigit(String cnpj, int[] weights) {
        int sum = 0;
        for (int i = 0; i < weights.length; i++) {
            sum += (cnpj.charAt(i) - '0') * weights[i];
        }
        int rest = sum % 11;
        return rest < 2 ? 0 : 11 - rest;
    }
}
