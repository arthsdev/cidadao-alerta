package com.artheus.cidadaoalerta.unit.security;

import com.artheus.cidadaoalerta.security.FiltroJwt;
import com.artheus.cidadaoalerta.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FiltroJwtUnitTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private FiltroJwt filtroJwt;

    @BeforeEach
    void limparSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ==== HELPERS ====

    private void mockToken(String token, boolean valido, String email) {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validarToken(token)).thenReturn(valido);
        if (valido && email != null) {
            when(jwtService.getEmailUsuario(token)).thenReturn(email);
        }
    }

    private void mockUsuario(String email) {
        // Usamos lenient para não dar UnnecessaryStubbingException
        lenient().when(userDetailsService.loadUserByUsername(email))
                .thenReturn(new User(email, "senha", Collections.emptyList()));
    }

    // ==== TESTES ====

    @Test
    void dadoTokenValido_quandoAutenticar_entaoSegueCadeia() throws Exception {
        mockToken("tokenValido", true, "teste@email.com");
        mockUsuario("teste@email.com");

        filtroJwt.doFilter(request, response, filterChain);

        verify(userDetailsService).loadUserByUsername("teste@email.com");
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(response);
    }

    @Test
    void dadoTokenInvalido_quandoFiltrar_entaoRetorna401() throws Exception {
        mockToken("tokenInvalido", false, null);

        filtroJwt.doFilter(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido ou expirado");
        verifyNoInteractions(userDetailsService);
        verifyNoInteractions(filterChain);
    }

    @Test
    void dadoUsuarioNaoEncontrado_quandoAutenticar_entaoRetorna401() throws Exception {
        mockToken("tokenNaoExiste", true, "naoExiste@email.com");

        when(userDetailsService.loadUserByUsername("naoExiste@email.com"))
                .thenThrow(new RuntimeException("Usuário não encontrado"));

        filtroJwt.doFilter(request, response, filterChain);

        verify(response).sendError(eq(HttpServletResponse.SC_UNAUTHORIZED), contains("Usuário não encontrado"));
        verifyNoInteractions(filterChain);
    }

    @Test
    void dadoSemToken_quandoFiltrar_entaoSegueCadeia() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filtroJwt.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userDetailsService);
        verifyNoInteractions(response);
    }

    @Test
    void dadoTokenValidoJaAutenticado_quandoFiltrar_entaoNaoReautentica() throws Exception {
        mockToken("tokenValido", true, "teste@email.com");
        mockUsuario("teste@email.com");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        new User("teste@email.com", "senha", Collections.emptyList()),
                        null,
                        Collections.emptyList()
                )
        );

        filtroJwt.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userDetailsService); // não deve tentar carregar novamente
        verifyNoInteractions(response);
    }

    @Test
    void dadoJwtServiceLancaExcecao_quandoFiltrar_entaoRetorna401() throws Exception {
        mockToken("tokenComErro", true, "erro@email.com");

        when(jwtService.getEmailUsuario("tokenComErro"))
                .thenThrow(new RuntimeException("Erro interno"));

        filtroJwt.doFilter(request, response, filterChain);

        verify(response).sendError(eq(HttpServletResponse.SC_UNAUTHORIZED), contains("Erro interno"));
        verifyNoInteractions(filterChain);
    }
}
