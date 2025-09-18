package com.artheus.cidadaoalerta.unit.service;

import com.artheus.cidadaoalerta.exception.email.EmailSendException;
import com.artheus.cidadaoalerta.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        emailService = new EmailService(mailSender);
    }

    @Test
    void deveEnviarEmailComSucesso() {
        assertDoesNotThrow(() -> emailService.enviarEmail(
                "teste@email.com", "Assunto", "Mensagem"));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void deveLancarEmailSendExceptionQuandoFalharEnvio() {
        doThrow(new RuntimeException("Erro interno")).when(mailSender).send(any(SimpleMailMessage.class));

        EmailSendException exception = assertThrows(EmailSendException.class, () ->
                emailService.enviarEmail("teste@email.com", "Assunto", "Mensagem"));

        assertTrue(exception.getMessage().contains("teste@email.com"));
    }
}
