package com.artheus.cidadaoalerta.dto;

import com.artheus.cidadaoalerta.domain.enums.Role;

public record DetalhamentoUsuario(
        Long id,
        String nome,
        String email,
        boolean ativo,
        Role papel

) {
}
