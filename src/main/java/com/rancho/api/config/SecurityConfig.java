package com.rancho.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Value("${api.cors.allowed-origins:http://localhost:*,http://127.0.0.1:*}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v1/auth/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        // Webhook do Asaas: publico (validado por token no proprio handler)
                        .requestMatchers("/v1/webhooks/**").permitAll()
                        // Perfil CLIENTE: acesso somente-leitura a seus animais e faturamento.
                        // O escopo por cliente (ver apenas o que e seu) e aplicado nos controllers.
                        .requestMatchers("/v1/users/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/v1/animais/**").hasAnyRole("ADMIN", "OPERADOR", "CLIENTE")
                        .requestMatchers(HttpMethod.GET, "/v1/pagamentos/**").hasAnyRole("ADMIN", "OPERADOR", "CLIENTE")
                        // PIX das proprias cobrancas: CLIENTE pode gerar o QR (a baixa vem do webhook).
                        // A simulacao (modo mock) fica restrita a ADMIN/OPERADOR, nunca ao proprio cliente.
                        .requestMatchers(HttpMethod.POST, "/v1/pagamentos/*/pix/simular").hasAnyRole("ADMIN", "OPERADOR")
                        .requestMatchers(HttpMethod.POST, "/v1/pagamentos/*/pix").hasAnyRole("ADMIN", "OPERADOR", "CLIENTE")
                        // Pagamento com cartao da propria cobranca (escopo validado no service).
                        .requestMatchers(HttpMethod.POST, "/v1/pagamentos/*/cartao").hasAnyRole("ADMIN", "OPERADOR", "CLIENTE")
                        // Qualquer outra rota (incluindo escritas em animais/pagamentos) exige ADMIN ou OPERADOR
                        .anyRequest().hasAnyRole("ADMIN", "OPERADOR"))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
