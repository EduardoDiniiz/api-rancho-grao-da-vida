package com.rancho.api.user.dto;

/**
 * Credenciais devolvidas uma unica vez ao gerar/redefinir o acesso de um cliente.
 * A senha vem em texto plano apenas nesta resposta (nao e armazenada assim).
 */
public record GeneratedCredentialsDTO(
        String login,
        String senha
) {}
