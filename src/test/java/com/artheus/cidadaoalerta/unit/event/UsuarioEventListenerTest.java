package com.artheus.cidadaoalerta.unit.event;

import com.artheus.cidadaoalerta.event.UsuarioEvent;
import com.artheus.cidadaoalerta.listener.UsuarioEventListener;
import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.model.enums.Role;
import com.artheus.cidadaoalerta.model.enums.TipoEventoUsuario;
import com.artheus.cidadaoalerta.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioEventListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UsuarioEventListener listener;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario(1L, "Fabiano", "fabiano@email.com",
                "senha123", true, Role.ROLE_USER, null);
    }

    @Test
    void deveEnviarEmailParaTodosTiposDeEvento() {
        for (TipoEventoUsuario tipo : TipoEventoUsuario.values()) {
            UsuarioEvent event = new UsuarioEvent(usuario, tipo);

            listener.handleUsuarioEvent(event);

            String assunto = switch (tipo) {
                case CRIADO -> "Novo Usuário Registrado";
                case ATUALIZADO -> "Usuário Atualizado";
                case INATIVADO -> "Usuário Inativado";
                case DELETADO -> "Usuário Deletado";
            };

            String mensagem = switch (tipo) {
                case CRIADO -> "O usuário 'Fabiano' foi registrado com sucesso.";
                case ATUALIZADO -> "O usuário 'Fabiano' foi atualizado com sucesso.";
                case INATIVADO -> "O usuário 'Fabiano' foi inativado.";
                case DELETADO -> "O usuário 'Fabiano' foi deletado.";
            };

            verify(emailService).enviarEmail(
                    eq(usuario.getEmail()),
                    eq(assunto),
                    eq(mensagem)
            );

            // Reseta mocks para o próximo loop
            reset(emailService);
        }
    }

    @Test
    void naoEnviaEmailSeUsuarioSemEmail() {
        Usuario usuarioSemEmail = new Usuario(2L, "Joao", null,
                "senha123", true, Role.ROLE_USER, null);
        UsuarioEvent event = new UsuarioEvent(usuarioSemEmail, TipoEventoUsuario.CRIADO);

        listener.handleUsuarioEvent(event);

        verifyNoInteractions(emailService);
    }

    @Test
    void naoEnviaEmailSeEmailBlank() {
        Usuario usuarioBlankEmail = new Usuario(3L, "Maria", "  ",
                "senha123", true, Role.ROLE_USER, null);
        UsuarioEvent event = new UsuarioEvent(usuarioBlankEmail, TipoEventoUsuario.ATUALIZADO);

        listener.handleUsuarioEvent(event);

        verifyNoInteractions(emailService);
    }

    @Test
    void deveTratarExcecaoDoEmailService() {
        UsuarioEvent event = new UsuarioEvent(usuario, TipoEventoUsuario.CRIADO);

        doThrow(new RuntimeException("Falha ao enviar")).when(emailService)
                .enviarEmail(anyString(), anyString(), anyString());

        listener.handleUsuarioEvent(event);

        verify(emailService).enviarEmail(
                eq(usuario.getEmail()),
                eq("Novo Usuário Registrado"),
                eq("O usuário 'Fabiano' foi registrado com sucesso.")
        );

        // Listener não deve lançar exceção mesmo se EmailService falhar
    }
}
