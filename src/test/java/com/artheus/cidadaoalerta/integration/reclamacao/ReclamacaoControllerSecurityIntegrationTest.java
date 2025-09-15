package com.artheus.cidadaoalerta.integration.reclamacao;

import com.artheus.cidadaoalerta.CidadaoAlertaApplication;
import com.artheus.cidadaoalerta.model.Localizacao;
import com.artheus.cidadaoalerta.model.Reclamacao;
import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.model.enums.CategoriaReclamacao;
import com.artheus.cidadaoalerta.model.enums.Role;
import com.artheus.cidadaoalerta.model.enums.StatusReclamacao;
import com.artheus.cidadaoalerta.repository.ReclamacaoRepository;
import com.artheus.cidadaoalerta.repository.UsuarioRepository;
import com.artheus.cidadaoalerta.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = {CidadaoAlertaApplication.class, ReclamacaoControllerSecurityIntegrationTest.TestConfig.class},
        properties = "spring.config.location=classpath:application-test.properties"
)
@AutoConfigureMockMvc
class ReclamacaoControllerSecurityIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public EmailService emailService() {
            return Mockito.mock(EmailService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ReclamacaoRepository reclamacaoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService; // mock injetado

    private Usuario usuarioDono;
    private Usuario usuarioOutro;
    private Reclamacao reclamacao;

    @BeforeEach
    void setUp() {
        reclamacaoRepository.deleteAll();
        usuarioRepository.deleteAll();

        // Configura comportamento do mock para não enviar e-mail
        Mockito.doNothing().when(emailService)
                .enviarEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        usuarioDono = criarUsuario("Usuario Dono", "usuario@email.com", Role.ROLE_USER);
        usuarioOutro = criarUsuario("Outro Usuario", "outro@email.com", Role.ROLE_USER);

        reclamacao = reclamacaoRepository.save(new Reclamacao(
                null,
                "Título Teste",
                "Descrição Teste com mais de 20 caracteres",
                CategoriaReclamacao.ILUMINACAO,
                new Localizacao(10.0, 20.0),
                StatusReclamacao.ABERTA,
                null,
                usuarioDono,
                true,
                null
        ));
    }

    @Test
    void deveRetornarForbidden_quandoUsuarioNaoForDono() throws Exception {
        mockMvc.perform(delete("/reclamacoes/{id}", reclamacao.getId())
                        .with(user(usuarioOutro.getEmail()).roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornarNoContent_quandoUsuarioForDono() throws Exception {
        mockMvc.perform(delete("/reclamacoes/{id}", reclamacao.getId())
                        .with(user(usuarioDono.getEmail()).roles("USER")))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveRetornarNoContent_quandoUsuarioForAdmin() throws Exception {
        Usuario admin = criarUsuario("Administrador", "admin@email.com", Role.ROLE_ADMIN);

        mockMvc.perform(delete("/reclamacoes/{id}", reclamacao.getId())
                        .with(user(admin.getEmail()).roles("ADMIN")))
                .andExpect(status().isNoContent());
    }

    private Usuario criarUsuario(String nome, String email, Role role) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode("123456"));
        usuario.setAtivo(true);
        usuario.setPapel(role);
        return usuarioRepository.save(usuario);
    }
}
