package com.rancho.api.animalservico;

import com.rancho.api.animal.Animal;
import com.rancho.api.animal.AnimalService;
import com.rancho.api.animalservico.dto.AnimalServicoRequestDTO;
import com.rancho.api.animalservico.dto.AnimalServicoResponseDTO;
import com.rancho.api.common.exception.ResourceNotFoundException;
import com.rancho.api.pagamento.PagamentoService;
import com.rancho.api.servico.Servico;
import com.rancho.api.servico.ServicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnimalServicoService {

    private final AnimalServicoRepository animalServicoRepository;
    private final AnimalService animalService;
    private final ServicoService servicoService;
    private final PagamentoService pagamentoService;

    @Transactional
    public AnimalServicoResponseDTO create(AnimalServicoRequestDTO dto) {
        Animal animal = animalService.getAnimal(dto.animalId());
        Servico servico = servicoService.getOrCreateByNome(dto.servicoNome(), dto.valor());

        AnimalServico as = AnimalServico.builder()
                .animal(animal)
                .servico(servico)
                .valor(dto.valor())
                .dataInicio(dto.dataInicio())
                .proximoVencimento(dto.dataInicio())
                .recorrenciaDias(dto.recorrenciaDias())
                .descricao(dto.descricao())
                .status(AnimalServicoStatus.ATIVO)
                .build();
        AnimalServico salvo = animalServicoRepository.save(as);

        // RF010 - gera a primeira cobranca do contrato
        pagamentoService.gerarCobranca(salvo);

        return toDTO(salvo);
    }

    @Transactional(readOnly = true)
    public List<AnimalServicoResponseDTO> findByAnimal(Long animalId) {
        return animalServicoRepository.findByAnimalIdOrderByCreatedAtDesc(animalId)
                .stream().map(this::toDTO).toList();
    }

    @Transactional
    public AnimalServicoResponseDTO update(Long id, AnimalServicoRequestDTO dto) {
        AnimalServico as = getAnimalServico(id);
        as.setValor(dto.valor());
        as.setRecorrenciaDias(dto.recorrenciaDias());
        as.setDescricao(dto.descricao());
        if (dto.servicoNome() != null && !dto.servicoNome().isBlank()) {
            as.setServico(servicoService.getOrCreateByNome(dto.servicoNome(), dto.valor()));
        }
        return toDTO(animalServicoRepository.save(as));
    }

    @Transactional
    public AnimalServicoResponseDTO alterarStatus(Long id, AnimalServicoStatus status) {
        AnimalServico as = getAnimalServico(id);
        as.setStatus(status);
        return toDTO(animalServicoRepository.save(as));
    }

    public AnimalServico getAnimalServico(Long id) {
        return animalServicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servico contratado", id));
    }

    private AnimalServicoResponseDTO toDTO(AnimalServico as) {
        return new AnimalServicoResponseDTO(
                as.getId(),
                as.getAnimal().getId(),
                as.getAnimal().getNome(),
                as.getServico().getId(),
                as.getServico().getNome(),
                as.getValor(),
                as.getDataInicio(),
                as.getProximoVencimento(),
                as.getRecorrenciaDias(),
                as.getDescricao(),
                as.getStatus());
    }
}
