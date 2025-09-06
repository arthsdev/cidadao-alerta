package com.artheus.cidadaoalerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record AtualizacaoUsuario(
        @Schema(nullable = true) @Size(min = 8, max = 20) String nome,

        @Schema(nullable = true) String email,

        @Schema(nullable = true) @Size(min = 10) String senha
) {
}
