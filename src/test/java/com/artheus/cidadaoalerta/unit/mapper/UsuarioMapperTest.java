package com.artheus.cidadaoalerta.unit.mapper;

import com.artheus.cidadaoalerta.dto.AtualizacaoUsuario;
import com.artheus.cidadaoalerta.dto.CadastroUsuario;
import com.artheus.cidadaoalerta.dto.DetalhamentoUsuario;
import com.artheus.cidadaoalerta.mapper.UsuarioMapper;
import com.artheus.cidadaoalerta.mapper.UsuarioMapperImpl;
import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.model.enums.Role;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioMapperTest {

    private final UsuarioMapper mapper = new UsuarioMapperImpl();

    @Test
    void deveMapearCadastroUsuarioParaEntity() {
        CadastroUsuario dto = new CadastroUsuario(
                "João Silva",
                "joao@email.com",
                "senhaSuperSegura"
        );

        Usuario entity = mapper.toEntity(dto);

        assertEquals(dto.nome(), entity.getNome());
        assertEquals(dto.email(), entity.getUsername());
        assertEquals(dto.senha(), entity.getPassword());
        assertEquals(Role.ROLE_USER, entity.getPapel());
        assertTrue(entity.isEnabled());
    }

    @Test
    void deveMapearEntityParaDetalhamentoUsuario() {
        Usuario entity = new Usuario();
        entity.setId(1L);
        entity.setNome("Maria Souza");
        entity.setEmail("maria@email.com");
        entity.setSenha("senha123456");
        entity.setAtivo(true);
        entity.setPapel(Role.ROLE_USER);

        DetalhamentoUsuario dto = mapper.toDetalhamentoDto(entity);

        assertEquals(entity.getId(), dto.id());
        assertEquals(entity.getNome(), dto.nome());
        assertEquals(entity.getUsername(), dto.email());
        assertEquals(entity.isEnabled(), dto.ativo());
        assertEquals(entity.getPapel(), dto.papel());
    }

    @Test
    void deveAtualizarEntityComAtualizacaoUsuario() {
        Usuario entity = new Usuario();
        entity.setNome("Nome Original");
        entity.setEmail("original@email.com");
        entity.setSenha("senhaOriginal");

        AtualizacaoUsuario dto = new AtualizacaoUsuario(
                "Nome Novo",
                "novo@email.com",
                "senhaNova123"
        );

        mapper.updateUsuarioFromDto(dto, entity);

        assertEquals("Nome Novo", entity.getNome());
        assertEquals("novo@email.com", entity.getUsername());
        assertEquals("senhaNova123", entity.getPassword());
    }

    @Test
    void deveIgnorarCamposNulosAoAtualizarEntity() {
        Usuario entity = new Usuario();
        entity.setNome("Nome Original");
        entity.setEmail("original@email.com");
        entity.setSenha("senhaOriginal");

        AtualizacaoUsuario dto = new AtualizacaoUsuario(null, null, null);

        mapper.updateUsuarioFromDto(dto, entity);

        assertEquals("Nome Original", entity.getNome());
        assertEquals("original@email.com", entity.getUsername());
        assertEquals("senhaOriginal", entity.getPassword());
    }

    // ================= CASOS EXTREMOS =================
    @Test
    void deveMapearEntityComCamposNulosParaDetalhamentoUsuario() {
        Usuario entity = new Usuario();
        entity.setId(5L);
        entity.setNome(null);
        entity.setEmail(null);
        entity.setSenha(null);
        entity.setAtivo(false);
        entity.setPapel(null);

        DetalhamentoUsuario dto = mapper.toDetalhamentoDto(entity);

        assertEquals(entity.getId(), dto.id());
        assertNull(dto.nome());
        assertNull(dto.email());
        assertFalse(dto.ativo());
        assertNull(dto.papel());
    }

    @Test
    void deveMapearListaVaziaDeUsuariosParaDetalhamentoDTO() {
        List<Usuario> usuarios = List.of();
        List<DetalhamentoUsuario> dtos = usuarios.stream()
                .map(mapper::toDetalhamentoDto)
                .toList();
        assertTrue(dtos.isEmpty(), "Lista vazia de usuários deve gerar lista vazia de DTOs");
    }

    @Test
    void deveMapearListaNulaDeUsuariosParaDetalhamentoDTO() {
        List<Usuario> usuarios = null;
        List<DetalhamentoUsuario> dtos = usuarios == null ? null : usuarios.stream().map(mapper::toDetalhamentoDto).toList();
        assertNull(dtos, "Lista nula de usuários deve gerar null");
    }
}
