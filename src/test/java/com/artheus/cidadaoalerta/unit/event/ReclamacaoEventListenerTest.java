package com.artheus.cidadaoalerta.unit.event;

import com.artheus.cidadaoalerta.event.ReclamacaoEvent;
import com.artheus.cidadaoalerta.listener.ReclamacaoEventListener;
import com.artheus.cidadaoalerta.model.Reclamacao;
import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.model.enums.Role;
import com.artheus.cidadaoalerta.model.enums.TipoEventoReclamacao;
import com.artheus.cidadaoalerta.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReclamacaoEventListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ReclamacaoEventListener listener;

    private Reclamacao reclamacao;

    @BeforeEach
    void setUp() {
        reclamacao = new Reclamacao();
        reclamacao.setTitulo("Buraco na rua");
        reclamacao.setUsuario(
                new Usuario(1L, "Fabiano", "fabiano@email.com",
                        "senha123", true, Role.ROLE_USER, null)
        );
    }

    @Test
    void deveEnviarEmailParaTodosTiposDeEvento() {
        for (TipoEventoReclamacao tipo : TipoEventoReclamacao.values()) {
            ReclamacaoEvent event = new ReclamacaoEvent(reclamacao, tipo);

            listener.handleReclamacaoEvent(event);

            verify(emailService).enviarEmail(
                    eq(reclamacao.getUsuario().getEmail()),
                    anyString(),
                    anyString()
            );
            reset(emailService);
        }
    }

    @Test
    void naoDeveQuebrarQuandoEmailServiceLancaExcecao() {
        doThrow(new RuntimeException("Erro envio"))
                .when(emailService).enviarEmail(anyString(), anyString(), anyString());

        ReclamacaoEvent event = new ReclamacaoEvent(reclamacao, TipoEventoReclamacao.CRIADA);
        listener.handleReclamacaoEvent(event);

        verify(emailService).enviarEmail(eq(reclamacao.getUsuario().getEmail()), anyString(), anyString());
    }

    // ---------------- Cobertura do branch de validação de e-mail ----------------
    @Test
    void naoDeveEnviarEmailQuandoUsuarioNaoPossuirEmail() {
        reclamacao.setUsuario(new Usuario(1L, "Fabiano", null, "senha123", true, Role.ROLE_USER, null));
        ReclamacaoEvent event = new ReclamacaoEvent(reclamacao, TipoEventoReclamacao.CRIADA);

        assertDoesNotThrow(() -> listener.handleReclamacaoEvent(event));
        verifyNoInteractions(emailService);
    }

    @Test
    void naoDeveEnviarEmailQuandoUsuarioPossuirEmailEmBranco() {
        reclamacao.setUsuario(new Usuario(1L, "Fabiano", "   ", "senha123", true, Role.ROLE_USER, null));
        ReclamacaoEvent event = new ReclamacaoEvent(reclamacao, TipoEventoReclamacao.CRIADA);

        assertDoesNotThrow(() -> listener.handleReclamacaoEvent(event));
        verifyNoInteractions(emailService);
    }

    // ---------- Testes para métodos privados usando reflexão ----------
    @Test
    void gerarAssunto_deveRetornarValorCorreto() throws Exception {
        Method method = ReclamacaoEventListener.class.getDeclaredMethod("gerarAssunto", TipoEventoReclamacao.class);
        method.setAccessible(true);

        for (TipoEventoReclamacao tipo : TipoEventoReclamacao.values()) {
            String assunto = (String) method.invoke(listener, tipo);
            String esperado = switch (tipo) {
                case CRIADA -> "Nova Reclamação Registrada";
                case ATUALIZADA -> "Reclamação Atualizada";
                case INATIVADA -> "Reclamação Inativada";
                case CONCLUIDA -> "Reclamação Concluída";
            };
            assertEquals(esperado, assunto);
        }
    }

    @Test
    void gerarMensagem_deveRetornarValorCorreto() throws Exception {
        Method method = ReclamacaoEventListener.class.getDeclaredMethod("gerarMensagem", Reclamacao.class, TipoEventoReclamacao.class);
        method.setAccessible(true);

        for (TipoEventoReclamacao tipo : TipoEventoReclamacao.values()) {
            String mensagem = (String) method.invoke(listener, reclamacao, tipo);
            String esperado = switch (tipo) {
                case CRIADA -> "A reclamação 'Buraco na rua' foi cadastrada com sucesso.";
                case ATUALIZADA -> "A reclamação 'Buraco na rua' foi atualizada.";
                case INATIVADA -> "A reclamação 'Buraco na rua' foi inativada.";
                case CONCLUIDA -> "A reclamação 'Buraco na rua' foi concluída.";
            };
            assertEquals(esperado, mensagem);
        }
    }
}
