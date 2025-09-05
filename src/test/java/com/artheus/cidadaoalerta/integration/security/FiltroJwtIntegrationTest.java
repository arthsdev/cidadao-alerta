package com.artheus.cidadaoalerta.integration.security;

import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.model.enums.Role;
import com.artheus.cidadaoalerta.repository.UsuarioRepository;
import com.artheus.cidadaoalerta.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FiltroJwtIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String token; // Token para ROLE_USER
    private Usuario usuario;

    @BeforeEach
    void setup() {
        usuarioRepository.deleteAll();

        // Cria usuário com ROLE_USER
        usuario = criarUsuario("TesteUsuario", "teste@teste.com", "12345678", Role.ROLE_USER);
        usuario = usuarioRepository.save(usuario);

        token = jwtService.gerarToken(usuario); // gera token JWT
    }

    private Usuario criarUsuario(String nome, String email, String senha, Role role) {
        final Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email);
        u.setSenha(passwordEncoder.encode(senha));
        u.setPapel(role);
        u.setAtivo(true);
        return u;
    }

    @Test
    void deveAutenticarUsuarioComTokenValido() throws Exception {
        mockMvc.perform(get("/reclamacoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void deveRetornar401ComTokenInvalido() throws Exception {
        mockMvc.perform(get("/reclamacoes")
                        .header("Authorization", "Bearer tokenInvalido")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar401SemToken() throws Exception {
        mockMvc.perform(get("/reclamacoes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void devePermitirAcessoRotasPublicasSemToken() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"UsuarioTeste","email":"teste2@teste.com","senha":"1234567890"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void deveRetornar401SeUsuarioDoTokenNaoExistir() throws Exception {
        UserDetails fakeUserDetails = org.springframework.security.core.userdetails.User
                .withUsername("naoexiste@teste.com")
                .password("senhaFake")
                .roles("USER")
                .build();

        final String tokenFake = jwtService.gerarToken(fakeUserDetails);

        mockMvc.perform(get("/reclamacoes")
                        .header("Authorization", "Bearer " + tokenFake)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "teste@teste.com", roles = {"USER"}) // Usuário comum, sem ROLE_ADMIN
    void deveBloquearAcessoUsuarioSemRoleAdmin() throws Exception {
        mockMvc.perform(get("/reclamacoes/export")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()); // Espera 403 Forbidden
    }


    @Test
    void devePermitirAcessoUsuarioComRoleAdmin() throws Exception {
        // Cria usuário ADMIN
        Usuario admin = criarUsuario("AdminTeste", "admin@teste.com", "1234567890", Role.ROLE_ADMIN);
        usuarioRepository.save(admin);

        String tokenAdmin = jwtService.gerarToken(admin);

        mockMvc.perform(get("/reclamacoes/export")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
