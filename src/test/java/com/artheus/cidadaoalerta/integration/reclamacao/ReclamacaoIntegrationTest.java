package com.artheus.cidadaoalerta.integration.reclamacao;

import com.artheus.cidadaoalerta.dto.AtualizacaoReclamacao;
import com.artheus.cidadaoalerta.dto.CadastroReclamacao;
import com.artheus.cidadaoalerta.model.Localizacao;
import com.artheus.cidadaoalerta.model.Reclamacao;
import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.model.enums.CategoriaReclamacao;
import com.artheus.cidadaoalerta.model.enums.Role;
import com.artheus.cidadaoalerta.model.enums.StatusReclamacao;
import com.artheus.cidadaoalerta.repository.ReclamacaoRepository;
import com.artheus.cidadaoalerta.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class ReclamacaoIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ReclamacaoRepository reclamacaoRepository;
    @Autowired private ObjectMapper objectMapper;

    private Usuario usuario;
    private Usuario admin;

    @BeforeEach
    void setup() {
        reclamacaoRepository.deleteAll();
        usuarioRepository.deleteAll();

        usuario = criarUsuario("Usuario Teste", "usuario@email.com", Role.ROLE_USER);
        admin   = criarUsuario("Admin Teste", "admin@email.com", Role.ROLE_ADMIN);
    }

    // --------------------- HELPERS ---------------------
    private Usuario criarUsuario(String nome, String email, Role role) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email);
        u.setSenha("senha12345A");
        u.setAtivo(true);
        u.setPapel(role);
        return usuarioRepository.save(u);
    }

    private Reclamacao criarReclamacao(String titulo, Usuario dono, boolean ativo) {
        return reclamacaoRepository.save(new Reclamacao(
                null,
                titulo,
                "Descrição longa e válida para teste",
                CategoriaReclamacao.ASFALTO,
                new Localizacao(-22.5, -45.5),
                StatusReclamacao.ABERTA,
                null,
                dono,
                ativo,
                0L
        ));
    }

    private CadastroReclamacao novoCadastroDto(String titulo) {
        return new CadastroReclamacao(
                titulo,
                "Descrição longa e válida para teste",
                CategoriaReclamacao.ASFALTO,
                new Localizacao(-22.5, -45.5)
        );
    }

    private AtualizacaoReclamacao novoAtualizacaoDto(String titulo, String descricao, CategoriaReclamacao categoria) {
        var dto = new AtualizacaoReclamacao();
        dto.setTitulo(titulo);
        dto.setDescricao(descricao);
        dto.setCategoriaReclamacao(categoria);
        return dto;
    }

    private String json(Object dto) throws Exception {
        return objectMapper.writeValueAsString(dto);
    }

    // wrappers para requests mais legíveis
    private ResultActions postReclamacao(Object dto, Usuario user) throws Exception {
        return mockMvc.perform(post("/reclamacoes")
                .with(authentication(new UsernamePasswordAuthenticationToken(user, null, List.of(() -> user.getPapel().name()))))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(dto)));
    }

    private ResultActions getReclamacao(Long id, Usuario user) throws Exception {
        return mockMvc.perform(get("/reclamacoes/{id}", id)
                .with(user(user.getEmail()).roles(user.getPapel().name().replace("ROLE_", ""))));
    }

    private ResultActions putReclamacao(Long id, Object dto, Usuario user) throws Exception {
        return mockMvc.perform(put("/reclamacoes/{id}", id)
                .with(user(user.getEmail()).roles(user.getPapel().name().replace("ROLE_", "")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(dto)));
    }

    private ResultActions patchReclamacao(Long id, Object dto, Usuario user) throws Exception {
        return mockMvc.perform(patch("/reclamacoes/{id}", id)
                .with(user(user.getEmail()).roles(user.getPapel().name().replace("ROLE_", "")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(dto)));
    }

    private ResultActions deleteReclamacao(Long id, Usuario user) throws Exception {
        return mockMvc.perform(delete("/reclamacoes/{id}", id)
                .with(user(user.getEmail()).roles(user.getPapel().name().replace("ROLE_", ""))));
    }

    // --------------------- TESTES ---------------------

    @Test
    void deveCriarReclamacao() throws Exception {
        var dto = novoCadastroDto("Buraco na rua");

        postReclamacao(dto, usuario)
                .andExpect(status().isCreated());
    }

    @Test
    void naoDeveCriarReclamacaoComTituloDuplicado() throws Exception {
        criarReclamacao("Buraco", usuario, true);
        var dto = novoCadastroDto("Buraco");

        postReclamacao(dto, usuario)
                .andExpect(status().isConflict());
    }

    @Test
    void deveListarReclamacoes() throws Exception {
        criarReclamacao("Teste list", usuario, true);

        mockMvc.perform(get("/reclamacoes")
                        .with(user(usuario.getEmail()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].titulo").value("Teste list"));
    }

    @Test
    void deveBuscarReclamacaoPorId() throws Exception {
        Reclamacao r = criarReclamacao("Buscar ID", usuario, true);

        getReclamacao(r.getId(), usuario)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Buscar ID"));
    }

    @Test
    void naoDeveBuscarReclamacaoInativa() throws Exception {
        Reclamacao r = criarReclamacao("Inativa", usuario, false);

        getReclamacao(r.getId(), usuario)
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveAtualizarReclamacao() throws Exception {
        Reclamacao r = criarReclamacao("Rua antiga", usuario, true);
        var dto = novoAtualizacaoDto("Rua Atualizada", "Nova descrição válida", CategoriaReclamacao.ILUMINACAO);

        putReclamacao(r.getId(), dto, usuario)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Rua Atualizada"))
                .andExpect(jsonPath("$.categoriaReclamacao").value("ILUMINACAO"));
    }

    @Test
    void deveAtualizarParcialReclamacao() throws Exception {
        Reclamacao r = criarReclamacao("Rua antiga", usuario, true);
        var patchDto = novoAtualizacaoDto("Rua parcial", null, null);

        patchReclamacao(r.getId(), patchDto, usuario)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Rua parcial"));
    }

    @Test
    void ownerDeveInativarReclamacao() throws Exception {
        Reclamacao r = criarReclamacao("Teste delete", usuario, true);

        deleteReclamacao(r.getId(), usuario)
                .andExpect(status().isNoContent());
    }

    @Test
    void adminDeveInativarReclamacaoDeOutroUsuario() throws Exception {
        Reclamacao r = criarReclamacao("Reclamacao outro", usuario, true);

        deleteReclamacao(r.getId(), admin)
                .andExpect(status().isNoContent());
    }

    @Test
    void usuarioNaoPodeInativarReclamacaoDeOutroUsuario() throws Exception {
        Usuario outro = criarUsuario("Outro Usuario", "outro@email.com", Role.ROLE_USER);
        Reclamacao r = criarReclamacao("Reclamacao outro", outro, true);

        deleteReclamacao(r.getId(), usuario)
                .andExpect(status().isForbidden());
    }
}
