package com.artheus.cidadaoalerta.unit.service;

import com.artheus.cidadaoalerta.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User userDetails;

    // Secret de teste (32 bytes para HS256)
    private final String secret = "12345678901234567890123456789012";
    private final long expiration = 1000 * 60 * 60; // 1 hora

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(secret, expiration);

        userDetails = new User(
                "teste@email.com",
                "senha123",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void deveGerarTokenValido() {
        String token = jwtService.gerarToken(userDetails);
        assertNotNull(token);
        assertTrue(jwtService.validarToken(token));
        assertEquals("teste@email.com", jwtService.getEmailUsuario(token));
        assertEquals("ROLE_USER", jwtService.getRoleUsuario(token));
    }

    @Test
    void validarTokenComTokenInvalidoDeveRetornarFalse() {
        String tokenInvalido = "token-malformado";
        assertFalse(jwtService.validarToken(tokenInvalido));
    }

    @Test
    void getEmailUsuarioComTokenInvalidoDeveLancarException() {
        String tokenInvalido = "token-malformado";
        assertThrows(io.jsonwebtoken.JwtException.class, () -> jwtService.getEmailUsuario(tokenInvalido));
    }

    @Test
    void getRoleUsuarioComTokenInvalidoDeveLancarException() {
        String tokenInvalido = "token-malformado";
        assertThrows(io.jsonwebtoken.JwtException.class, () -> jwtService.getRoleUsuario(tokenInvalido));
    }

    @Test
    void validarTokenComTokenExpiradoDeveRetornarFalse() throws InterruptedException {
        // Cria um JwtService com expiração mínima (1ms)
        JwtService shortExpiryService = new JwtService(secret, 1);
        String token = shortExpiryService.gerarToken(userDetails);
        Thread.sleep(10); // espera o token expirar
        assertFalse(shortExpiryService.validarToken(token));
    }

    @Test
    void gerarTokenComRoleDiferente() {
        User admin = new User(
                "admin@email.com",
                "senha123",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        String token = jwtService.gerarToken(admin);
        assertEquals("ROLE_ADMIN", jwtService.getRoleUsuario(token));
    }

    @Test
    void gerarTokenComUsuarioNuloDeveLancarException() {
        assertThrows(NullPointerException.class, () -> jwtService.gerarToken(null));
    }
}
