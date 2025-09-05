package com.artheus.cidadaoalerta.unit.model;

import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.model.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioAuthoritiesTest {

    @Test
    void deveRetornarAuthoritiesCorretamente() {
        Usuario usuario = new Usuario();
        usuario.setPapel(Role.ROLE_ADMIN);

        Collection<? extends GrantedAuthority> authorities = usuario.getAuthorities();

        assertNotNull(authorities, "Authorities não deve ser nulo");
        assertEquals(1, authorities.size(), "Deve conter exatamente uma authority");
        assertTrue(
                authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")),
                "Deve conter ROLE_ADMIN"
        );
    }

    @Test
    void deveRetornarTrueParaMetodosDeContaQuandoAtivo() {
        Usuario usuario = new Usuario();
        usuario.setAtivo(true);

        assertAll(
                () -> assertTrue(usuario.isAccountNonExpired(), "Conta não expirou deve ser true"),
                () -> assertTrue(usuario.isAccountNonLocked(), "Conta não bloqueada deve ser true"),
                () -> assertTrue(usuario.isCredentialsNonExpired(), "Credenciais não expiraram deve ser true"),
                () -> assertTrue(usuario.isEnabled(), "Conta habilitada deve ser true")
        );
    }

    @Test
    void deveRetornarFalseParaMetodosDeContaQuandoInativo() {
        Usuario usuario = new Usuario();
        usuario.setAtivo(false);

        assertAll(
                () -> assertFalse(usuario.isAccountNonExpired(), "Conta expirada deve ser false"),
                () -> assertFalse(usuario.isAccountNonLocked(), "Conta bloqueada deve ser false"),
                () -> assertFalse(usuario.isCredentialsNonExpired(), "Credenciais expiradas devem ser false"),
                () -> assertFalse(usuario.isEnabled(), "Conta desabilitada deve ser false")
        );
    }
}
