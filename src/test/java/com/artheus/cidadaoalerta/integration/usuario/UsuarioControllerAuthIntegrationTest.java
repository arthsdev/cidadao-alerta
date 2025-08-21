package com.artheus.cidadaoalerta.integration.usuario;

import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.model.enums.Role;
import com.artheus.cidadaoalerta.repository.UsuarioRepository;
import com.artheus.cidadaoalerta.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UsuarioControllerAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private Usuario usuario;

    @BeforeEach
    void setup() {
        SecurityContextHolder.clearContext();
        usuarioRepository.deleteAll();

        usuario = new Usuario();
        usuario.setNome("Teste Usuario");
        usuario.setEmail("teste@email.com");
        usuario.setSenha(passwordEncoder.encode("senhaSegura123"));
        usuario.setAtivo(true);
        usuario.setPapel(Role.ROLE_USER);

        usuarioRepository.save(usuario);
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveRetornarUsuarioAutenticadoComJwtValido() throws Exception {
        Usuario usuarioLogado = usuario;
        String token = jwtService.gerarToken(usuario);

        mockMvc.perform(get("/usuarios/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Teste Usuario"))
                .andExpect(jsonPath("$.email").value("teste@email.com"));
    }
}
