package com.rancho.api.animal;

import com.rancho.api.animal.dto.AnimalRequestDTO;
import com.rancho.api.animal.dto.AnimalResponseDTO;
import com.rancho.api.cliente.Cliente;
import com.rancho.api.cliente.ClienteService;
import com.rancho.api.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnimalService {

    private final AnimalRepository animalRepository;
    private final AnimalMapper animalMapper;
    private final ClienteService clienteService;

    @Transactional
    public AnimalResponseDTO create(AnimalRequestDTO dto) {
        Cliente cliente = clienteService.getCliente(dto.clienteId());
        Animal animal = Animal.builder()
                .cliente(cliente)
                .nome(dto.nome())
                .dataNascimento(dto.dataNascimento())
                .sexo(dto.sexo())
                .esporte(dto.esporte())
                .registro(dto.registro())
                .enfermidades(dto.enfermidades())
                .observacoes(dto.observacoes())
                .status(AnimalStatus.ATIVO)
                .build();
        return animalMapper.toResponseDTO(animalRepository.save(animal));
    }

    @Transactional(readOnly = true)
    public AnimalResponseDTO findById(Long id) {
        return animalMapper.toResponseDTO(getAnimal(id));
    }

    @Transactional(readOnly = true)
    public Page<AnimalResponseDTO> findAll(String search, AnimalStatus status, Long clienteId, Pageable pageable) {
        String searchParam = (search != null && !search.isBlank())
                ? "%" + search.toLowerCase() + "%" : null;
        return animalRepository.search(searchParam, status, clienteId, pageable)
                .map(animalMapper::toResponseDTO);
    }

    @Transactional
    public AnimalResponseDTO update(Long id, AnimalRequestDTO dto) {
        Animal animal = getAnimal(id);
        if (!animal.getCliente().getId().equals(dto.clienteId())) {
            animal.setCliente(clienteService.getCliente(dto.clienteId()));
        }
        animal.setNome(dto.nome());
        animal.setDataNascimento(dto.dataNascimento());
        animal.setSexo(dto.sexo());
        animal.setEsporte(dto.esporte());
        animal.setRegistro(dto.registro());
        animal.setEnfermidades(dto.enfermidades());
        animal.setObservacoes(dto.observacoes());
        return animalMapper.toResponseDTO(animalRepository.save(animal));
    }

    @Transactional
    public AnimalResponseDTO archive(Long id) {
        Animal animal = getAnimal(id);
        animal.setStatus(AnimalStatus.ARQUIVADO);
        return animalMapper.toResponseDTO(animalRepository.save(animal));
    }

    @Transactional
    public AnimalResponseDTO unarchive(Long id) {
        Animal animal = getAnimal(id);
        animal.setStatus(AnimalStatus.ATIVO);
        return animalMapper.toResponseDTO(animalRepository.save(animal));
    }

    public Animal getAnimal(Long id) {
        return animalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Animal", id));
    }
}
