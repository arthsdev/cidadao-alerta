package com.artheus.cidadaoalerta.unit.controller;

import com.artheus.cidadaoalerta.controller.ReclamacaoController;
import com.artheus.cidadaoalerta.dto.*;
import com.artheus.cidadaoalerta.model.Localizacao;
import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.model.enums.CategoriaReclamacao;
import com.artheus.cidadaoalerta.model.enums.StatusReclamacao;
import com.artheus.cidadaoalerta.service.ReclamacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReclamacaoControllerTest {

    @Mock
    private ReclamacaoService reclamacaoService;

    @InjectMocks
    private ReclamacaoController reclamacaoController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private CadastroReclamacao cadastroDto;
    private DetalhamentoReclamacao detalhamentoDto;
    private AtualizacaoReclamacao atualizacaoDto;
    private Usuario usuarioLogado;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        usuarioLogado = new Usuario();
        usuarioLogado.setId(1L);
        usuarioLogado.setNome("Fabiano Martins");
        usuarioLogado.setEmail("fabiano@email.com");

        Localizacao localizacao = new Localizacao(10.0, 20.0);

        cadastroDto = new CadastroReclamacao(
                "Rua sem iluminação",
                "A rua está completamente escura à noite, perigo para pedestres",
                CategoriaReclamacao.ILUMINACAO,
                localizacao
        );

        detalhamentoDto = new DetalhamentoReclamacao(
                1L,
                cadastroDto.titulo(),
                cadastroDto.descricao(),
                cadastroDto.categoriaReclamacao(),
                cadastroDto.localizacao(),
                StatusReclamacao.ABERTA,
                LocalDateTime.now(),
                usuarioLogado.getId(),
                usuarioLogado.getNome()
        );

        atualizacaoDto = new AtualizacaoReclamacao();
        atualizacaoDto.setTitulo("Rua iluminada parcialmente");
        atualizacaoDto.setDescricao("Algumas lâmpadas foram instaladas, mas ainda escuro em alguns pontos");
        atualizacaoDto.setCategoriaReclamacao(CategoriaReclamacao.ILUMINACAO);
        atualizacaoDto.setLocalizacao(localizacao);

        mockMvc = MockMvcBuilders
                .standaloneSetup(reclamacaoController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    private void mockSecurityContext() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(usuarioLogado);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    // ===================== TESTES DE SUCESSO =====================

    @Test
    void deveListarReclamacoesComPaginacao() throws Exception {
        DetalhamentoReclamacao reclamacao = new DetalhamentoReclamacao(
                1L,
                "Buraco na rua",
                "Rua tal, centro",
                CategoriaReclamacao.ASFALTO,
                new Localizacao(10.0, 20.0),
                StatusReclamacao.ABERTA,
                LocalDateTime.now(),
                1L,
                "Fabiano Martins"
        );

        Page<DetalhamentoReclamacao> pagina = new PageImpl<>(List.of(reclamacao));
        when(reclamacaoService.listarReclamacoes(any(Pageable.class))).thenReturn(pagina);

        mockMvc.perform(get("/reclamacoes")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "id,asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].titulo").value("Buraco na rua"))
                .andExpect(jsonPath("$.content[0].categoriaReclamacao").value("ASFALTO"))
                .andExpect(jsonPath("$.content[0].statusReclamacao").value("ABERTA"));

        verify(reclamacaoService).listarReclamacoes(any(Pageable.class));
    }

    @Test
    void deveCadastrarReclamacao() throws Exception {
        // Mock do serviço
        doReturn(detalhamentoDto)
                .when(reclamacaoService)
                .cadastrarReclamacao(any(CadastroReclamacao.class), any(Usuario.class));

        // Executa a requisição simulando o usuário logado
        mockMvc.perform(post("/reclamacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(cadastroDto))
                        .requestAttr("principal", usuarioLogado)) // aqui passamos o usuário
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(detalhamentoDto.id()))
                .andExpect(jsonPath("$.titulo").value(detalhamentoDto.titulo()))
                .andExpect(jsonPath("$.descricao").value(detalhamentoDto.descricao()))
                .andExpect(jsonPath("$.categoriaReclamacao").value(detalhamentoDto.categoriaReclamacao().name()))
                .andExpect(jsonPath("$.statusReclamacao").value(detalhamentoDto.statusReclamacao().name()));

        verify(reclamacaoService).cadastrarReclamacao(any(CadastroReclamacao.class), any(Usuario.class));
    }



    @Test
    void deveBuscarReclamacaoPorId() throws Exception {
        when(reclamacaoService.buscarPorId(1L)).thenReturn(detalhamentoDto);

        mockMvc.perform(get("/reclamacoes/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(detalhamentoDto.id()))
                .andExpect(jsonPath("$.titulo").value(detalhamentoDto.titulo()));

        verify(reclamacaoService).buscarPorId(1L);
    }

    @Test
    void deveAtualizarReclamacao() throws Exception {
        when(reclamacaoService.atualizarReclamacao(eq(1L), any(AtualizacaoReclamacao.class)))
                .thenReturn(detalhamentoDto);

        mockMvc.perform(put("/reclamacoes/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(atualizacaoDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(detalhamentoDto.id()))
                .andExpect(jsonPath("$.titulo").value(detalhamentoDto.titulo()));

        verify(reclamacaoService).atualizarReclamacao(eq(1L), any(AtualizacaoReclamacao.class));
    }

    @Test
    void deveDesativarReclamacao() throws Exception {
        doNothing().when(reclamacaoService).inativarReclamacao(1L);

        mockMvc.perform(delete("/reclamacoes/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(reclamacaoService).inativarReclamacao(1L);
    }

    // ===================== TESTES DE ERRO =====================

    @Test
    void deveRetornar404AoBuscarReclamacaoInexistente() throws Exception {
        when(reclamacaoService.buscarPorId(99L))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/reclamacoes/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar404AoAtualizarReclamacaoInexistente() throws Exception {
        when(reclamacaoService.atualizarReclamacao(eq(99L), any(AtualizacaoReclamacao.class)))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));

        mockMvc.perform(put("/reclamacoes/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(atualizacaoDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400ParaCadastroInvalido() throws Exception {
        CadastroReclamacao dtoInvalido = new CadastroReclamacao("", "descricao curta", null, null);

        mockMvc.perform(post("/reclamacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dtoInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar400AoAtualizarReclamacaoDesativada() throws Exception {
        when(reclamacaoService.atualizarReclamacao(eq(1L), any(AtualizacaoReclamacao.class)))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Não é possível atualizar uma reclamação desativada"));

        mockMvc.perform(put("/reclamacoes/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(atualizacaoDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar400AoDesativarReclamacaoJaDesativada() throws Exception {
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Reclamação já está desativada"))
                .when(reclamacaoService).inativarReclamacao(1L);

        mockMvc.perform(delete("/reclamacoes/{id}", 1L))
                .andExpect(status().isBadRequest());
    }
}
