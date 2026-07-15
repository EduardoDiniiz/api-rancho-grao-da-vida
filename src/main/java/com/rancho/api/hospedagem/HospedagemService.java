package com.rancho.api.hospedagem;

import com.rancho.api.animal.Animal;
import com.rancho.api.animal.AnimalService;
import com.rancho.api.baia.Baia;
import com.rancho.api.baia.BaiaRepository;
import com.rancho.api.baia.BaiaStatus;
import com.rancho.api.common.exception.BusinessException;
import com.rancho.api.common.exception.ResourceNotFoundException;
import com.rancho.api.hospedagem.dto.HospedagemRequestDTO;
import com.rancho.api.hospedagem.dto.HospedagemResponseDTO;
import com.rancho.api.pagamento.PagamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class HospedagemService {

    private final HospedagemRepository hospedagemRepository;
    private final BaiaRepository baiaRepository;
    private final AnimalService animalService;
    private final PagamentoService pagamentoService;

    @Transactional
    public HospedagemResponseDTO registrarEntrada(HospedagemRequestDTO dto) {
        Animal animal = animalService.getAnimal(dto.animalId());
        Baia baia = baiaRepository.findById(dto.baiaId())
                .orElseThrow(() -> new ResourceNotFoundException("Baia", dto.baiaId()));

        // RN002 - uma baia so pode estar ocupada por um animal simultaneamente
        if (hospedagemRepository.existsByBaiaIdAndStatus(baia.getId(), HospedagemStatus.ATIVO)) {
            throw new BusinessException("Esta baia ja esta ocupada");
        }
        if (baia.getStatus() == BaiaStatus.MANUTENCAO) {
            throw new BusinessException("Baia em manutencao nao pode receber animais");
        }
        if (hospedagemRepository.existsByAnimalIdAndStatus(animal.getId(), HospedagemStatus.ATIVO)) {
            throw new BusinessException("Este animal ja possui uma hospedagem ativa");
        }

        boolean comCobranca = dto.valorMensal() != null && dto.valorMensal().signum() > 0;

        Hospedagem hospedagem = Hospedagem.builder()
                .animal(animal)
                .cliente(animal.getCliente())
                .baia(baia)
                .dataEntrada(dto.dataEntrada())
                .valorMensal(comCobranca ? dto.valorMensal() : null)
                .proximoVencimento(comCobranca ? dto.dataEntrada() : null)
                .status(HospedagemStatus.ATIVO)
                .build();

        baia.setStatus(BaiaStatus.OCUPADA);
        baiaRepository.save(baia);

        Hospedagem salvo = hospedagemRepository.save(hospedagem);

        // Gera a primeira fatura mensal da hospedagem (as demais nascem na baixa)
        if (comCobranca) {
            pagamentoService.gerarCobrancaHospedagem(salvo);
        }

        return toDTO(salvo);
    }

    @Transactional
    public HospedagemResponseDTO registrarSaida(Long id, LocalDate dataSaida) {
        Hospedagem hospedagem = getHospedagem(id);
        if (hospedagem.getStatus() == HospedagemStatus.ENCERRADO) {
            throw new BusinessException("Hospedagem ja encerrada");
        }
        hospedagem.setDataSaida(dataSaida != null ? dataSaida : LocalDate.now());
        hospedagem.setStatus(HospedagemStatus.ENCERRADO);

        Baia baia = hospedagem.getBaia();
        baia.setStatus(BaiaStatus.LIVRE);
        baiaRepository.save(baia);

        return toDTO(hospedagemRepository.save(hospedagem));
    }

    @Transactional(readOnly = true)
    public Page<HospedagemResponseDTO> findAll(HospedagemStatus status, Long animalId, Long baiaId, Pageable pageable) {
        return hospedagemRepository.search(status, animalId, baiaId, pageable).map(this::toDTO);
    }

    private Hospedagem getHospedagem(Long id) {
        return hospedagemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospedagem", id));
    }

    private HospedagemResponseDTO toDTO(Hospedagem h) {
        return new HospedagemResponseDTO(
                h.getId(),
                h.getAnimal().getId(),
                h.getAnimal().getNome(),
                h.getCliente().getId(),
                h.getCliente().getNome(),
                h.getBaia().getId(),
                h.getBaia().getIdentificacao(),
                h.getDataEntrada(),
                h.getDataSaida(),
                h.getValorMensal(),
                h.getProximoVencimento(),
                h.getStatus());
    }
}
