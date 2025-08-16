package com.artheus.cidadaoalerta.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroUsuario(
        @NotBlank(message = "Nome não pode estar vazio")
        @Size(min = 8, max = 20)
        String nome,

        @NotBlank(message = "Email não pode estar vazio")
        @Email(message = "Formato de email inválido")
        String email,

        @NotBlank
        @Size(min = 10, message = "Senha deve ter no mínimo 10 caracteres")
        String senha
) {
}
