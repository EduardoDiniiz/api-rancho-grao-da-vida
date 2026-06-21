package com.rancho.api.baia;

import com.rancho.api.baia.dto.BaiaRequestDTO;
import com.rancho.api.baia.dto.BaiaResponseDTO;
import com.rancho.api.common.exception.BusinessException;
import com.rancho.api.common.exception.ResourceNotFoundException;
import com.rancho.api.hospedagem.HospedagemRepository;
import com.rancho.api.hospedagem.HospedagemStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BaiaService {

    private final BaiaRepository baiaRepository;
    private final HospedagemRepository hospedagemRepository;

    @Transactional
    public BaiaResponseDTO create(BaiaRequestDTO dto) {
        if (baiaRepository.existsByIdentificacao(dto.identificacao())) {
            throw new BusinessException("Ja existe uma baia com esta identificacao");
        }
        Baia baia = Baia.builder()
                .identificacao(dto.identificacao())
                .localizacao(dto.localizacao())
                .capacidade(dto.capacidade() != null ? dto.capacidade() : 1)
                .status(dto.status() != null ? dto.status() : BaiaStatus.LIVRE)
                .observacao(dto.observacao())
                .build();
        return toDTO(baiaRepository.save(baia));
    }

    @Transactional(readOnly = true)
    public BaiaResponseDTO findById(Long id) {
        return toDTO(getBaia(id));
    }

    @Transactional(readOnly = true)
    public Page<BaiaResponseDTO> findAll(String search, BaiaStatus status, Pageable pageable) {
        String searchParam = (search != null && !search.isBlank())
                ? "%" + search.toLowerCase() + "%" : null;
        return baiaRepository.search(searchParam, status, pageable).map(this::toDTO);
    }

    @Transactional
    public BaiaResponseDTO update(Long id, BaiaRequestDTO dto) {
        Baia baia = getBaia(id);
        if (!baia.getIdentificacao().equals(dto.identificacao())
                && baiaRepository.existsByIdentificacao(dto.identificacao())) {
            throw new BusinessException("Ja existe uma baia com esta identificacao");
        }
        baia.setIdentificacao(dto.identificacao());
        baia.setLocalizacao(dto.localizacao());
        if (dto.capacidade() != null) baia.setCapacidade(dto.capacidade());
        if (dto.status() != null) baia.setStatus(dto.status());
        baia.setObservacao(dto.observacao());
        return toDTO(baiaRepository.save(baia));
    }

    @Transactional
    public void delete(Long id) {
        Baia baia = getBaia(id);
        if (hospedagemRepository.existsByBaiaIdAndStatus(id, HospedagemStatus.ATIVO)) {
            throw new BusinessException("Nao e possivel excluir baia ocupada");
        }
        baiaRepository.delete(baia);
    }

    public Baia getBaia(Long id) {
        return baiaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Baia", id));
    }

    private BaiaResponseDTO toDTO(Baia baia) {
        String animalAtual = hospedagemRepository
                .findFirstByBaiaIdAndStatus(baia.getId(), HospedagemStatus.ATIVO)
                .map(h -> h.getAnimal().getNome())
                .orElse(null);
        return new BaiaResponseDTO(
                baia.getId(),
                baia.getIdentificacao(),
                baia.getLocalizacao(),
                baia.getCapacidade(),
                baia.getStatus(),
                baia.getObservacao(),
                animalAtual);
    }
}
