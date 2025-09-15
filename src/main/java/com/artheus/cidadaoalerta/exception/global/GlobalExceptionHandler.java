package com.artheus.cidadaoalerta.exception.global;

import com.artheus.cidadaoalerta.exception.csv.CsvGenerationException;
import com.artheus.cidadaoalerta.exception.email.EmailSendException;
import com.artheus.cidadaoalerta.exception.model.ApiError;
import com.artheus.cidadaoalerta.exception.reclamacao.*;
import com.artheus.cidadaoalerta.exception.usuario.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private final boolean emAmbienteProducao;

    // Construtor padrão assume ambiente de desenvolvimento
    public GlobalExceptionHandler() {
        this.emAmbienteProducao = estaEmAmbienteProducao();
    }

    // Construtor alternativo para testes
    public GlobalExceptionHandler(boolean emAmbienteProducao) {
        this.emAmbienteProducao = emAmbienteProducao;
    }

    // ================= VALIDAÇÃO =================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidacao(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldError> erros = ex.getBindingResult() != null
                ? ex.getBindingResult().getFieldErrors()
                : List.of();

        String detalhes = erros.stream()
                .filter(e -> e != null && e.getDefaultMessage() != null)
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        if (detalhes.isEmpty()) {
            detalhes = "Dados inválidos fornecidos";
        }

        return construirResposta("Erro de validação", detalhes, HttpStatus.BAD_REQUEST, request);
    }

    // ================= CSV =================
    @ExceptionHandler(CsvGenerationException.class)
    public ResponseEntity<ApiError> handleErroCsv(CsvGenerationException ex, HttpServletRequest request) {
        log.error("Erro ao gerar CSV", ex);
        return construirResposta("Erro ao gerar CSV", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    // ================= RECLAMAÇÃO =================
    @ExceptionHandler(ReclamacaoNaoEncontradaException.class)
    public ResponseEntity<ApiError> handleReclamacaoNaoEncontrada(ReclamacaoNaoEncontradaException ex, HttpServletRequest request) {
        log.warn("Reclamação não encontrada: {}", ex.getMessage());
        return construirResposta("Reclamação não encontrada", ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(ReclamacaoDesativadaException.class)
    public ResponseEntity<ApiError> handleReclamacaoDesativada(ReclamacaoDesativadaException ex, HttpServletRequest request) {
        log.warn("Reclamação desativada: {}", ex.getMessage());
        return construirResposta("Reclamação desativada", ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(ReclamacaoDuplicadaException.class)
    public ResponseEntity<ApiError> handleReclamacaoDuplicada(ReclamacaoDuplicadaException ex, HttpServletRequest request) {
        log.warn("Reclamação duplicada: {}", ex.getMessage());
        return construirResposta("Reclamação duplicada", ex.getMessage(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(ReclamacaoAtualizacaoInvalidaException.class)
    public ResponseEntity<ApiError> handleAtualizacaoInvalida(ReclamacaoAtualizacaoInvalidaException ex, HttpServletRequest request) {
        log.warn("Atualização inválida em reclamação: {}", ex.getMessage());
        return construirResposta("Atualização de reclamação inválida", ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    // ================= USUÁRIO =================
    @ExceptionHandler(UsuarioNaoAutenticadoException.class)
    public ResponseEntity<ApiError> handleUsuarioNaoAutenticado(UsuarioNaoAutenticadoException ex, HttpServletRequest request) {
        log.warn("Usuário não autenticado: {}", ex.getMessage());
        return construirResposta("Usuário não autenticado", ex.getMessage(), HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(UsuarioSemPermissaoException.class)
    public ResponseEntity<ApiError> handleUsuarioSemPermissao(UsuarioSemPermissaoException ex, HttpServletRequest request) {
        log.warn("Usuário sem permissão: {}", ex.getMessage());
        return construirResposta("Usuário sem permissão", ex.getMessage(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<ApiError> handleUsuarioNaoEncontrado(UsuarioNaoEncontradoException ex, HttpServletRequest request) {
        log.warn("Usuário não encontrado: {}", ex.getMessage());
        return construirResposta("Usuário não encontrado", ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    // ================= EMAIL =================
    @ExceptionHandler(EmailSendException.class)
    public ResponseEntity<ApiError> handleErroEmail(EmailSendException ex, HttpServletRequest request) {
        log.error("Erro ao enviar e-mail", ex);
        return construirResposta("Erro ao enviar e-mail", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    // ================= SPRING SECURITY =================
    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<ApiError> handleAcessoNegado(Exception ex, HttpServletRequest request) {
        log.warn("Acesso negado", ex);
        return construirResposta("Acesso negado", "Access Denied", HttpStatus.FORBIDDEN, request);
    }

    // ================= GENÉRICO =================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleExcecaoGenerica(Exception ex, HttpServletRequest request) {
        log.error("Erro interno não tratado", ex);
        return construirResposta("Erro interno", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    // ================= MÉTODOS AUXILIARES =================
    private ResponseEntity<ApiError> construirResposta(String titulo, String detalhe, HttpStatus status, HttpServletRequest request) {
        String caminho = request != null ? request.getRequestURI() : "N/A";

        ApiError erro = ApiError.builder()
                .type("Algo deu errado. Por favor, tente novamente mais tarde.")
                .title(titulo)
                .status(status.value())
                .detail(deveExibirDetalhesErro(detalhe) ? detalhe : "Ocorreu um erro interno. Tente novamente mais tarde.")
                .instance(caminho)
                .traceId(obterOuGerarTraceId())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(erro);
    }

    private String converterTituloEmSlug(String input) {
        if (input == null || input.isBlank()) return "";

        String normalizado = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return normalizado.toLowerCase()
                .replaceAll("[_\\s]+", "-")
                .replaceAll("[^a-z0-9\\-]", "");
    }

    private String obterOuGerarTraceId() {
        String traceId = MDC.get("traceId");
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }
        return traceId;
    }

    private boolean deveExibirDetalhesErro(String detalheMensagem) {
        return !emAmbienteProducao;
    }

    private boolean estaEmAmbienteProducao() {
        String perfil = System.getenv("SPRING_PROFILES_ACTIVE");
        return perfil != null && perfil.equalsIgnoreCase("prod");
    }
}
