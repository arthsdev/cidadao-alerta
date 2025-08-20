package com.artheus.cidadaoalerta.unit.service;

import com.artheus.cidadaoalerta.dto.AtualizacaoReclamacao;
import com.artheus.cidadaoalerta.dto.CadastroReclamacao;
import com.artheus.cidadaoalerta.dto.DetalhamentoReclamacao;
import com.artheus.cidadaoalerta.mapper.ReclamacaoMapper;
import com.artheus.cidadaoalerta.model.Localizacao;
import com.artheus.cidadaoalerta.model.Reclamacao;
import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.model.enums.CategoriaReclamacao;
import com.artheus.cidadaoalerta.repository.ReclamacaoRepository;
import com.artheus.cidadaoalerta.repository.UsuarioRepository;
import com.artheus.cidadaoalerta.service.ReclamacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReclamacaoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ReclamacaoRepository reclamacaoRepository;

    @Mock
    private ReclamacaoMapper reclamacaoMapper;

    @InjectMocks
    private ReclamacaoService reclamacaoService;

    private Usuario usuario;
    private Reclamacao reclamacao;
    private CadastroReclamacao cadastroDto;
    private AtualizacaoReclamacao atualizacaoDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Fabiano Martins");

        Localizacao localizacao = new Localizacao(-22.422, -45.456);

        cadastroDto = new CadastroReclamacao(
                "Rua escura",
                "A iluminação da rua está apagada há 3 dias",
                CategoriaReclamacao.ILUMINACAO,
                localizacao,
                1L
        );

        reclamacao = new Reclamacao();
        reclamacao.setId(1L);
        reclamacao.setTitulo(cadastroDto.titulo());
        reclamacao.setDescricao(cadastroDto.descricao());
        reclamacao.setCategoriaReclamacao(cadastroDto.categoriaReclamacao());
        reclamacao.setLocalizacao(cadastroDto.localizacao());
        reclamacao.setUsuario(usuario);
        reclamacao.setAtivo(true);

        atualizacaoDto = new AtualizacaoReclamacao();
        atualizacaoDto.setTitulo("Rua escura - atualizada");
        atualizacaoDto.setDescricao("A iluminação da rua continua apagada");
        atualizacaoDto.setCategoriaReclamacao(CategoriaReclamacao.ILUMINACAO);
        atualizacaoDto.setLocalizacao(localizacao);
    }

    @Test
    void testarCadastroReclamacao() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(reclamacaoMapper.toEntity(cadastroDto, usuario)).thenReturn(reclamacao);
        when(reclamacaoRepository.save(reclamacao)).thenReturn(reclamacao);
        when(reclamacaoMapper.toDetalhamentoDto(reclamacao)).thenReturn(mock(DetalhamentoReclamacao.class));

        DetalhamentoReclamacao dto = reclamacaoService.cadastrarReclamacao(cadastroDto);
        assertNotNull(dto);

        verify(usuarioRepository).findById(1L);
        verify(reclamacaoRepository).save(reclamacao);
    }

    @Test
    void testarCadastroReclamacaoComTituloDuplicado() {
        // Configurando o cenário onde já existe uma reclamação com o mesmo título para o mesmo usuário
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(reclamacaoRepository.findByTituloAndUsuarioIdAndAtivoTrue(cadastroDto.titulo(), usuario.getId()))
                .thenReturn(Optional.of(reclamacao));  // A reclamação já existe!

        // Esperamos que o método lance uma ResponseStatusException com HTTP 409 (CONFLICT)
        assertThrows(ResponseStatusException.class, () -> reclamacaoService.cadastrarReclamacao(cadastroDto),
                "Já existe uma reclamação ativa com esse título para este usuário.");
    }

    @Test
    void testarListarReclamacoes() {
        when(reclamacaoRepository.findByAtivoTrue()).thenReturn(Arrays.asList(reclamacao));
        when(reclamacaoMapper.toDetalhamentoDto(reclamacao)).thenReturn(mock(DetalhamentoReclamacao.class));

        List<DetalhamentoReclamacao> lista = reclamacaoService.listarReclamacoes();
        assertEquals(1, lista.size());
    }

    @Test
    void testarBuscarPorId() {
        when(reclamacaoRepository.findById(1L)).thenReturn(Optional.of(reclamacao));
        when(reclamacaoMapper.toDetalhamentoDto(reclamacao)).thenReturn(mock(DetalhamentoReclamacao.class));

        DetalhamentoReclamacao dto = reclamacaoService.buscarPorId(1L);
        assertNotNull(dto);
    }

    @Test
    void testarAtualizarReclamacao() {
        when(reclamacaoRepository.findById(1L)).thenReturn(Optional.of(reclamacao));
        doNothing().when(reclamacaoMapper).updateReclamacaoFromDto(atualizacaoDto, reclamacao);
        when(reclamacaoMapper.toDetalhamentoDto(reclamacao)).thenReturn(mock(DetalhamentoReclamacao.class));

        DetalhamentoReclamacao dto = reclamacaoService.atualizarReclamacao(1L, atualizacaoDto);
        assertNotNull(dto);
        verify(reclamacaoMapper).updateReclamacaoFromDto(atualizacaoDto, reclamacao);
    }

    @Test
    void testarDesativarReclamacao() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(usuario.getEmail()); // retorna o email do usuário dono

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(reclamacaoRepository.findById(1L)).thenReturn(Optional.of(reclamacao));

        reclamacaoService.inativarReclamacao(1L);
        assertFalse(reclamacao.isAtivo());
    }

    @Test
    void buscarPorIdNaoExistente() {
        when(reclamacaoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> reclamacaoService.buscarPorId(99L));
    }
}
