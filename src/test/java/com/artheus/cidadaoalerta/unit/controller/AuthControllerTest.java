package com.artheus.cidadaoalerta.unit.controller;

import com.artheus.cidadaoalerta.controller.AuthController;
import com.artheus.cidadaoalerta.dto.RequisicaoLogin;
import com.artheus.cidadaoalerta.dto.RespostaLogin;
import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.model.enums.Role;
import com.artheus.cidadaoalerta.security.JwtService;
import com.artheus.cidadaoalerta.security.UsuarioDetailsService;
import com.artheus.cidadaoalerta.service.EmailService;
import com.artheus.cidadaoalerta.service.ReclamacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UsuarioDetailsService usuarioDetailsService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private RequisicaoLogin requisicaoValida;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();

        requisicaoValida = new RequisicaoLogin("teste@email.com", "senha123");

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Fabiano");
        usuario.setEmail("teste@email.com");
        usuario.setSenha("senha123");
        usuario.setPapel(Role.ROLE_USER);
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    // ==================== TESTES PRINCIPAIS ====================

    @Test
    void deveAutenticarUsuarioComSucesso() throws Exception {
        when(usuarioDetailsService.loadUserByUsername(requisicaoValida.email()))
                .thenReturn(usuario);
        when(jwtService.gerarToken(usuario)).thenReturn("jwt-token");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(requisicaoValida)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Login realizado"))
                .andExpect(jsonPath("$.token").value("jwt-token"));

        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(usuarioDetailsService).loadUserByUsername(requisicaoValida.email());
        verify(jwtService).gerarToken(usuario);
    }

    @Test
    void deveRetornar401ParaCredenciaisInvalidas() throws Exception {
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Senha incorreta"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(requisicaoValida)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Credenciais inválidas"))
                .andExpect(jsonPath("$.token").isEmpty());

        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtService);
    }

    @Test
    void deveRetornar400ParaCamposInvalidos() throws Exception {
        RequisicaoLogin dtoInvalido = new RequisicaoLogin("", "");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dtoInvalido)))
                .andExpect(status().isBadRequest());
    }

    // ==================== TESTES ADICIONAIS ====================

    @Test
    void deveRetornar401QuandoUsuarioNaoEncontrado() throws Exception {
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(usuarioDetailsService.loadUserByUsername(requisicaoValida.email()))
                .thenThrow(new UsernameNotFoundException("Usuário não encontrado"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(requisicaoValida)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Credenciais inválidas"))
                .andExpect(jsonPath("$.token").isEmpty());
    }

    @Test
    void deveGerarTokenComRoleDiferente() throws Exception {
        usuario.setPapel(Role.ROLE_ADMIN); // mudar a role

        when(usuarioDetailsService.loadUserByUsername(requisicaoValida.email()))
                .thenReturn(usuario);
        when(jwtService.gerarToken(usuario)).thenReturn("jwt-token-admin");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(requisicaoValida)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-admin"))
                .andExpect(jsonPath("$.mensagem").value("Login realizado"));
    }

    @Test
    void deveTratarExcecaoGenericaNoAuth() throws Exception {
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Erro inesperado"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(requisicaoValida)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Credenciais inválidas"))
                .andExpect(jsonPath("$.token").isEmpty());
    }

}
