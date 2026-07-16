package com.rancho.api.cliente;

import com.rancho.api.animal.AnimalRepository;
import com.rancho.api.cliente.dto.ClienteRequestDTO;
import com.rancho.api.cliente.dto.ClienteResponseDTO;
import com.rancho.api.common.exception.BusinessException;
import com.rancho.api.common.exception.ResourceNotFoundException;
import com.rancho.api.user.User;
import com.rancho.api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final AnimalRepository animalRepository;
    private final UserRepository userRepository;

    @Transactional
    public ClienteResponseDTO create(ClienteRequestDTO dto) {
        Cliente cliente = clienteMapper.toEntity(dto);
        cliente.setCpfCnpj(digitsOrNull(dto.cpfCnpj()));
        cliente.setTelefone(digitsOrNull(dto.telefone()));
        return toDTO(clienteRepository.save(cliente));
    }

    @Transactional(readOnly = true)
    public ClienteResponseDTO findById(Long id) {
        return toDTO(getCliente(id));
    }

    @Transactional(readOnly = true)
    public Page<ClienteResponseDTO> findAll(String search, Pageable pageable) {
        boolean hasSearch = search != null && !search.isBlank();
        String searchParam = hasSearch ? "%" + search.toLowerCase() + "%" : null;
        // busca por CPF/CNPJ ou telefone ignora a mascara digitada pelo usuario
        String digits = hasSearch ? search.replaceAll("\\D", "") : "";
        String digitsParam = !digits.isEmpty() ? "%" + digits + "%" : null;
        return clienteRepository.search(searchParam, digitsParam, pageable).map(this::toDTO);
    }

    @Transactional
    public ClienteResponseDTO update(Long id, ClienteRequestDTO dto) {
        Cliente cliente = getCliente(id);
        clienteMapper.updateEntityFromDTO(dto, cliente);
        // CPF/CNPJ e telefone sao persistidos apenas com digitos; a mascara e
        // aplicada somente na exibicao (frontend).
        if (dto.cpfCnpj() != null) cliente.setCpfCnpj(digitsOrNull(dto.cpfCnpj()));
        if (dto.telefone() != null) cliente.setTelefone(digitsOrNull(dto.telefone()));
        return toDTO(clienteRepository.save(cliente));
    }

    @Transactional
    public void delete(Long id) {
        Cliente cliente = getCliente(id);
        if (animalRepository.countByClienteIdAndStatus(id,
                com.rancho.api.animal.AnimalStatus.ATIVO) > 0) {
            throw new BusinessException("Nao e possivel excluir cliente com animais ativos vinculados");
        }
        clienteRepository.delete(cliente);
    }

    public Cliente getCliente(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }

    /** Mantem apenas digitos; devolve null quando vazio (limpa o campo). */
    private static String digitsOrNull(String value) {
        if (value == null) return null;
        String digits = value.replaceAll("\\D", "");
        return digits.isEmpty() ? null : digits;
    }

    private ClienteResponseDTO toDTO(Cliente cliente) {
        long total = animalRepository.countByClienteId(cliente.getId());
        User usuario = userRepository.findFirstByCliente_IdOrderByIdAsc(cliente.getId()).orElse(null);
        return clienteMapper.toResponseDTO(cliente, total,
                usuario != null ? usuario.getId() : null,
                usuario != null ? usuario.getLogin() : null);
    }
}
