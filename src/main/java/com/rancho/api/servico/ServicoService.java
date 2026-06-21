package com.rancho.api.servico;

import com.rancho.api.common.exception.ResourceNotFoundException;
import com.rancho.api.servico.dto.ServicoRequestDTO;
import com.rancho.api.servico.dto.ServicoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final ServicoMapper servicoMapper;

    @Transactional
    public ServicoResponseDTO create(ServicoRequestDTO dto) {
        return servicoMapper.toResponseDTO(servicoRepository.save(servicoMapper.toEntity(dto)));
    }

    @Transactional(readOnly = true)
    public ServicoResponseDTO findById(Long id) {
        return servicoMapper.toResponseDTO(getServico(id));
    }

    @Transactional(readOnly = true)
    public Page<ServicoResponseDTO> findAll(String search, boolean apenasAtivos, Pageable pageable) {
        String searchParam = (search != null && !search.isBlank())
                ? "%" + search.toLowerCase() + "%" : null;
        return servicoRepository.search(searchParam, apenasAtivos, pageable)
                .map(servicoMapper::toResponseDTO);
    }

    @Transactional
    public ServicoResponseDTO update(Long id, ServicoRequestDTO dto) {
        Servico servico = getServico(id);
        servicoMapper.updateEntityFromDTO(dto, servico);
        return servicoMapper.toResponseDTO(servicoRepository.save(servico));
    }

    @Transactional
    public ServicoResponseDTO toggleActive(Long id, boolean active) {
        Servico servico = getServico(id);
        servico.setActive(active);
        return servicoMapper.toResponseDTO(servicoRepository.save(servico));
    }

    public Servico getServico(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servico", id));
    }
}
