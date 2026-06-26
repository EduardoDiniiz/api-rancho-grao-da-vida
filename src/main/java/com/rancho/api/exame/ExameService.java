package com.rancho.api.exame;

import com.rancho.api.animal.Animal;
import com.rancho.api.animal.AnimalService;
import com.rancho.api.common.exception.ResourceNotFoundException;
import com.rancho.api.exame.dto.ExameRequestDTO;
import com.rancho.api.exame.dto.ExameResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExameService {

    private final ExameRepository exameRepository;
    private final AnimalService animalService;

    @Transactional
    public ExameResponseDTO create(ExameRequestDTO dto) {
        Animal animal = animalService.getAnimal(dto.animalId());
        Exame exame = Exame.builder()
                .animal(animal)
                .nome(dto.nome())
                .data(dto.data())
                .resultado(dto.resultado())
                .veterinario(dto.veterinario())
                .observacao(dto.observacao())
                .build();
        return toDTO(exameRepository.save(exame));
    }

    @Transactional
    public ExameResponseDTO update(Long id, ExameRequestDTO dto) {
        Exame exame = getExame(id);
        if (!exame.getAnimal().getId().equals(dto.animalId())) {
            exame.setAnimal(animalService.getAnimal(dto.animalId()));
        }
        exame.setNome(dto.nome());
        exame.setData(dto.data());
        exame.setResultado(dto.resultado());
        exame.setVeterinario(dto.veterinario());
        exame.setObservacao(dto.observacao());
        return toDTO(exameRepository.save(exame));
    }

    @Transactional(readOnly = true)
    public List<ExameResponseDTO> findByAnimal(Long animalId) {
        return exameRepository.findByAnimalIdOrderByDataDesc(animalId)
                .stream().map(this::toDTO).toList();
    }

    @Transactional
    public void delete(Long id) {
        exameRepository.delete(getExame(id));
    }

    private Exame getExame(Long id) {
        return exameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exame", id));
    }

    private ExameResponseDTO toDTO(Exame e) {
        return new ExameResponseDTO(
                e.getId(),
                e.getAnimal().getId(),
                e.getAnimal().getNome(),
                e.getNome(),
                e.getData(),
                e.getResultado(),
                e.getVeterinario(),
                e.getObservacao());
    }
}
