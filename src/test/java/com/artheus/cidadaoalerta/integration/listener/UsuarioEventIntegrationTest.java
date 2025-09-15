package com.artheus.cidadaoalerta.integration.listener;

import com.artheus.cidadaoalerta.event.UsuarioEvent;
import com.artheus.cidadaoalerta.listener.UsuarioEventListener;
import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.model.enums.TipoEventoUsuario;
import com.artheus.cidadaoalerta.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioEventListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UsuarioEventListener listener;

    private Usuario criarUsuario(String nome, String email) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        return usuario;
    }

    @Test
    void deveEnviarEmailQuandoUsuarioCriado() {
        Usuario usuario = criarUsuario("Fulano", "fulano@email.com");
        UsuarioEvent event = new UsuarioEvent(usuario, TipoEventoUsuario.CRIADO);

        listener.handleUsuarioEvent(event);

        verify(emailService).enviarEmail(
                eq("fulano@email.com"),
                eq("Novo Usuário Registrado"),
                contains("Fulano")
        );
    }

    @Test
    void deveEnviarEmailQuandoUsuarioAtualizado() {
        Usuario usuario = criarUsuario("Beltrano", "beltrano@email.com");
        UsuarioEvent event = new UsuarioEvent(usuario, TipoEventoUsuario.ATUALIZADO);

        listener.handleUsuarioEvent(event);

        verify(emailService).enviarEmail(
                eq("beltrano@email.com"),
                eq("Usuário Atualizado"),
                contains("Beltrano")
        );
    }

    @Test
    void deveNaoEnviarEmailSeUsuarioNaoTemEmail() {
        Usuario usuario = criarUsuario("SemEmail", null);
        UsuarioEvent event = new UsuarioEvent(usuario, TipoEventoUsuario.CRIADO);

        listener.handleUsuarioEvent(event);

        verifyNoInteractions(emailService);
    }

    @Test
    void deveTratarErroQuandoEmailServiceLancaExcecao() {
        Usuario usuario = criarUsuario("Erro", "erro@email.com");
        UsuarioEvent event = new UsuarioEvent(usuario, TipoEventoUsuario.INATIVADO);

        doThrow(new RuntimeException("Falha SMTP"))
                .when(emailService).enviarEmail(anyString(), anyString(), anyString());

        listener.handleUsuarioEvent(event);

        verify(emailService).enviarEmail(
                eq("erro@email.com"),
                eq("Usuário Inativado"),
                contains("Erro")
        );
    }
}
