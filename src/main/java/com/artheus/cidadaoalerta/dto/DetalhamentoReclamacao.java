package com.artheus.cidadaoalerta.dto;

import com.artheus.cidadaoalerta.model.Localizacao;
import com.artheus.cidadaoalerta.model.enums.CategoriaReclamacao;
import com.artheus.cidadaoalerta.model.enums.StatusReclamacao;

import java.time.LocalDateTime;

public record DetalhamentoReclamacao(

        Long id,

        String titulo,

        String descricao,

        CategoriaReclamacao categoriaReclamacao,

        Localizacao localizacao,

        StatusReclamacao statusReclamacao,

        LocalDateTime dataCriacao,

        Long usuarioId,

        String nomeUsuario
) {
}
