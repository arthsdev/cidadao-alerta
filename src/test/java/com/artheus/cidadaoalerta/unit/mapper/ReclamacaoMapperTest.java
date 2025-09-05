package com.artheus.cidadaoalerta.unit.mapper;

import com.artheus.cidadaoalerta.dto.AtualizacaoReclamacao;
import com.artheus.cidadaoalerta.dto.CadastroReclamacao;
import com.artheus.cidadaoalerta.dto.DetalhamentoReclamacao;
import com.artheus.cidadaoalerta.mapper.ReclamacaoMapper;
import com.artheus.cidadaoalerta.mapper.ReclamacaoMapperImpl;
import com.artheus.cidadaoalerta.model.Localizacao;
import com.artheus.cidadaoalerta.model.Reclamacao;
import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.model.enums.CategoriaReclamacao;
import com.artheus.cidadaoalerta.model.enums.StatusReclamacao;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReclamacaoMapperTest {

    private final ReclamacaoMapper mapper = new ReclamacaoMapperImpl();

    @Test
    void deveMapearCadastroDTOParaEntity() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("João");

        Localizacao localizacao = new Localizacao(-22.0, -45.0);
        CadastroReclamacao dto = new CadastroReclamacao(
                "Buraco na rua",
                "Grande buraco no centro da cidade, precisa de reparo urgente",
                CategoriaReclamacao.ASFALTO,
                localizacao
        );

        Reclamacao entity = mapper.toEntity(dto, usuario);

        assertEquals(dto.titulo(), entity.getTitulo());
        assertEquals(dto.descricao(), entity.getDescricao());
        assertEquals(dto.categoriaReclamacao(), entity.getCategoriaReclamacao());
        assertEquals(dto.localizacao(), entity.getLocalizacao());
        assertEquals(usuario, entity.getUsuario());
        assertEquals(StatusReclamacao.ABERTA, entity.getStatus());
    }

    @Test
    void deveAtualizarEntityComAtualizacaoDTO() {
        Reclamacao entity = new Reclamacao();
        entity.setTitulo("Antigo título");
        entity.setDescricao("Descrição antiga");
        entity.setCategoriaReclamacao(CategoriaReclamacao.ILUMINACAO);
        entity.setLocalizacao(new Localizacao(-22.1, -45.1));

        AtualizacaoReclamacao dto = new AtualizacaoReclamacao();
        dto.setTitulo("Novo título");
        dto.setDescricao("Nova descrição atualizada");
        dto.setCategoriaReclamacao(CategoriaReclamacao.SANEAMENTO);
        dto.setLocalizacao(new Localizacao(-22.2, -45.2));

        mapper.updateReclamacaoFromDto(dto, entity);

        assertEquals(dto.getTitulo(), entity.getTitulo());
        assertEquals(dto.getDescricao(), entity.getDescricao());
        assertEquals(dto.getCategoriaReclamacao(), entity.getCategoriaReclamacao());
        assertEquals(dto.getLocalizacao(), entity.getLocalizacao());
    }

    @Test
    void deveIgnorarCamposNulosAoAtualizarEntity() {
        Reclamacao entity = new Reclamacao();
        entity.setTitulo("Título original");
        entity.setDescricao("Descrição original");
        entity.setCategoriaReclamacao(CategoriaReclamacao.ASFALTO);
        Localizacao localizacaoOriginal = new Localizacao(-22.0, -45.0);
        entity.setLocalizacao(localizacaoOriginal);

        AtualizacaoReclamacao dto = new AtualizacaoReclamacao();
        dto.setTitulo(null);
        dto.setDescricao(null);
        dto.setCategoriaReclamacao(null);
        dto.setLocalizacao(null);

        mapper.updateReclamacaoFromDto(dto, entity);

        assertEquals("Título original", entity.getTitulo());
        assertEquals("Descrição original", entity.getDescricao());
        assertEquals(CategoriaReclamacao.ASFALTO, entity.getCategoriaReclamacao());
        assertSame(localizacaoOriginal, entity.getLocalizacao());
    }

    @Test
    void deveMapearEntityParaDetalhamentoDTO() {
        Usuario usuario = new Usuario();
        usuario.setId(5L);
        usuario.setNome("João");

        Localizacao localizacao = new Localizacao(-22.2, -45.2);
        Reclamacao entity = new Reclamacao();
        entity.setId(1L);
        entity.setTitulo("Buraco grande");
        entity.setDescricao("Buraco enorme próximo à praça");
        entity.setCategoriaReclamacao(CategoriaReclamacao.ASFALTO);
        entity.setLocalizacao(localizacao);
        entity.setStatus(StatusReclamacao.ABERTA);
        entity.setDataCriacao(LocalDateTime.now());
        entity.setUsuario(usuario);

        DetalhamentoReclamacao dto = mapper.toDetalhamentoDto(entity);

        assertEquals(entity.getId(), dto.id());
        assertEquals(entity.getTitulo(), dto.titulo());
        assertEquals(entity.getDescricao(), dto.descricao());
        assertEquals(entity.getCategoriaReclamacao(), dto.categoriaReclamacao());
        assertEquals(entity.getLocalizacao(), dto.localizacao());
        assertEquals(entity.getStatus(), dto.statusReclamacao());
        assertEquals(entity.getDataCriacao(), dto.dataCriacao());
        assertEquals(usuario.getId(), dto.usuarioId());
        assertEquals(usuario.getNome(), dto.nomeUsuario());
    }

    @Test
    void deveMapearEntityComUsuarioNulo() {
        Reclamacao entity = new Reclamacao();
        entity.setId(10L);
        entity.setTitulo("Teste");
        entity.setDescricao("Descrição válida com mais de 20 caracteres");
        entity.setCategoriaReclamacao(CategoriaReclamacao.ASFALTO);
        entity.setLocalizacao(new Localizacao(-22.0, -45.0));
        entity.setStatus(StatusReclamacao.ABERTA);
        entity.setUsuario(null);

        DetalhamentoReclamacao dto = mapper.toDetalhamentoDto(entity);

        assertNull(dto.usuarioId());
        assertNull(dto.nomeUsuario());
        assertEquals(entity.getId(), dto.id());
    }

    @Test
    void deveMapearListaDeEntitiesParaDetalhamentoDTO() {
        Usuario usuario = new Usuario();
        usuario.setId(5L);
        usuario.setNome("João");

        Reclamacao r1 = new Reclamacao();
        r1.setId(1L);
        r1.setTitulo("R1");
        r1.setDescricao("Descrição 1");
        r1.setCategoriaReclamacao(CategoriaReclamacao.ILUMINACAO);
        r1.setStatus(StatusReclamacao.ABERTA);
        r1.setUsuario(usuario);

        Reclamacao r2 = new Reclamacao();
        r2.setId(2L);
        r2.setTitulo("R2");
        r2.setDescricao("Descrição 2");
        r2.setCategoriaReclamacao(CategoriaReclamacao.ASFALTO);
        r2.setStatus(StatusReclamacao.ABERTA);
        r2.setUsuario(usuario);

        List<DetalhamentoReclamacao> dtos = mapper.toDetalhamentoList(List.of(r1, r2));
        assertEquals(2, dtos.size());
        assertEquals("R1", dtos.get(0).titulo());
        assertEquals("R2", dtos.get(1).titulo());
    }

    @Test
    void deveMapearListaVaziaENula() {
        List<DetalhamentoReclamacao> vazia = mapper.toDetalhamentoList(List.of());
        List<DetalhamentoReclamacao> nula = mapper.toDetalhamentoList(null);

        assertTrue(vazia.isEmpty());
        assertNull(nula);
    }

    @Test
    void deveMapearListaComNulls() {
        Usuario usuario = new Usuario();
        usuario.setId(5L);
        usuario.setNome("João");

        Reclamacao r1 = new Reclamacao();
        r1.setId(1L);
        r1.setTitulo("R1");
        r1.setDescricao("Descrição 1");
        r1.setCategoriaReclamacao(CategoriaReclamacao.ILUMINACAO);
        r1.setStatus(StatusReclamacao.ABERTA);
        r1.setUsuario(usuario);

        List<Reclamacao> reclamacoes = new ArrayList<>();
        reclamacoes.add(null);
        reclamacoes.add(r1);

        List<DetalhamentoReclamacao> dtos = mapper.toDetalhamentoList(reclamacoes);

        assertEquals(2, dtos.size());
        assertNull(dtos.get(0)); // primeiro elemento null
        assertEquals("R1", dtos.get(1).titulo());
    }

    @Test
    void deveMapearEntityComCamposNulos() {
        Reclamacao entity = new Reclamacao();
        entity.setId(1L);
        entity.setTitulo(null);
        entity.setDescricao(null);
        entity.setCategoriaReclamacao(null);
        entity.setLocalizacao(null);
        entity.setStatus(null);
        entity.setUsuario(null);

        DetalhamentoReclamacao dto = mapper.toDetalhamentoDto(entity);

        assertEquals(1L, dto.id());
        assertNull(dto.titulo());
        assertNull(dto.descricao());
        assertNull(dto.categoriaReclamacao());
        assertNull(dto.localizacao());
        assertNull(dto.statusReclamacao());
        assertNull(dto.usuarioId());
        assertNull(dto.nomeUsuario());
        assertNull(dto.dataCriacao());
    }
}
