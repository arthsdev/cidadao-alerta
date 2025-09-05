package com.artheus.cidadaoalerta.unit.security;

import com.artheus.cidadaoalerta.repository.ReclamacaoRepository;
import com.artheus.cidadaoalerta.security.ReclamacaoSecurity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReclamacaoSecurityTest {

    @Mock
    private ReclamacaoRepository reclamacaoRepository;

    private ReclamacaoSecurity reclamacaoSecurity;

    @BeforeEach
    void setUp() {
        reclamacaoSecurity = new ReclamacaoSecurity(reclamacaoRepository);
    }

    private void setAuthentication(String email, String... roles) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        TestingAuthenticationToken auth = new TestingAuthenticationToken(email, "senha", roles);
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void isOwner_deveRetornarTrueSeUsuarioForDono() {
        setAuthentication("teste@email.com");

        when(reclamacaoRepository.existsByIdAndUsuario_Email(1L, "teste@email.com"))
                .thenReturn(true);

        assertTrue(reclamacaoSecurity.isOwner(1L));
        verify(reclamacaoRepository).existsByIdAndUsuario_Email(1L, "teste@email.com");
    }

    @Test
    void isOwner_deveRetornarFalseSeUsuarioNaoForDono() {
        setAuthentication("outro@email.com");

        when(reclamacaoRepository.existsByIdAndUsuario_Email(1L, "outro@email.com"))
                .thenReturn(false);

        assertFalse(reclamacaoSecurity.isOwner(1L));
    }

    @Test
    void isOwner_deveRetornarFalseSeNaoHouverAutenticacao() {
        SecurityContextHolder.clearContext();
        assertFalse(reclamacaoSecurity.isOwner(1L));
    }

    @Test
    void isAdmin_deveRetornarTrueSeUsuarioForAdmin() {
        setAuthentication("admin@email.com", "ROLE_ADMIN");

        assertTrue(reclamacaoSecurity.isAdmin());
    }

    @Test
    void isAdmin_deveRetornarFalseSeUsuarioNaoForAdmin() {
        setAuthentication("user@email.com", "ROLE_USER");

        assertFalse(reclamacaoSecurity.isAdmin());
    }

    @Test
    void canDelete_deveRetornarTrueSeUsuarioForDono() {
        setAuthentication("dono@email.com");
        when(reclamacaoRepository.existsByIdAndUsuario_Email(1L, "dono@email.com")).thenReturn(true);

        assertTrue(reclamacaoSecurity.canDelete(1L));
    }

    @Test
    void canDelete_deveRetornarTrueSeUsuarioForAdmin() {
        setAuthentication("admin@email.com", "ROLE_ADMIN");

        assertTrue(reclamacaoSecurity.canDelete(1L));
    }

    @Test
    void canDelete_deveRetornarFalseSeUsuarioNaoForDonoNemAdmin() {
        setAuthentication("user@email.com", "ROLE_USER");
        when(reclamacaoRepository.existsByIdAndUsuario_Email(1L, "user@email.com")).thenReturn(false);

        assertFalse(reclamacaoSecurity.canDelete(1L));
    }
}
