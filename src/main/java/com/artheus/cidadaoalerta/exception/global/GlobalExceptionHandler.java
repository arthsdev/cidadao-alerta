package com.artheus.cidadaoalerta.exception.global;

import com.artheus.cidadaoalerta.exception.csv.CsvGenerationException;
import com.artheus.cidadaoalerta.exception.email.EmailSendException;
import com.artheus.cidadaoalerta.exception.model.ApiError;
import com.artheus.cidadaoalerta.exception.reclamacao.ReclamacaoAtualizacaoInvalidaException;
import com.artheus.cidadaoalerta.exception.reclamacao.ReclamacaoDesativadaException;
import com.artheus.cidadaoalerta.exception.reclamacao.ReclamacaoDuplicadaException;
import com.artheus.cidadaoalerta.exception.reclamacao.ReclamacaoNaoEncontradaException;
import com.artheus.cidadaoalerta.exception.usuario.UsuarioNaoAutenticadoException;
import com.artheus.cidadaoalerta.exception.usuario.UsuarioNaoEncontradoException;
import com.artheus.cidadaoalerta.exception.usuario.UsuarioSemPermissaoException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // ================= CSV =================
    @ExceptionHandler(CsvGenerationException.class)
    public ResponseEntity<ApiError> handleCsvGenerationException(
            CsvGenerationException ex, HttpServletRequest request) {
        log.error("Erro ao gerar CSV", ex);
        return buildResponse("Erro ao gerar CSV", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    // ================= RECLAMAÇÃO =================
    @ExceptionHandler(ReclamacaoNaoEncontradaException.class)
    public ResponseEntity<ApiError> handleReclamacaoNaoEncontrada(
            ReclamacaoNaoEncontradaException ex, HttpServletRequest request) {
        log.warn("Reclamação não encontrada: {}", ex.getMessage());
        return buildResponse("Reclamação não encontrada", ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(ReclamacaoDesativadaException.class)
    public ResponseEntity<ApiError> handleReclamacaoDesativada(
            ReclamacaoDesativadaException ex, HttpServletRequest request) {
        log.warn("Tentativa de acessar reclamação desativada: {}", ex.getMessage());
        return buildResponse("Reclamação desativada", ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(ReclamacaoDuplicadaException.class)
    public ResponseEntity<ApiError> handleReclamacaoDuplicada(
            ReclamacaoDuplicadaException ex, HttpServletRequest request) {
        log.warn("Reclamação duplicada: {}", ex.getMessage());
        return buildResponse("Reclamação duplicada", ex.getMessage(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(ReclamacaoAtualizacaoInvalidaException.class)
    public ResponseEntity<ApiError> handleAtualizacaoInvalida(
            ReclamacaoAtualizacaoInvalidaException ex, HttpServletRequest request) {
        log.warn("Atualização inválida em reclamação: {}", ex.getMessage());
        return buildResponse("Atualização de reclamação inválida", ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    // ================= USUÁRIO =================
    @ExceptionHandler(UsuarioNaoAutenticadoException.class)
    public ResponseEntity<ApiError> handleUsuarioNaoAutenticado(
            UsuarioNaoAutenticadoException ex, HttpServletRequest request) {
        log.warn("Usuário não autenticado: {}", ex.getMessage());
        return buildResponse("Usuário não autenticado", ex.getMessage(), HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(UsuarioSemPermissaoException.class)
    public ResponseEntity<ApiError> handleUsuarioSemPermissao(
            UsuarioSemPermissaoException ex, HttpServletRequest request) {
        log.warn("Usuário sem permissão: {}", ex.getMessage());
        return buildResponse("Usuário sem permissão", ex.getMessage(), HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<ApiError> handleUsuarioNaoEncontrado(
            UsuarioNaoEncontradoException ex, HttpServletRequest request) {
        log.warn("Usuário não encontrado: {}", ex.getMessage());
        return buildResponse("Usuário não encontrado", ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    // ================= EMAIL =================
    @ExceptionHandler(EmailSendException.class)
    public ResponseEntity<ApiError> handleEmailSendException(
            EmailSendException ex, HttpServletRequest request) {
        log.error("Erro ao enviar e-mail", ex);
        return buildResponse("Erro ao enviar e-mail", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    // ================= SPRING SECURITY =================
    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<ApiError> handleAccessDenied(Exception ex, HttpServletRequest request) {
        log.warn("Acesso negado", ex);
        return buildResponse("Acesso negado", "Access Denied", HttpStatus.FORBIDDEN, request);
    }

    // ================= GENÉRICO =================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Erro interno não tratado", ex);
        return buildResponse("Erro interno", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    // ================= MÉTODO AUXILIAR =================
    /**
     * Constrói a resposta HTTP padronizada para exceções da API.
     * O método também gera um traceId para rastreamento e converte o título do erro em um slug para
     * formar um link identificador genérico do tipo do erro.
     * Dependendo do ambiente (produção ou não), pode ocultar detalhes sensíveis da mensagem de erro.
     */
    private ResponseEntity<ApiError> buildResponse(
            String title,
            String detail,
            HttpStatus status,
            HttpServletRequest request
    ) {
        String path = request != null ? request.getRequestURI() : "N/A";

        ApiError error = ApiError.builder()
                .type("Algo deu errado. Por favor, tente novamente mais tarde.")
                .title(title)
                .status(status.value())
                .detail(deveExibirDetalhesErro(detail) ? detail : "Ocorreu um erro interno. Tente novamente mais tarde.")
                .instance(path)
                .traceId(obterOuGerarTraceId())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(error);
    }

    /**
     * Transforma uma string qualquer em uma versão amigável para URLs,
     * removendo caracteres especiais e substituindo espaços por hífens.
     * Exemplo: "Erro ao gerar CSV" vira "erro-ao-gerar-csv".
     * Isso aqui ajuda a criar URIs mais legíveis para documentação de erros.
     */
    private String converterTituloEmSlug(String input) {
        if (input == null) return "";
        return input.toLowerCase()
                .replace(" ", "-")
                .replaceAll("[^a-z0-9\\-]", "");
    }

    /**
     * Gera um identificador único para rastreamento da requisição atual.
     * Primeiro tenta recuperar o traceId que pode estar armazenado no MDC (Mapped Diagnostic Context),
     * que é usado para propagação do ID de rastreamento nos logs durante a requisição.
     * Se não encontrar nenhum traceId no MDC, gera um novo UUID aleatório como fallback.
     * Isso facilita o debug.
     */
    private String obterOuGerarTraceId() {
        String traceId = MDC.get("traceId"); // tenta pegar do contexto de logging
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString(); // fallback: cria um novo UUID
        }
        return traceId;
    }

    /**
     * Verifica se a aplicação está rodando no ambiente de produção.
     * Essa verificação é feita consultando a variável de ambiente 'SPRING_PROFILES_ACTIVE',
     * que indica o perfil ativo do Spring. Se o perfil for 'prod', considera que está em produção.
     * Isso pode ser usado para controlar comportamentos diferentes entre dev, teste e produção,
     * como mostrar detalhes de erros ou ativar logs mais verbosos.
     */
    private boolean estaEmAmbienteProducao() {
        String perfilAtivo = System.getenv("SPRING_PROFILES_ACTIVE");
        return perfilAtivo != null && perfilAtivo.equalsIgnoreCase("prod");
    }

    /**
     * Define se os detalhes do erro devem ser exibidos na resposta.
     * Em ambiente de produção, por questões de segurança e experiência do usuário,
     * geralmente não mostramos detalhes específicos do erro.
     * Em outros ambientes (desenvolvimento, teste), os detalhes são exibidos para facilitar o debug.
     */
    private boolean deveExibirDetalhesErro(String detalheMensagem) {
        return !estaEmAmbienteProducao();
    }

}
