package com.artheus.cidadaoalerta.unit.exception.global;

import com.artheus.cidadaoalerta.exception.csv.CsvGenerationException;
import com.artheus.cidadaoalerta.exception.reclamacao.*;
import com.artheus.cidadaoalerta.exception.usuario.*;
import com.artheus.cidadaoalerta.exception.global.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // ================= CSV =================
    @Test
    void deveLancarCsvGenerationException() {
        CsvGenerationException ex = new CsvGenerationException("Erro ao gerar CSV");
        ResponseEntity<Map<String, Object>> response = handler.handleCsvGenerationException(ex);

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Erro ao gerar CSV", body.get("error"));
        assertEquals("Erro ao gerar CSV", body.get("message"));
        assertEquals(500, body.get("status"));
    }

    // ================= RECLAMACAO =================
    @Test
    void deveLancarReclamacaoNaoEncontradaException() {
        ReclamacaoNaoEncontradaException ex = new ReclamacaoNaoEncontradaException("Reclamação não encontrada");
        ResponseEntity<Map<String, Object>> response = handler.handleReclamacaoNaoEncontrada(ex);

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Reclamação não encontrada", body.get("error"));
        assertEquals("Reclamação não encontrada", body.get("message"));
        assertEquals(404, body.get("status"));
    }

    @Test
    void deveLancarReclamacaoDesativadaException() {
        ReclamacaoDesativadaException ex = new ReclamacaoDesativadaException("Reclamação desativada");
        ResponseEntity<Map<String, Object>> response = handler.handleReclamacaoDesativada(ex);

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Reclamação desativada", body.get("error"));
        assertEquals("Reclamação desativada", body.get("message"));
        assertEquals(400, body.get("status"));
    }

    @Test
    void deveLancarReclamacaoDuplicadaException() {
        ReclamacaoDuplicadaException ex = new ReclamacaoDuplicadaException("Rua XYZ suja", 123L);
        ResponseEntity<Map<String, Object>> response = handler.handleReclamacaoDuplicada(ex);

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Reclamação duplicada", body.get("error"));
        assertEquals("Já existe uma reclamação ativa com o título 'Rua XYZ suja' para o usuário ID 123", body.get("message"));
        assertEquals(409, body.get("status"));
    }

    @Test
    void deveLancarReclamacaoAtualizacaoInvalidaException() {
        ReclamacaoAtualizacaoInvalidaException ex = new ReclamacaoAtualizacaoInvalidaException("Atualização inválida");
        ResponseEntity<Map<String, Object>> response = handler.handleAtualizacaoInvalida(ex);

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Atualização de reclamação inválida", body.get("error"));
        assertEquals("Atualização inválida", body.get("message"));
        assertEquals(400, body.get("status"));
    }

    // ================= USUARIO =================
    @Test
    void deveLancarUsuarioNaoAutenticadoException() {
        UsuarioNaoAutenticadoException ex = new UsuarioNaoAutenticadoException("Usuário não autenticado");
        ResponseEntity<Map<String, Object>> response = handler.handleUsuarioNaoAutenticado(ex);

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Usuário não autenticado", body.get("error"));
        assertEquals("Usuário não autenticado", body.get("message"));
        assertEquals(401, body.get("status"));
    }

    @Test
    void deveLancarUsuarioSemPermissaoException() {
        UsuarioSemPermissaoException ex = new UsuarioSemPermissaoException("Usuário sem permissão");
        ResponseEntity<Map<String, Object>> response = handler.handleUsuarioSemPermissao(ex);

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Usuário sem permissão", body.get("error"));
        assertEquals("Usuário sem permissão", body.get("message"));
        assertEquals(403, body.get("status"));
    }

    @Test
    void deveLancarUsuarioNaoEncontradoException() {
        UsuarioNaoEncontradoException ex = new UsuarioNaoEncontradoException("teste@teste.com");
        ResponseEntity<Map<String, Object>> response = handler.handleUsuarioNaoEncontrado(ex);

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Usuário não encontrado", body.get("error"));
        assertEquals("Usuário não encontrado com email: teste@teste.com", body.get("message"));
        assertEquals(404, body.get("status"));
    }

    // ================= SPRING SECURITY =================
    @Test
    void deveTratarAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Acesso negado");
        ResponseEntity<Map<String, Object>> response = handler.handleAccessDenied(ex);

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Acesso negado", body.get("error"));
        assertEquals("Access Denied", body.get("message"));
        assertEquals(403, body.get("status"));
    }

    @Test
    void deveTratarAuthorizationDeniedException() {
        AuthorizationDeniedException ex = new AuthorizationDeniedException("Acesso negado");
        ResponseEntity<Map<String, Object>> response = handler.handleAccessDenied(ex);

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Acesso negado", body.get("error"));
        assertEquals("Access Denied", body.get("message"));
        assertEquals(403, body.get("status"));
    }


    // ================= GENÉRICO =================
    @Test
    void deveLancarExceptionGenerica() {
        Exception ex = new Exception("Erro interno");
        ResponseEntity<Map<String, Object>> response = handler.handleGenericException(ex);

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Erro interno", body.get("error"));
        assertEquals("Erro interno", body.get("message"));
        assertEquals(500, body.get("status"));
    }
}
