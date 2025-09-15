package com.artheus.cidadaoalerta.unit.controller;

import com.artheus.cidadaoalerta.controller.ReclamacaoController;
import com.artheus.cidadaoalerta.dto.AtualizacaoReclamacao;
import com.artheus.cidadaoalerta.dto.CadastroReclamacao;
import com.artheus.cidadaoalerta.dto.DetalhamentoReclamacao;
import com.artheus.cidadaoalerta.dto.ReclamacaoPageResponse;
import com.artheus.cidadaoalerta.model.Localizacao;
import com.artheus.cidadaoalerta.model.enums.CategoriaReclamacao;
import com.artheus.cidadaoalerta.model.enums.StatusReclamacao;
import com.artheus.cidadaoalerta.service.ReclamacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

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
                1L,
                "Fabiano Martins"
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

    // ===================== TESTES DE SUCESSO =====================

    @Test
    void deveListarReclamacoesComPaginacao() throws Exception {
        ReclamacaoPageResponse<DetalhamentoReclamacao> response =
                new ReclamacaoPageResponse<>(List.of(detalhamentoDto), 0, 10, 1, 1, true);

        when(reclamacaoService.listarReclamacoes(any(Pageable.class))).thenReturn(response);

        mockMvc.perform(get("/reclamacoes")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "id,asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].titulo").value("Rua sem iluminação"));

        verify(reclamacaoService).listarReclamacoes(any(Pageable.class));
    }

    @Test
    void deveCadastrarReclamacao() throws Exception {
        when(reclamacaoService.cadastrarReclamacao(any(CadastroReclamacao.class)))
                .thenReturn(detalhamentoDto);

        mockMvc.perform(post("/reclamacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(cadastroDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(detalhamentoDto.id()))
                .andExpect(jsonPath("$.titulo").value(detalhamentoDto.titulo()));

        verify(reclamacaoService).cadastrarReclamacao(any(CadastroReclamacao.class));
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
        when(reclamacaoService.atualizarReclamacao(any(Long.class), any(AtualizacaoReclamacao.class)))
                .thenReturn(detalhamentoDto);

        mockMvc.perform(put("/reclamacoes/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(atualizacaoDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(detalhamentoDto.id()))
                .andExpect(jsonPath("$.titulo").value(detalhamentoDto.titulo()));

        verify(reclamacaoService).atualizarReclamacao(any(Long.class), any(AtualizacaoReclamacao.class));
    }

    @Test
    void deveDesativarReclamacao() throws Exception {
        doNothing().when(reclamacaoService).inativarReclamacao(1L);

        mockMvc.perform(delete("/reclamacoes/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(reclamacaoService).inativarReclamacao(1L);
    }

    @Test
    void deveCobrirTodosCaminhosDeDataInicioEDataFim() throws Exception {
        // Cenários de teste:
        // 1. Ambos null
        mockMvc.perform(get("/reclamacoes")
                        .param("dataInicio", "")
                        .param("dataFim", "")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        // 2. dataInicio preenchida, dataFim null
        mockMvc.perform(get("/reclamacoes")
                        .param("dataInicio", "2025-01-01")
                        .param("dataFim", "")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        // 3. dataInicio null, dataFim preenchida
        mockMvc.perform(get("/reclamacoes")
                        .param("dataInicio", "")
                        .param("dataFim", "2025-01-31")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        // 4. Ambos preenchidos
        mockMvc.perform(get("/reclamacoes")
                        .param("dataInicio", "2025-01-01")
                        .param("dataFim", "2025-01-31")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
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
    void deveRetornar400ParaCadastroInvalido() throws Exception {
        CadastroReclamacao dtoInvalido = new CadastroReclamacao("", "descricao curta", null, null);

        mockMvc.perform(post("/reclamacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dtoInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar400AoAtualizarReclamacaoInvalida() throws Exception {
        AtualizacaoReclamacao dtoInvalido = new AtualizacaoReclamacao();
        dtoInvalido.setTitulo("");
        dtoInvalido.setDescricao("");
        dtoInvalido.setCategoriaReclamacao(null);

        mockMvc.perform(put("/reclamacoes/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dtoInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar404AoDeletarReclamacaoInexistente() throws Exception {
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND))
                .when(reclamacaoService).inativarReclamacao(99L);

        mockMvc.perform(delete("/reclamacoes/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveNormalizarParametrosDePaginacaoInvalidos() throws Exception {
        ReclamacaoPageResponse<DetalhamentoReclamacao> fakePage = new ReclamacaoPageResponse<>(
                List.of(), 0, 10, 0L, 0, true
        );

        // Forçar o tipo do any() para Pageable
        when(reclamacaoService.listarReclamacoes(ArgumentMatchers.<Pageable>any()))
                .thenReturn(fakePage);

        mockMvc.perform(get("/reclamacoes")
                        .param("page", "-1")
                        .param("size", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10));
    }


}
