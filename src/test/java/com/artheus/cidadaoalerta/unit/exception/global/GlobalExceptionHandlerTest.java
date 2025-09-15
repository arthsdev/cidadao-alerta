package com.artheus.cidadaoalerta.unit.exception.global;

import com.artheus.cidadaoalerta.exception.csv.CsvGenerationException;
import com.artheus.cidadaoalerta.exception.email.EmailSendException;
import com.artheus.cidadaoalerta.exception.global.GlobalExceptionHandler;
import com.artheus.cidadaoalerta.exception.model.ApiError;
import com.artheus.cidadaoalerta.exception.reclamacao.*;
import com.artheus.cidadaoalerta.exception.usuario.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setup() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/teste");
    }

    @Test
    void testHandleCsvGenerationException() {
        CsvGenerationException ex = new CsvGenerationException("Erro CSV");
        ResponseEntity<ApiError> response = handler.handleCsvGenerationException(ex, request);

        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertTrue(response.getBody().getDetail().contains("Erro CSV"));
    }

    @Test
    void testHandleReclamacaoNaoEncontrada() {
        ReclamacaoNaoEncontradaException ex = new ReclamacaoNaoEncontradaException("Não encontrado");
        ResponseEntity<ApiError> response = handler.handleReclamacaoNaoEncontrada(ex, request);

        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
    }

    @Test
    void testHandleReclamacaoDesativada() {
        ReclamacaoDesativadaException ex = new ReclamacaoDesativadaException("Desativada");
        ResponseEntity<ApiError> response = handler.handleReclamacaoDesativada(ex, request);

        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    void testHandleReclamacaoDuplicada() {
        ReclamacaoDuplicadaException ex = new ReclamacaoDuplicadaException();
        ResponseEntity<ApiError> response = handler.handleReclamacaoDuplicada(ex, request);

        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
    }

    @Test
    void testHandleAtualizacaoInvalida() {
        ReclamacaoAtualizacaoInvalidaException ex = new ReclamacaoAtualizacaoInvalidaException("Inválida");
        ResponseEntity<ApiError> response = handler.handleAtualizacaoInvalida(ex, request);

        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    void testHandleUsuarioNaoAutenticado() {
        UsuarioNaoAutenticadoException ex = new UsuarioNaoAutenticadoException("Não autenticado");
        ResponseEntity<ApiError> response = handler.handleUsuarioNaoAutenticado(ex, request);

        assertNotNull(response.getBody());
        assertEquals(401, response.getBody().getStatus());
    }

    @Test
    void testHandleUsuarioSemPermissao() {
        UsuarioSemPermissaoException ex = new UsuarioSemPermissaoException("Sem permissão");
        ResponseEntity<ApiError> response = handler.handleUsuarioSemPermissao(ex, request);

        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
    }

    @Test
    void testHandleUsuarioNaoEncontrado() {
        UsuarioNaoEncontradoException ex = new UsuarioNaoEncontradoException("Não encontrado");
        ResponseEntity<ApiError> response = handler.handleUsuarioNaoEncontrado(ex, request);

        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
    }

    @Test
    void testHandleEmailSendException() {
        EmailSendException ex = new EmailSendException("Erro e-mail");
        ResponseEntity<ApiError> response = handler.handleEmailSendException(ex, request);

        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
    }

    @Test
    void testHandleAccessDeniedWithAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Negado");
        ResponseEntity<ApiError> response = handler.handleAccessDenied(ex, request);

        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
    }

    @Test
    void testHandleAccessDeniedWithAuthorizationDeniedException() {
        AuthorizationDeniedException ex = new AuthorizationDeniedException("Negado");
        ResponseEntity<ApiError> response = handler.handleAccessDenied(ex, request);

        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().getStatus());
    }

    @Test
    void testHandleGenericException() {
        Exception ex = new Exception("Erro genérico");
        ResponseEntity<ApiError> response = handler.handleGenericException(ex, request);

        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertTrue(response.getBody().getDetail().contains("Erro genérico"));
    }
}
