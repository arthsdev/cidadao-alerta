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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class ReclamacaoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ReclamacaoRepository reclamacaoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Usuario usuario;
    private Usuario admin;

    @BeforeEach
    void setup() {
        usuario = new Usuario();
        usuario.setNome("Usuario Teste");
        usuario.setEmail("usuario@email.com");
        usuario.setSenha("senha12345A");
        usuario.setAtivo(true);
        usuario.setPapel(Role.ROLE_USER);
        usuarioRepository.save(usuario);

        admin = new Usuario();
        admin.setNome("Admin Teste");
        admin.setEmail("admin@email.com");
        admin.setSenha("senha12345A");
        admin.setAtivo(true);
        admin.setPapel(Role.ROLE_ADMIN);
        usuarioRepository.save(admin);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(usuario, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // --------------------- POST ---------------------
    @Test
    void deveCriarReclamacao() throws Exception {
        var dto = new CadastroReclamacao(
                "Buraco na rua",
                "Há um buraco grande na avenida principal que precisa de reparo",
                CategoriaReclamacao.ASFALTO,
                new Localizacao(-22.5, -45.5)
        );

        mockMvc.perform(post("/reclamacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Buraco na rua"));
    }

    @Test
    void naoDeveCriarReclamacaoComTituloDuplicado() throws Exception {
        reclamacaoRepository.save(new Reclamacao(null, "Buraco",
                "Descrição longa e válida para teste",
                CategoriaReclamacao.ASFALTO, new Localizacao(-22.5, -45.5),
                StatusReclamacao.ABERTA, null, usuario, true, 0L));

        var dto = new CadastroReclamacao(
                "Buraco",
                "Outra descrição longa e válida para teste",
                CategoriaReclamacao.ASFALTO,
                new Localizacao(-22.5, -45.5)
        );

        mockMvc.perform(post("/reclamacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    // --------------------- GET ---------------------
    @Test
    void deveListarReclamacoes() throws Exception {
        reclamacaoRepository.save(new Reclamacao(null, "Teste list",
                "Descrição longa e válida para teste",
                CategoriaReclamacao.SANEAMENTO, new Localizacao(-22.5, -45.5),
                StatusReclamacao.ABERTA, null, usuario, true, 0L));

        mockMvc.perform(get("/reclamacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].titulo").value("Teste list"));
    }

    @Test
    void deveBuscarReclamacaoPorId() throws Exception {
        Reclamacao r = reclamacaoRepository.save(new Reclamacao(null, "Buscar ID",
                "Descrição longa e válida para teste",
                CategoriaReclamacao.ILUMINACAO,
                new Localizacao(-22.5, -45.5), StatusReclamacao.ABERTA, null,
                usuario, true, 0L));

        mockMvc.perform(get("/reclamacoes/{id}", r.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Buscar ID"));
    }

    @Test
    void naoDeveBuscarReclamacaoInativa() throws Exception {
        Reclamacao r = reclamacaoRepository.save(new Reclamacao(null, "Inativa",
                "Descrição longa e válida para teste",
                CategoriaReclamacao.ILUMINACAO,
                new Localizacao(-22.5, -45.5), StatusReclamacao.ABERTA, null,
                usuario, false, 0L));

        mockMvc.perform(get("/reclamacoes/{id}", r.getId()))
                .andExpect(status().isBadRequest());
    }

    // --------------------- PUT ---------------------
    @Test
    void deveAtualizarReclamacao() throws Exception {
        Reclamacao r = reclamacaoRepository.save(new Reclamacao(null, "Rua antiga",
                "Descrição inicial longa e válida para teste",
                CategoriaReclamacao.SEGURANCA,
                new Localizacao(-22.5, -45.5), StatusReclamacao.ABERTA, null,
                usuario, true, 0L));

        var dto = new AtualizacaoReclamacao();
        dto.setTitulo("Rua Atualizada");
        dto.setDescricao("Nova descrição longa e válida para teste");
        dto.setCategoriaReclamacao(CategoriaReclamacao.ILUMINACAO);

        mockMvc.perform(put("/reclamacoes/{id}", r.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Rua Atualizada"))
                .andExpect(jsonPath("$.categoriaReclamacao").value("ILUMINACAO"));
    }

    // --------------------- PATCH ---------------------
    @Test
    void deveAtualizarParcialReclamacao() throws Exception {
        Reclamacao r = reclamacaoRepository.save(new Reclamacao(null, "Rua antiga",
                "Descrição inicial longa e válida para teste",
                CategoriaReclamacao.SEGURANCA,
                new Localizacao(-22.5, -45.5), StatusReclamacao.ABERTA, null,
                usuario, true, 0L));

        var patchDto = new AtualizacaoReclamacao();
        patchDto.setTitulo("Rua parcial");

        mockMvc.perform(patch("/reclamacoes/{id}", r.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Rua parcial"));
    }

    // --------------------- DELETE ---------------------
    @Test
    void ownerDeveInativarReclamacao() throws Exception {
        Reclamacao r = reclamacaoRepository.save(new Reclamacao(null, "Teste delete",
                "Descrição longa e válida para delete",
                CategoriaReclamacao.SANEAMENTO,
                new Localizacao(-22.5, -45.5), StatusReclamacao.ABERTA, null,
                usuario, true, 0L));

        mockMvc.perform(delete("/reclamacoes/{id}", r.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void adminDeveInativarReclamacaoDeOutroUsuario() throws Exception {
        Reclamacao r = reclamacaoRepository.save(new Reclamacao(null, "Reclamacao outro",
                "Descrição longa e válida para admin",
                CategoriaReclamacao.SANEAMENTO,
                new Localizacao(-22.5, -45.5), StatusReclamacao.ABERTA, null,
                usuario, true, 0L));

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(admin, null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(delete("/reclamacoes/{id}", r.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void usuarioNaoPodeInativarReclamacaoDeOutroUsuario() throws Exception {
        Usuario outro = new Usuario();
        outro.setNome("Outro Usuario");
        outro.setEmail("outro@email.com");
        outro.setSenha("senha12345A");
        outro.setAtivo(true);
        outro.setPapel(Role.ROLE_USER);
        usuarioRepository.save(outro);

        Reclamacao r = reclamacaoRepository.save(new Reclamacao(null, "Reclamacao outro",
                "Descrição longa e válida para teste",
                CategoriaReclamacao.SANEAMENTO,
                new Localizacao(-22.5, -45.5), StatusReclamacao.ABERTA, null,
                outro, true, 0L));

        mockMvc.perform(delete("/reclamacoes/{id}", r.getId()))
                .andExpect(status().isForbidden());
    }
}
