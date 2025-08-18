package com.artheus.cidadaoalerta.mapper;

import com.artheus.cidadaoalerta.dto.AtualizacaoReclamacao;
import com.artheus.cidadaoalerta.dto.CadastroReclamacao;
import com.artheus.cidadaoalerta.dto.DetalhamentoReclamacao;
import com.artheus.cidadaoalerta.model.Reclamacao;
import com.artheus.cidadaoalerta.model.Usuario;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ReclamacaoMapper {

    // DTO de cadastro → Entity
    @Mapping(target = "id", ignore = true)         // Ignora id na criação
    @Mapping(target = "version", ignore = true)    // Ignora version na criação
    @Mapping(target = "status", constant = "ABERTA")
    @Mapping(target = "usuario", source = "usuario")
    Reclamacao toEntity(CadastroReclamacao dto, Usuario usuario);

    // Entity → DTO de saída
    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "nomeUsuario", source = "usuario.nome")
    @Mapping(target = "statusReclamacao", source = "status")
    DetalhamentoReclamacao toDetalhamentoDto(Reclamacao reclamacao);

    // Atualiza uma entidade existente a partir do DTO de atualização
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE) //campos nulos no Dto nao sobscrevem valores existentes
    void updateReclamacaoFromDto(AtualizacaoReclamacao dto, @MappingTarget Reclamacao reclamacao);
}

