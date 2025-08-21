package com.artheus.cidadaoalerta.dto;

import com.artheus.cidadaoalerta.model.Localizacao;
import com.artheus.cidadaoalerta.model.enums.CategoriaReclamacao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CadastroReclamacao(

        @NotBlank(message = "Titulo não deve estar vazio")
        @Size(min = 5, max = 100)
        String titulo,

        @NotBlank(message = "A descricao nao pode estar vazia")
        @Size(min = 20, max = 400)
        String descricao,

        @NotNull(message = "Categoria da reclamação não pode estar vazia")
        CategoriaReclamacao categoriaReclamacao,

        @NotNull(message = "Localização não pode estar vazia")
        @Valid
        Localizacao localizacao
) {
}
