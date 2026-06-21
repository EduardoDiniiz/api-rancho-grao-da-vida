package com.rancho.api.animal;

import com.rancho.api.animal.dto.AnimalResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnimalMapper {

    @Mapping(target = "clienteId", source = "cliente.id")
    @Mapping(target = "clienteNome", source = "cliente.nome")
    AnimalResponseDTO toResponseDTO(Animal animal);
}
