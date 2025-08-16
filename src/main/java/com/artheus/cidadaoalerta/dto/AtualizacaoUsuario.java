package com.artheus.cidadaoalerta.dto;

import jakarta.validation.constraints.Size;

public record AtualizacaoUsuario(
        @Size(min = 8, max = 20)
        String nome,
        String email,

        @Size(min = 10)
        String senha
) {
}
