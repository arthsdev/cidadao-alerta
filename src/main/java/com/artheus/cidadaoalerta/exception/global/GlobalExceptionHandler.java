package com.artheus.cidadaoalerta.exception.global;

import com.artheus.cidadaoalerta.exception.csv.CsvGenerationException;
import com.artheus.cidadaoalerta.exception.reclamacao.*;
import com.artheus.cidadaoalerta.exception.usuario.UsuarioNaoAutenticadoException;
import com.artheus.cidadaoalerta.exception.usuario.UsuarioNaoEncontradoException;
import com.artheus.cidadaoalerta.exception.usuario.UsuarioSemPermissaoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // ================= CSV =================
    @ExceptionHandler(CsvGenerationException.class)
    public ResponseEntity<Map<String, Object>> handleCsvGenerationException(CsvGenerationException ex) {
        return buildResponse("Erro ao gerar CSV", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ================= RECLAMACAO =================
    @ExceptionHandler(ReclamacaoNaoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> handleReclamacaoNaoEncontrada(ReclamacaoNaoEncontradaException ex) {
        return buildResponse("Reclamação não encontrada", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ReclamacaoDesativadaException.class)
    public ResponseEntity<Map<String, Object>> handleReclamacaoDesativada(ReclamacaoDesativadaException ex) {
        return buildResponse("Reclamação desativada", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ReclamacaoDuplicadaException.class)
    public ResponseEntity<Map<String, Object>> handleReclamacaoDuplicada(ReclamacaoDuplicadaException ex) {
        return buildResponse("Reclamação duplicada", ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ReclamacaoAtualizacaoInvalidaException.class)
    public ResponseEntity<Map<String, Object>> handleAtualizacaoInvalida(ReclamacaoAtualizacaoInvalidaException ex) {
        return buildResponse("Atualização de reclamação inválida", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // ================= USUARIO =================
    @ExceptionHandler(UsuarioNaoAutenticadoException.class)
    public ResponseEntity<Map<String, Object>> handleUsuarioNaoAutenticado(UsuarioNaoAutenticadoException ex) {
        return buildResponse("Usuário não autenticado", ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UsuarioSemPermissaoException.class)
    public ResponseEntity<Map<String, Object>> handleUsuarioSemPermissao(UsuarioSemPermissaoException ex) {
        return buildResponse("Usuário sem permissão", ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleUsuarioNaoEncontrado(UsuarioNaoEncontradoException ex) {
        return buildResponse("Usuário não encontrado", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // ================= SPRING SECURITY =================
    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<Map<String, Object>> handleAccessDenied(Exception ex) {
        return buildResponse("Acesso negado", "Access Denied", HttpStatus.FORBIDDEN);
    }

    // ================= GENÉRICO =================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return buildResponse("Erro interno", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ================= MÉTODO AUXILIAR =================
    private ResponseEntity<Map<String, Object>> buildResponse(String error, String message, HttpStatus status) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("error", error);
        body.put("message", message);
        body.put("status", status.value());
        return ResponseEntity.status(status).body(body);
    }
}
