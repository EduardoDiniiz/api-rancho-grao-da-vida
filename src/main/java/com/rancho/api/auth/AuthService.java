package com.rancho.api.auth;

import com.rancho.api.auth.dto.AuthResponseDTO;
import com.rancho.api.auth.dto.LoginDTO;
import com.rancho.api.common.exception.BusinessException;
import com.rancho.api.config.JwtService;
import com.rancho.api.user.User;
import com.rancho.api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponseDTO login(LoginDTO dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.login(), dto.password()));

        User user = userRepository.findByLoginOrEmail(dto.login())
                .orElseThrow(() -> new BusinessException("Credenciais invalidas", HttpStatus.UNAUTHORIZED));

        String token = jwtService.generateToken(user);
        return new AuthResponseDTO(token, jwtService.getExpiration(),
                user.getId(), user.getName(), user.getRole());
    }
}
