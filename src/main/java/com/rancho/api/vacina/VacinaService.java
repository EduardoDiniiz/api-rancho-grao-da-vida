package com.rancho.api.vacina;

import com.rancho.api.animal.Animal;
import com.rancho.api.animal.AnimalService;
import com.rancho.api.common.exception.ResourceNotFoundException;
import com.rancho.api.vacina.dto.VacinaRequestDTO;
import com.rancho.api.vacina.dto.VacinaResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VacinaService {

    private static final int DIAS_ALERTA = 30;

    private final VacinaRepository vacinaRepository;
    private final AnimalService animalService;

    @Transactional
    public VacinaResponseDTO create(VacinaRequestDTO dto) {
        Animal animal = animalService.getAnimal(dto.animalId());
        Vacina vacina = Vacina.builder()
                .animal(animal)
                .nome(dto.nome())
                .dataAplicacao(dto.dataAplicacao())
                .dataVencimento(dto.dataVencimento())
                .observacao(dto.observacao())
                .build();
        return toDTO(vacinaRepository.save(vacina));
    }

    @Transactional
    public VacinaResponseDTO update(Long id, VacinaRequestDTO dto) {
        Vacina vacina = getVacina(id);
        if (!vacina.getAnimal().getId().equals(dto.animalId())) {
            vacina.setAnimal(animalService.getAnimal(dto.animalId()));
        }
        vacina.setNome(dto.nome());
        vacina.setDataAplicacao(dto.dataAplicacao());
        vacina.setDataVencimento(dto.dataVencimento());
        vacina.setObservacao(dto.observacao());
        return toDTO(vacinaRepository.save(vacina));
    }

    @Transactional(readOnly = true)
    public List<VacinaResponseDTO> findByAnimal(Long animalId) {
        return vacinaRepository.findByAnimalIdOrderByDataAplicacaoDesc(animalId)
                .stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<VacinaResponseDTO> findAlertas() {
        LocalDate hoje = LocalDate.now();
        List<VacinaResponseDTO> resultado = new java.util.ArrayList<>();
        resultado.addAll(vacinaRepository.findVencidas(hoje).stream().map(this::toDTO).toList());
        resultado.addAll(vacinaRepository.findVencendoEntre(hoje, hoje.plusDays(DIAS_ALERTA))
                .stream().map(this::toDTO).toList());
        return resultado;
    }

    @Transactional
    public void delete(Long id) {
        vacinaRepository.delete(getVacina(id));
    }

    private Vacina getVacina(Long id) {
        return vacinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vacina", id));
    }

    private VacinaResponseDTO toDTO(Vacina v) {
        return new VacinaResponseDTO(
                v.getId(),
                v.getAnimal().getId(),
                v.getAnimal().getNome(),
                v.getNome(),
                v.getDataAplicacao(),
                v.getDataVencimento(),
                v.getObservacao(),
                situacao(v.getDataVencimento()));
    }

    private String situacao(LocalDate vencimento) {
        if (vencimento == null) return "EM_DIA";
        LocalDate hoje = LocalDate.now();
        if (vencimento.isBefore(hoje)) return "VENCIDA";
        if (!vencimento.isAfter(hoje.plusDays(DIAS_ALERTA))) return "PROXIMA";
        return "EM_DIA";
    }
}
