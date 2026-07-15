package com.rancho.api.user;

import com.rancho.api.cliente.ClienteService;
import com.rancho.api.common.exception.BusinessException;
import com.rancho.api.common.exception.ResourceNotFoundException;
import com.rancho.api.user.dto.UserCreateDTO;
import com.rancho.api.user.dto.UserResponseDTO;
import com.rancho.api.user.dto.UserUpdateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
