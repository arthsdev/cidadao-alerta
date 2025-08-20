package com.artheus.cidadaoalerta.unit.controller;

import com.artheus.cidadaoalerta.controller.UsuarioController;
import com.artheus.cidadaoalerta.dto.AtualizacaoUsuario;
import com.artheus.cidadaoalerta.dto.CadastroUsuario;
import com.artheus.cidadaoalerta.dto.DetalhamentoUsuario;
import com.artheus.cidadaoalerta.mapper.UsuarioMapper;
import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.model.enums.Role;
import com.artheus.cidadaoalerta.security.UsuarioDetails;
import com.artheus.cidadaoalerta.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @InjectMocks
    private UsuarioController usuarioController;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private Authentication authentication;

    @Mock
    private UsuarioDetails usuarioDetails;

    @BeforeEach
    void setup() {
        // Configura um request mock para o ServletUriComponentsBuilder
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void deveCadastrarUsuario() {
        CadastroUsuario dto = new CadastroUsuario("Fulano Teste", "fulano@email.com", "senha123456");
        DetalhamentoUsuario response = new DetalhamentoUsuario(1L, "Fulano Teste", "fulano@email.com", true, Role.ROLE_USER);

        when(usuarioService.cadastrarUsuario(dto)).thenReturn(response);

        ResponseEntity<DetalhamentoUsuario> resultado = usuarioController.cadastrarUsuario(dto);

        assertEquals(HttpStatus.CREATED, resultado.getStatusCode());
        assertEquals(response, resultado.getBody());
        assertNotNull(resultado.getHeaders().getLocation());
        verify(usuarioService).cadastrarUsuario(dto);
    }

    @Test
    void deveListarUsuarios() {
        List<DetalhamentoUsuario> usuarios = List.of(
                new DetalhamentoUsuario(1L, "Fulano", "fulano@email.com", true, Role.ROLE_USER)
        );

        when(usuarioService.listarUsuarios()).thenReturn(usuarios);

        ResponseEntity<List<DetalhamentoUsuario>> resultado = usuarioController.listarUsuarios();

        assertEquals(HttpStatus.OK, resultado.getStatusCode());
        assertEquals(usuarios, resultado.getBody());
        verify(usuarioService).listarUsuarios();
    }

    @Test
    void deveBuscarUsuarioPorId() {
        DetalhamentoUsuario usuario = new DetalhamentoUsuario(1L, "Fulano", "fulano@email.com", true, Role.ROLE_USER);
        when(usuarioService.buscarPorId(1L)).thenReturn(usuario);

        ResponseEntity<DetalhamentoUsuario> resultado = usuarioController.buscarUsuarioPorId(1L);

        assertEquals(HttpStatus.OK, resultado.getStatusCode());
        assertEquals(usuario, resultado.getBody());
        verify(usuarioService).buscarPorId(1L);
    }

    @Test
    void deveAtualizarUsuario() {
        AtualizacaoUsuario dto = new AtualizacaoUsuario("Novo Nome", "novo@email.com", "novasenha123");
        DetalhamentoUsuario usuarioAtualizado = new DetalhamentoUsuario(1L, "Novo Nome", "novo@email.com", true, Role.ROLE_USER);

        when(usuarioService.atualizarUsuario(1L, dto)).thenReturn(usuarioAtualizado);

        ResponseEntity<DetalhamentoUsuario> resultado = usuarioController.atualizarUsuario(1L, dto);

        assertEquals(HttpStatus.OK, resultado.getStatusCode());
        assertEquals(usuarioAtualizado, resultado.getBody());
        verify(usuarioService).atualizarUsuario(1L, dto);
    }

    @Test
    void deveInativarUsuario() {
        ResponseEntity<Void> resultado = usuarioController.inativarUsuario(1L);

        assertEquals(HttpStatus.NO_CONTENT, resultado.getStatusCode());
        verify(usuarioService).desativarUsuario(1L);
    }

    @Test
    void deveBuscarUsuarioLogado() {
        Usuario usuario = new Usuario(1L, "Fulano", "fulano@email.com", "senha123456", true, Role.ROLE_USER, new ArrayList<>());
        DetalhamentoUsuario dto = new DetalhamentoUsuario(1L, "Fulano", "fulano@email.com", true, Role.ROLE_USER);

        when(authentication.getPrincipal()).thenReturn(usuarioDetails);
        when(usuarioDetails.getUsuario()).thenReturn(usuario);
        when(usuarioMapper.toDetalhamentoDto(usuario)).thenReturn(dto);

        ResponseEntity<DetalhamentoUsuario> resultado = usuarioController.buscarUsuarioLogado(authentication);

        assertEquals(HttpStatus.OK, resultado.getStatusCode());
        assertEquals(dto, resultado.getBody());
        verify(usuarioMapper).toDetalhamentoDto(usuario);
    }
}
