package com.rancho.api.cliente;

import com.rancho.api.cliente.dto.ClienteRequestDTO;
import com.rancho.api.cliente.dto.ClienteResponseDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Cliente toEntity(ClienteRequestDTO dto);

    @Mapping(target = "totalAnimais", source = "totalAnimais")
    ClienteResponseDTO toResponseDTO(Cliente cliente, Long totalAnimais, Long usuarioId, String usuarioLogin);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(ClienteRequestDTO dto, @MappingTarget Cliente cliente);
}
