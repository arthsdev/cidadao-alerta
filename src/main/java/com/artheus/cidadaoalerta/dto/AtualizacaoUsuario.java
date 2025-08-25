package com.artheus.cidadaoalerta.dto;

import jakarta.validation.constraints.Size;
import jakarta.annotation.Nullable;

public record AtualizacaoUsuario(
        @Nullable
        @Size(min = 8, max = 20)
        String nome,

        @Nullable
        String email,

        @Nullable
        @Size(min = 10)
        String senha
) {}
