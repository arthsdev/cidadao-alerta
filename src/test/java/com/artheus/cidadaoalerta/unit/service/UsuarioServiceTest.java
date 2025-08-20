package com.artheus.cidadaoalerta.unit.service;

import com.artheus.cidadaoalerta.dto.AtualizacaoUsuario;
import com.artheus.cidadaoalerta.dto.CadastroUsuario;
import com.artheus.cidadaoalerta.dto.DetalhamentoUsuario;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
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
    }

    @Test
    void deveRetornar404QuandoUsuarioNaoExiste() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.atualizarUsuario(1L, atualizacaoUsuario);
        });

        assertEquals("Usuário não encontrado", exception.getMessage());
    }

    @Test
    void deveListarUsuarios() {
        List<Usuario> listaUsuarios = List.of(usuario);
        when(usuarioRepository.findAll()).thenReturn(listaUsuarios);
        when(usuarioMapper.toDetalhamentoDto(usuario)).thenReturn(detalhamentoUsuario);

        List<DetalhamentoUsuario> resultado = usuarioService.listarUsuarios();

        assertEquals(1, resultado.size());
        assertEquals(detalhamentoUsuario.id(), resultado.get(0).id());
    }

    @Test
    void deveBuscarUsuarioPorIdComSucesso() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toDetalhamentoDto(usuario)).thenReturn(detalhamentoUsuario);

        DetalhamentoUsuario resultado = usuarioService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(detalhamentoUsuario.id(), resultado.id());
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaExiste() {
        // dado
        CadastroUsuario cadastro = new CadastroUsuario("Usuario Teste", "teste@email.com", "senha123456");
        Usuario usuarioExistente = new Usuario(1L, "Outro Usuario", "teste@email.com", "senha123456", true, Role.ROLE_USER, new ArrayList<>());

        when(usuarioRepository.findByEmail(cadastro.email())).thenReturn(Optional.of(usuarioExistente));

        assertThrows(RuntimeException.class, () -> usuarioService.cadastrarUsuario(cadastro));
    }

    @Test
    void deveRetornarErroAoAtualizarUsuarioInexistente() {
        AtualizacaoUsuario atualizacao = new AtualizacaoUsuario("Nome Atualizado", "novo@email.com", "novaSenha123");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> usuarioService.atualizarUsuario(1L, atualizacao));
    }

    @Test
    void deveAtualizarUsuarioComMapper() {
        // Dados de atualização
        AtualizacaoUsuario atualizacao = new AtualizacaoUsuario(
                "Nome Atualizado",
                "novo@email.com",
                "novaSenha123"
        );

        // Usuário original
        Usuario usuario = new Usuario(
                1L,
                "Nome Antigo",
                "antigo@email.com",
                "senha123456",
                true,
                Role.ROLE_USER,
                new ArrayList<>()
        );

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode(atualizacao.senha())).thenReturn("senhaCriptografada");

        doAnswer(invocation -> {
            AtualizacaoUsuario dto = invocation.getArgument(0);
            Usuario u = invocation.getArgument(1);
            u.setNome(dto.nome());
            u.setEmail(dto.email());
            u.setSenha(passwordEncoder.encode(dto.senha())); // se quiser já simular encode
            return null;
        }).when(usuarioMapper).updateUsuarioFromDto(atualizacao, usuario);

        usuarioService.atualizarUsuario(1L, atualizacao);

        // Verifica se a atualização ocorreu
        assertEquals("Nome Atualizado", usuario.getNome());
        assertEquals("senhaCriptografada", usuario.getSenha());
    }


}
