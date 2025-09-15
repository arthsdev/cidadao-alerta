package com.artheus.cidadaoalerta.integration.security;

import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.model.enums.Role;
import com.artheus.cidadaoalerta.repository.UsuarioRepository;
import com.artheus.cidadaoalerta.security.JwtService;
import com.artheus.cidadaoalerta.service.EmailService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class FiltroJwtIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public EmailService emailService() {
            // EmailService fake para evitar envio real
            return new EmailService(Mockito.mock(JavaMailSender.class));
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String tokenUser;
    private String tokenAdmin;

    @BeforeAll
    void setupAll() {
        Usuario usuario = criarUsuario("TesteUsuario", "teste@teste.com", "12345678", Role.ROLE_USER);
        usuarioRepository.save(usuario);
        tokenUser = jwtService.gerarToken(usuario);

        Usuario admin = criarUsuario("AdminTeste", "admin@teste.com", "12345678", Role.ROLE_ADMIN);
        usuarioRepository.save(admin);
        tokenAdmin = jwtService.gerarToken(admin);
    }

    private Usuario criarUsuario(String nome, String email, String senha, Role role) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email);
        u.setSenha(passwordEncoder.encode(senha));
        u.setPapel(role);
        u.setAtivo(true);
        return u;
    }

    @Test
    void devePermitirAcessoComTokenValido() throws Exception {
        mockMvc.perform(get("/reclamacoes")
                        .header("Authorization", "Bearer " + tokenUser))
                .andExpect(status().isOk());
    }

    @Test
    void deveNegarAcessoComTokenInvalido() throws Exception {
        mockMvc.perform(get("/reclamacoes")
                        .header("Authorization", "Bearer tokenInvalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveNegarAcessoSemToken() throws Exception {
        mockMvc.perform(get("/reclamacoes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void devePermitirAcessoRotasPublicasSemToken() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"UsuarioTeste2","email":"teste2@teste.com","senha":"1234567890"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void deveNegarAcessoExportSemPermissao() throws Exception {
        mockMvc.perform(get("/reclamacoes/export")
                        .header("Authorization", "Bearer " + tokenUser))
                .andExpect(status().isForbidden());
    }

    @Test
    void devePermitirAcessoExportComRoleAdmin() throws Exception {
        mockMvc.perform(get("/reclamacoes/export")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
    }
}
