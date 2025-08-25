package com.artheus.cidadaoalerta.unit.service;

import com.artheus.cidadaoalerta.dto.AtualizacaoReclamacao;
import com.artheus.cidadaoalerta.dto.CadastroReclamacao;
import com.artheus.cidadaoalerta.dto.DetalhamentoReclamacao;
import com.artheus.cidadaoalerta.dto.ReclamacaoPageResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

        // Usuário
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Fabiano Martins");
        usuario.setEmail("fabiano@email.com");

        // Localização
        Localizacao localizacao = new Localizacao(-22.422, -45.456);

        // DTO de cadastro
        cadastroDto = new CadastroReclamacao(
                "Rua escura",
                "A iluminação da rua está apagada há 3 dias",
                CategoriaReclamacao.ILUMINACAO,
                localizacao
        );

        // Reclamacao
        reclamacao = new Reclamacao();
        reclamacao.setId(1L);
        reclamacao.setTitulo(cadastroDto.titulo());
        reclamacao.setDescricao(cadastroDto.descricao());
        reclamacao.setCategoriaReclamacao(cadastroDto.categoriaReclamacao());
        reclamacao.setLocalizacao(cadastroDto.localizacao());
        reclamacao.setUsuario(usuario);
        reclamacao.setAtivo(true);

        // DTO de atualização
        atualizacaoDto = new AtualizacaoReclamacao();
        atualizacaoDto.setTitulo("Rua escura - atualizada");
        atualizacaoDto.setDescricao("A iluminação da rua continua apagada");
        atualizacaoDto.setCategoriaReclamacao(CategoriaReclamacao.ILUMINACAO);
        atualizacaoDto.setLocalizacao(localizacao);
    }

    // ===================== TESTES DE CADASTRO =====================

    @Test
    void deveCadastrarReclamacaoComSucesso() {
        when(reclamacaoRepository.findByTituloAndUsuarioIdAndAtivoTrue(cadastroDto.titulo(), usuario.getId()))
                .thenReturn(Optional.empty());
        when(reclamacaoMapper.toEntity(cadastroDto, usuario)).thenReturn(reclamacao);
        when(reclamacaoRepository.save(reclamacao)).thenReturn(reclamacao);
        when(reclamacaoMapper.toDetalhamentoDto(reclamacao)).thenReturn(mock(DetalhamentoReclamacao.class));

        DetalhamentoReclamacao dto = reclamacaoService.cadastrarReclamacao(cadastroDto, usuario);

        assertNotNull(dto);
        verify(reclamacaoRepository).save(reclamacao);
    }

    @Test
    void deveLancarErroAoCadastrarReclamacaoComTituloDuplicado() {
        when(reclamacaoRepository.findByTituloAndUsuarioIdAndAtivoTrue(cadastroDto.titulo(), usuario.getId()))
                .thenReturn(Optional.of(reclamacao));

        assertThrows(ResponseStatusException.class,
                () -> reclamacaoService.cadastrarReclamacao(cadastroDto, usuario));
    }

    // ===================== TESTES DE LISTAGEM =====================

    @Test
    void deveListarReclamacoesAtivas() {
        Pageable pageable = PageRequest.of(0, 10);

        when(reclamacaoRepository.findByAtivoTrue(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(reclamacao)));

        when(reclamacaoMapper.toDetalhamentoDto(reclamacao))
                .thenReturn(mock(DetalhamentoReclamacao.class));

        ReclamacaoPageResponse<DetalhamentoReclamacao> response = reclamacaoService.listarReclamacoes(pageable);

        List<DetalhamentoReclamacao> lista = response.content(); // pega a lista de reclamações

        assertEquals(1, lista.size());
        verify(reclamacaoRepository).findByAtivoTrue(any(Pageable.class));
    }


    // ===================== TESTES DE BUSCA =====================

    @Test
    void deveBuscarReclamacaoPorIdComSucesso() {
        when(reclamacaoRepository.findById(1L)).thenReturn(Optional.of(reclamacao));
        when(reclamacaoMapper.toDetalhamentoDto(reclamacao)).thenReturn(mock(DetalhamentoReclamacao.class));

        DetalhamentoReclamacao dto = reclamacaoService.buscarPorId(1L);

        assertNotNull(dto);
    }

    @Test
    void deveLancarErroAoBuscarReclamacaoInexistente() {
        when(reclamacaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> reclamacaoService.buscarPorId(99L));
    }

    // ===================== TESTES DE ATUALIZAÇÃO =====================

    @Test
    void deveAtualizarReclamacaoComSucesso() {
        when(reclamacaoRepository.findById(1L)).thenReturn(Optional.of(reclamacao));
        doNothing().when(reclamacaoMapper).updateReclamacaoFromDto(atualizacaoDto, reclamacao);
        when(reclamacaoMapper.toDetalhamentoDto(reclamacao)).thenReturn(mock(DetalhamentoReclamacao.class));

        DetalhamentoReclamacao dto = reclamacaoService.atualizarReclamacao(1L, atualizacaoDto);

        assertNotNull(dto);
        verify(reclamacaoMapper).updateReclamacaoFromDto(atualizacaoDto, reclamacao);
    }

    // ===================== TESTES DE DESATIVAÇÃO =====================

    @Test
    void deveInativarReclamacaoComSucesso() {
        // Mock do UserDetails com email do usuário
        UserDetails userDetails = User.withUsername(usuario.getEmail())
                .password("senha")
                .roles("USER")
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(usuario.getEmail()); // importante: getName() deve retornar o email do usuário

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(reclamacaoRepository.findById(1L)).thenReturn(Optional.of(reclamacao));
        when(reclamacaoRepository.save(any(Reclamacao.class))).thenReturn(reclamacao);

        // Executa
        reclamacaoService.inativarReclamacao(1L);

        // Verifica
        assertFalse(reclamacao.isAtivo());
        verify(reclamacaoRepository).findById(1L);
        verify(reclamacaoRepository).save(reclamacao);
    }

}
