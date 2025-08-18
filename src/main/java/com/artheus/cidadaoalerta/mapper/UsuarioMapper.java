package com.artheus.cidadaoalerta.mapper;

import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.dto.AtualizacaoUsuario;
import com.artheus.cidadaoalerta.dto.CadastroUsuario;
import com.artheus.cidadaoalerta.dto.DetalhamentoUsuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    // DTO de entrada → Entity
    @Mapping(target = "papel", constant = "ROLE_USER")
    Usuario toEntity(CadastroUsuario cadastroUsuario);

    // Entity → DTO de saída
    DetalhamentoUsuario toDetalhamentoDto(Usuario usuario);

    // Atualiza uma entidade existente a partir de um DTO
    void updateUsuarioFromDto(AtualizacaoUsuario dto, @MappingTarget Usuario usuario);
}
