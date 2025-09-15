package com.artheus.cidadaoalerta.dto;

import com.artheus.cidadaoalerta.model.enums.CategoriaReclamacao;
import com.artheus.cidadaoalerta.model.Localizacao;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AtualizacaoReclamacao {

        @Size(min = 5, max = 100)
        private String titulo;

        @Size(min = 20, max = 400)
        private String descricao;

        private CategoriaReclamacao categoriaReclamacao;

        private Localizacao localizacao;
}

