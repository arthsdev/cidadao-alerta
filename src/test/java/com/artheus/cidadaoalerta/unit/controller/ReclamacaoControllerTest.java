package com.artheus.cidadaoalerta.unit.controller;

import com.artheus.cidadaoalerta.controller.ReclamacaoController;
import com.artheus.cidadaoalerta.dto.AtualizacaoReclamacao;
import com.artheus.cidadaoalerta.dto.CadastroReclamacao;
import com.artheus.cidadaoalerta.dto.DetalhamentoReclamacao;
import com.artheus.cidadaoalerta.model.Localizacao;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reclamacaoController).build();
        objectMapper = new ObjectMapper();

        Localizacao localizacao = new Localizacao();
        localizacao.setLatitude(10.0);
        localizacao.setLongitude(20.0);

        cadastroDto = new CadastroReclamacao(
                "Rua sem iluminação",
                "A rua está completamente escura à noite, perigo para pedestres",
                CategoriaReclamacao.ILUMINACAO,
                localizacao,
                1L
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
                "Fabiano"
        );

        atualizacaoDto = new AtualizacaoReclamacao();
        atualizacaoDto.setTitulo("Rua iluminada parcialmente");
        atualizacaoDto.setDescricao("Algumas lâmpadas foram instaladas, mas ainda escuro em alguns pontos");
        atualizacaoDto.setCategoriaReclamacao(CategoriaReclamacao.ILUMINACAO);
        atualizacaoDto.setLocalizacao(localizacao);
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    // ===================== TESTES DE SUCESSO =====================

    @Test
    void deveCadastrarReclamacao() throws Exception {
        when(reclamacaoService.cadastrarReclamacao(any(CadastroReclamacao.class)))
                .thenReturn(detalhamentoDto);

        mockMvc.perform(post("/reclamacoes")
                        .contentType("application/json")
                        .content(toJson(cadastroDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(detalhamentoDto.id()))
                .andExpect(jsonPath("$.titulo").value(detalhamentoDto.titulo()))
                .andExpect(jsonPath("$.descricao").value(detalhamentoDto.descricao()))
                .andExpect(jsonPath("$.categoriaReclamacao").value(detalhamentoDto.categoriaReclamacao().name()))
                .andExpect(jsonPath("$.statusReclamacao").value(detalhamentoDto.statusReclamacao().name()));

        verify(reclamacaoService).cadastrarReclamacao(any(CadastroReclamacao.class));
    }

    @Test
    void deveListarReclamacoes() throws Exception {
        when(reclamacaoService.listarReclamacoes()).thenReturn(List.of(detalhamentoDto));

        mockMvc.perform(get("/reclamacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(detalhamentoDto.id()))
                .andExpect(jsonPath("$[0].titulo").value(detalhamentoDto.titulo()));

        verify(reclamacaoService).listarReclamacoes();
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
                        .contentType("application/json")
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
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/reclamacoes/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar404AoAtualizarReclamacaoInexistente() throws Exception {
        when(reclamacaoService.atualizarReclamacao(eq(99L), any(AtualizacaoReclamacao.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(put("/reclamacoes/{id}", 99L)
                        .contentType("application/json")
                        .content(toJson(atualizacaoDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRetornar400ParaCadastroInvalido() throws Exception {
        CadastroReclamacao dtoInvalido = new CadastroReclamacao(
                "",
                "descricao curta",
                null,
                null,
                null
        );

        mockMvc.perform(post("/reclamacoes")
                        .contentType("application/json")
                        .content(toJson(dtoInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar400AoAtualizarReclamacaoDesativada() throws Exception {
        when(reclamacaoService.atualizarReclamacao(eq(1L), any(AtualizacaoReclamacao.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível atualizar uma reclamação desativada"));

        mockMvc.perform(put("/reclamacoes/{id}", 1L)
                        .contentType("application/json")
                        .content(toJson(atualizacaoDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar400AoDesativarReclamacaoJaDesativada() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reclamação já está desativada"))
                .when(reclamacaoService).inativarReclamacao(1L);

        mockMvc.perform(delete("/reclamacoes/{id}", 1L))
                .andExpect(status().isBadRequest());
    }
}
