package com.rancho.api.user;

import com.rancho.api.cliente.Cliente;
import com.rancho.api.cliente.ClienteService;
import com.rancho.api.common.exception.BusinessException;
import com.rancho.api.common.exception.ResourceNotFoundException;
import com.rancho.api.user.dto.GeneratedCredentialsDTO;
import com.rancho.api.user.dto.UserCreateDTO;
import com.rancho.api.user.dto.UserResponseDTO;
import com.rancho.api.user.dto.UserUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.text.Normalizer;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ClienteService clienteService;

    @Transactional
    public UserResponseDTO create(UserCreateDTO dto) {
        if (userRepository.existsByLogin(dto.login())) {
            throw new BusinessException("Login ja cadastrado");
        }
        if (userRepository.existsByEmail(dto.email())) {
            throw new BusinessException("Email ja cadastrado");
        }

        User user = userMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.password()));
        aplicarCliente(user, dto.clienteId());

        return userMapper.toResponseDTO(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findById(Long id) {
        return userMapper.toResponseDTO(getUser(id));
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDTO> findAll(String search, Pageable pageable) {
        String searchParam = (search != null && !search.isBlank())
                ? "%" + search.toLowerCase() + "%" : null;
        return userRepository.searchUsers(searchParam, pageable)
                .map(userMapper::toResponseDTO);
    }

    @Transactional
    public UserResponseDTO update(Long id, UserUpdateDTO dto) {
        User user = getUser(id);

        if (dto.email() != null && !dto.email().equals(user.getEmail())
                && userRepository.existsByEmail(dto.email())) {
            throw new BusinessException("Email ja cadastrado");
        }

        userMapper.updateEntityFromDTO(dto, user);
        aplicarCliente(user, dto.clienteId());
        return userMapper.toResponseDTO(userRepository.save(user));
    }

    /**
     * Aplica o vinculo com o cliente de acordo com o perfil:
     * CLIENTE exige um cliente valido; demais perfis nunca ficam vinculados.
     */
    private void aplicarCliente(User user, Long clienteId) {
        if (user.getRole() == Role.CLIENTE) {
            if (clienteId != null) {
                user.setCliente(clienteService.getCliente(clienteId));
            }
            if (user.getCliente() == null) {
                throw new BusinessException("Cliente e obrigatorio para o perfil CLIENTE");
            }
        } else {
            user.setCliente(null);
        }
    }

    @Transactional
    public void changePassword(Long id, String currentPassword, String newPassword) {
        User user = getUser(id);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessException("Senha atual incorreta");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Cria um usuario com perfil CLIENTE vinculado ao cliente informado, gerando
     * login e senha automaticamente. Devolve as credenciais em texto plano uma
     * unica vez (a senha e persistida com hash).
     */
    @Transactional
    public GeneratedCredentialsDTO gerarParaCliente(Long clienteId) {
        Cliente cliente = clienteService.getCliente(clienteId);
        if (userRepository.existsByClienteId(clienteId)) {
            throw new BusinessException("Cliente ja possui usuario");
        }

        String login = gerarLoginUnico(cliente.getNome());
        String rawPassword = gerarSenha();

        User user = User.builder()
                .name(cliente.getNome())
                .login(login)
                .email(resolverEmail(cliente, login))
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.CLIENTE)
                .cliente(cliente)
                .active(true)
                .build();
        userRepository.save(user);

        return new GeneratedCredentialsDTO(login, rawPassword);
    }

    /**
     * Redefine a senha do usuario do cliente para uma nova senha aleatoria e a
     * devolve junto do login (para reenvio das credenciais). Invalida a senha
     * anterior.
     */
    @Transactional
    public GeneratedCredentialsDTO redefinirCredenciaisCliente(Long clienteId) {
        User user = userRepository.findFirstByClienteIdOrderByIdAsc(clienteId)
                .orElseThrow(() -> new BusinessException("Cliente nao possui usuario"));

        String rawPassword = gerarSenha();
        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);

        return new GeneratedCredentialsDTO(user.getLogin(), rawPassword);
    }

    /** Gera um login a partir do nome, garantindo unicidade (sufixo numerico). */
    private String gerarLoginUnico(String nome) {
        String base = Normalizer.normalize(nome, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")          // remove acentos
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", ".")     // separadores -> ponto
                .replaceAll("(^\\.+)|(\\.+$)", ""); // apara pontos das pontas
        if (base.length() < 3) {
            base = (base + "cliente").substring(0, 3);
        }
        if (base.length() > 40) {
            base = base.substring(0, 40);
        }
        String login = base;
        int i = 1;
        while (userRepository.existsByLogin(login)) {
            login = base + (++i);
        }
        return login;
    }

    /**
     * Usa o e-mail do cliente quando disponivel e ainda nao utilizado; caso
     * contrario gera um e-mail interno unico (o e-mail e obrigatorio e unico).
     */
    private String resolverEmail(Cliente cliente, String login) {
        String email = cliente.getEmail();
        if (email != null && !email.isBlank() && !userRepository.existsByEmail(email)) {
            return email;
        }
        String candidato = login + "@cliente.rancho.local";
        int i = 1;
        while (userRepository.existsByEmail(candidato)) {
            candidato = login + (++i) + "@cliente.rancho.local";
        }
        return candidato;
    }

    private static final SecureRandom RANDOM = new SecureRandom();
    // Sem caracteres ambiguos (0/O, 1/l/I) para facilitar a leitura/digitacao.
    private static final String SENHA_ALFABETO = "abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private static String gerarSenha() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(SENHA_ALFABETO.charAt(RANDOM.nextInt(SENHA_ALFABETO.length())));
        }
        return sb.toString();
    }

    /** Redefine a senha sem exigir a atual (uso administrativo). */
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User user = getUser(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        User user = getUser(id);
        user.setActive(false);
        userRepository.save(user);
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }
}
