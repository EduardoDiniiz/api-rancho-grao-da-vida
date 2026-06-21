package com.rancho.api.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " nao encontrado com id: " + id, HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String resource, String field, String value) {
        super(resource + " nao encontrado com " + field + ": " + value, HttpStatus.NOT_FOUND);
    }
}
