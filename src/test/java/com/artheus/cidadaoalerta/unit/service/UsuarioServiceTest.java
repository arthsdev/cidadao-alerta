package com.artheus.cidadaoalerta.unit.service;

import com.artheus.cidadaoalerta.dto.AtualizacaoUsuario;
import com.artheus.cidadaoalerta.dto.CadastroUsuario;
import com.artheus.cidadaoalerta.dto.DetalhamentoUsuario;
import com.artheus.cidadaoalerta.event.UsuarioEvent;
import com.artheus.cidadaoalerta.mapper.UsuarioMapper;
import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.model.enums.Role;
import com.artheus.cidadaoalerta.repository.UsuarioRepository;
import com.artheus.cidadaoalerta.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;
    private CadastroUsuario cadastroUsuario;
    private AtualizacaoUsuario atualizacaoUsuario;
    private DetalhamentoUsuario detalhamentoUsuario;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        usuario = new Usuario(1L, "Fabiano Augusto", "fabiano@email.com", "senha123456", true, Role.ROLE_USER, List.of());
        cadastroUsuario = new CadastroUsuario("Fabiano Augusto", "fabiano@email.com", "senha123");
        atualizacaoUsuario = new AtualizacaoUsuario("Fabiano Augusto Atualizado", "fabiano@email.com", "novaSenha123");
        detalhamentoUsuario = new DetalhamentoUsuario(1L, "Fabiano Augusto", "fabiano@email.com", true, Role.ROLE_USER);
    }

    @Test
    void deveCadastrarUsuarioComSucesso() {
        when(usuarioMapper.toEntity(cadastroUsuario)).thenReturn(usuario);
        when(passwordEncoder.encode(usuario.getSenha())).thenReturn(usuario.getSenha());
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(usuarioMapper.toDetalhamentoDto(usuario)).thenReturn(detalhamentoUsuario);

        DetalhamentoUsuario resultado = usuarioService.cadastrarUsuario(cadastroUsuario);

        assertNotNull(resultado);
        assertEquals(detalhamentoUsuario.id(), resultado.id());
        verify(usuarioRepository).save(usuario);
        verify(eventPublisher, times(1)).publishEvent(any(UsuarioEvent.class));
    }

    @Test
    void deveAtualizarUsuarioComSucesso() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        doNothing().when(usuarioMapper).updateUsuarioFromDto(atualizacaoUsuario, usuario);
        when(passwordEncoder.encode(usuario.getSenha())).thenReturn("novaSenha123");
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(usuarioMapper.toDetalhamentoDto(usuario)).thenReturn(detalhamentoUsuario);

        DetalhamentoUsuario resultado = usuarioService.atualizarUsuario(1L, atualizacaoUsuario);

        assertNotNull(resultado);
        verify(usuarioMapper).updateUsuarioFromDto(atualizacaoUsuario, usuario);
        verify(usuarioRepository).save(usuario);
        verify(eventPublisher, times(1)).publishEvent(any(UsuarioEvent.class));
    }

    @Test
    void deveRetornar404QuandoUsuarioNaoExiste() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> usuarioService.atualizarUsuario(1L, atualizacaoUsuario));
        assertEquals("Usuário não encontrado", exception.getMessage());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void deveListarUsuarios() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        when(usuarioMapper.toDetalhamentoDto(usuario)).thenReturn(detalhamentoUsuario);

        var resultado = usuarioService.listarUsuarios();

        assertEquals(1, resultado.size());
        assertEquals(detalhamentoUsuario.id(), resultado.get(0).id());
    }

    @Test
    void deveBuscarUsuarioPorIdComSucesso() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toDetalhamentoDto(usuario)).thenReturn(detalhamentoUsuario);

        var resultado = usuarioService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(detalhamentoUsuario.id(), resultado.id());
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaExiste() {
        CadastroUsuario cadastro = new CadastroUsuario("Usuario Teste", "teste@email.com", "senha123456");
        Usuario usuarioExistente = new Usuario(1L, "Outro Usuario", "teste@email.com", "senha123456", true, Role.ROLE_USER, List.of());

        when(usuarioRepository.findByEmail(cadastro.email())).thenReturn(Optional.of(usuarioExistente));

        assertThrows(RuntimeException.class, () -> usuarioService.cadastrarUsuario(cadastro));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void naoDeveReHasharSenhaQuandoSenhaNaoForInformada() {
        Usuario usuarioExistente = new Usuario(1L, "Fulano", "fulano@email.com", "senhaHashExistente", true, Role.ROLE_USER, List.of());

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));

        AtualizacaoUsuario dto = new AtualizacaoUsuario("Fulano Atualizado", "novo@email.com", null);
        usuarioService.atualizarUsuario(1L, dto);

        assertEquals("senhaHashExistente", usuarioExistente.getSenha());
        verify(passwordEncoder, never()).encode(anyString());
        verify(eventPublisher, times(1)).publishEvent(any(UsuarioEvent.class));
    }

    @Test
    void deveReHasharSenhaQuandoInformada() {
        Usuario usuarioExistente = new Usuario(1L, "Fulano", "fulano@email.com", "senhaHashExistente", true, Role.ROLE_USER, List.of());

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioExistente));
        when(passwordEncoder.encode("novaSenha123")).thenReturn("novaSenhaHash");

        AtualizacaoUsuario dto = new AtualizacaoUsuario("Fulano Atualizado", "novo@email.com", "novaSenha123");
        usuarioService.atualizarUsuario(1L, dto);

        assertEquals("novaSenhaHash", usuarioExistente.getSenha());
        verify(passwordEncoder, times(1)).encode("novaSenha123");
        verify(eventPublisher, times(1)).publishEvent(any(UsuarioEvent.class));
    }
}
