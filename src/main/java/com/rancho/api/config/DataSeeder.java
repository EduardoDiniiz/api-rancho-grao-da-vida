package com.rancho.api.config;

import com.rancho.api.user.Role;
import com.rancho.api.user.User;
import com.rancho.api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seed inicial (profile "local"): cria apenas os usuarios administradores
 * quando o banco esta vazio. Sem dados de demonstracao.
 */
@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Banco ja possui usuarios, seed ignorado.");
            return;
        }

        log.info("Criando usuarios iniciais...");

        userRepository.save(User.builder()
                .name("Eduardo Diniz")
                .email("eduardodiniz@graodavida.com.br")
                .login("eduardodiniz")
                .password(passwordEncoder.encode("Gestao01%"))
                .role(Role.ADMIN).active(true).build());

        userRepository.save(User.builder()
                .name("Carlinhos JC")
                .email("carlinhosjc@graodavida.com.br")
                .login("carlinhosjc")
                .password(passwordEncoder.encode("senha0123%"))
                .role(Role.ADMIN).active(true).build());

        log.info("Usuarios criados: eduardodiniz e carlinhosjc (ADMIN).");
    }
}
