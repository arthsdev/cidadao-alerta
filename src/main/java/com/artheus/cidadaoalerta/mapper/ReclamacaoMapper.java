package com.artheus.cidadaoalerta.mapper;

import com.artheus.cidadaoalerta.dto.AtualizacaoReclamacao;
import com.artheus.cidadaoalerta.dto.CadastroReclamacao;
import com.artheus.cidadaoalerta.dto.DetalhamentoReclamacao;
import com.artheus.cidadaoalerta.model.Reclamacao;
import com.artheus.cidadaoalerta.model.Usuario;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReclamacaoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "status", expression = "java(com.artheus.cidadaoalerta.model.enums.StatusReclamacao.ABERTA)")
    @Mapping(target = "usuario", source = "usuario")
    Reclamacao toEntity(CadastroReclamacao dto, Usuario usuario);

    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "nomeUsuario", source = "usuario.nome")
    @Mapping(target = "statusReclamacao", source = "status")
    DetalhamentoReclamacao toDetalhamentoDto(Reclamacao reclamacao);

    List<DetalhamentoReclamacao> toDetalhamentoList(List<Reclamacao> reclamacoes);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateReclamacaoFromDto(AtualizacaoReclamacao dto, @MappingTarget Reclamacao reclamacao);
}

