package com.artheus.cidadaoalerta.unit.exception.email;

import com.artheus.cidadaoalerta.exception.email.EmailSendException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmailSendExceptionTest {

    @Test
    void deveCriarExcecaoComConstrutorVazio() {
        EmailSendException ex = new EmailSendException();
        assertEquals("Erro ao enviar e-mail", ex.getMessage());
    }

    @Test
    void deveCriarExcecaoComMensagem() {
        EmailSendException ex = new EmailSendException("Falha ao enviar e-mail");
        assertEquals("Falha ao enviar e-mail", ex.getMessage());
    }

    @Test
    void deveCriarExcecaoComMensagemECausa() {
        Throwable causa = new RuntimeException("Causa original");
        EmailSendException ex = new EmailSendException("Falha ao enviar e-mail", causa);
        assertEquals("Falha ao enviar e-mail", ex.getMessage());
        assertEquals(causa, ex.getCause());
    }
}
