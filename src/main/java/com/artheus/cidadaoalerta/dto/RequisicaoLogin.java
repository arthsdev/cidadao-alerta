package com.artheus.cidadaoalerta.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RequisicaoLogin(
        @Schema(description = "Email do usuário", example = "joao@email.com")
        @Email(message = "Email inválido")
        @NotBlank(message = "Email é obrigatório")
        String email,

        @Schema(description = "Senha do usuário", example = "senha123")
        @NotBlank(message = "Senha é obrigatória")
        String senha
) {}
