package com.artheus.cidadaoalerta.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RequisicaoLogin(
        @Email(message = "Email inválido")
        @NotBlank(message = "Email é obrigatório")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        String senha
) {}
