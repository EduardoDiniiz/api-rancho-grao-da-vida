package com.rancho.api.servico;

import com.rancho.api.servico.dto.ServicoRequestDTO;
import com.rancho.api.servico.dto.ServicoResponseDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ServicoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Servico toEntity(ServicoRequestDTO dto);

    ServicoResponseDTO toResponseDTO(Servico servico);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(ServicoRequestDTO dto, @MappingTarget Servico servico);
}
