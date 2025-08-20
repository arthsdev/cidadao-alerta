package com.artheus.cidadaoalerta.integration;

import com.artheus.cidadaoalerta.CidadaoAlertaApplication;
import com.artheus.cidadaoalerta.model.Localizacao;
import com.artheus.cidadaoalerta.model.Reclamacao;
import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.model.enums.CategoriaReclamacao;
import com.artheus.cidadaoalerta.model.enums.Role;
import com.artheus.cidadaoalerta.model.enums.StatusReclamacao;
import com.artheus.cidadaoalerta.repository.ReclamacaoRepository;
import com.artheus.cidadaoalerta.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CidadaoAlertaApplication.class, properties = "spring.config.location=classpath:application-test.properties")
@AutoConfigureMockMvc
public class ReclamacaoControllerSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ReclamacaoRepository reclamacaoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario usuarioDono;
    private Usuario usuarioOutro;
    private Reclamacao reclamacao;

    @BeforeEach
    void setUp() {
        reclamacaoRepository.deleteAll();
        usuarioRepository.deleteAll();

        // Usuário dono da reclamação
        usuarioDono = new Usuario();
        usuarioDono.setNome("Usuario Dono");
        usuarioDono.setEmail("usuario@email.com");
        usuarioDono.setSenha(passwordEncoder.encode("senha123"));
        usuarioDono.setAtivo(true);
        usuarioDono.setPapel(Role.ROLE_USER);
        usuarioDono = usuarioRepository.save(usuarioDono);

        // Outro usuário
        usuarioOutro = new Usuario();
        usuarioOutro.setNome("Outro Usuario");
        usuarioOutro.setEmail("outro@email.com");
        usuarioOutro.setSenha(passwordEncoder.encode("senha123"));
        usuarioOutro.setAtivo(true);
        usuarioOutro.setPapel(Role.ROLE_USER);
        usuarioOutro = usuarioRepository.save(usuarioOutro);

        // Criando localização
        Localizacao localizacao = new Localizacao(10.0, 20.0);

        // Reclamação criada pelo usuário dono
        reclamacao = new Reclamacao(
                null, // id
                "Título Teste", // titulo
                "Descrição Teste com mais de 20 caracteres", // descricao
                CategoriaReclamacao.ILUMINACAO, // categoria
                localizacao, // localizacao
                StatusReclamacao.ABERTA, // status
                null, // dataCriacao será gerada automaticamente
                usuarioDono, // usuário dono
                true, // ativo
                null // version
        );
        reclamacao = reclamacaoRepository.save(reclamacao);
    }

    @Test
    void delete_deveRetornarForbidden_seUsuarioNaoForDono() throws Exception {
        mockMvc.perform(delete("/reclamacoes/{id}", reclamacao.getId())
                        .with(user(usuarioOutro.getEmail()).password("senha123456").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_deveRetornarNoContent_seUsuarioForDono() throws Exception {
        mockMvc.perform(delete("/reclamacoes/{id}", reclamacao.getId())
                        .with(user(usuarioDono.getEmail()).password("senha123").roles("USER")))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_deveRetornarNoContent_seUsuarioForAdmin() throws Exception {
        Usuario admin = new Usuario();
        admin.setNome("Administrador");
        admin.setEmail("admin@email.com");
        admin.setSenha(passwordEncoder.encode("admin123"));
        admin.setAtivo(true);
        admin.setPapel(Role.ROLE_ADMIN);
        admin = usuarioRepository.save(admin);

        mockMvc.perform(delete("/reclamacoes/{id}", reclamacao.getId())
                        .with(user(admin.getEmail()).password("admin123").roles("ADMIN")))
                .andExpect(status().isNoContent());
    }
}
