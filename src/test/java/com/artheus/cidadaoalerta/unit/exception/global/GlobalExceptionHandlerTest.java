package com.artheus.cidadaoalerta.unit.exception.global;

import com.artheus.cidadaoalerta.exception.csv.CsvGenerationException;
import com.artheus.cidadaoalerta.exception.email.EmailSendException;
import com.artheus.cidadaoalerta.exception.global.GlobalExceptionHandler;
import com.artheus.cidadaoalerta.exception.model.ApiError;
import com.artheus.cidadaoalerta.exception.reclamacao.ReclamacaoAtualizacaoInvalidaException;
import com.artheus.cidadaoalerta.exception.reclamacao.ReclamacaoDesativadaException;
import com.artheus.cidadaoalerta.exception.reclamacao.ReclamacaoDuplicadaException;
import com.artheus.cidadaoalerta.exception.reclamacao.ReclamacaoNaoEncontradaException;
import com.artheus.cidadaoalerta.exception.usuario.UsuarioNaoAutenticadoException;
import com.artheus.cidadaoalerta.exception.usuario.UsuarioNaoEncontradoException;
import com.artheus.cidadaoalerta.exception.usuario.UsuarioSemPermissaoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setup() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/teste");
    }

    // ================= MÉTODO AUXILIAR =====================
    private void assertApiError(ResponseEntity<ApiError> response, int status, String titulo, String detalheContem) {
        assertNotNull(response);
        assertNotNull(response.getBody());
        ApiError body = response.getBody();
        assertEquals(status, body.getStatus());
        assertEquals(titulo, body.getTitle());
        if (detalheContem != null) {
            assertTrue(body.getDetail().contains(detalheContem));
        }
        assertNotNull(body.getTraceId());
    }

    // ================= TESTES MÉTODOS PRIVADOS =================
    @Test
    void converterTituloEmSlug_DeveNormalizarCorretamente() throws Exception {
        Method metodo = GlobalExceptionHandler.class.getDeclaredMethod("converterTituloEmSlug", String.class);
        metodo.setAccessible(true);

        assertEquals("titulo-de-teste-123", metodo.invoke(handler, "Título de Teste 123!"));
        assertEquals("", metodo.invoke(handler, ""));
        assertEquals("", metodo.invoke(handler, (Object) null));
        assertEquals("exemplo-titulo-456", metodo.invoke(handler, "Exemplo_Título 456"));
        assertEquals("titulo", metodo.invoke(handler, "Título!@#%&*()"));
    }

    @Test
    void obterOuGerarTraceId_DeveGerarTraceIdValido() throws Exception {
        Method metodo = GlobalExceptionHandler.class.getDeclaredMethod("obterOuGerarTraceId");
        metodo.setAccessible(true);

        String traceId = (String) metodo.invoke(handler);
        assertNotNull(traceId);
        assertDoesNotThrow(() -> UUID.fromString(traceId));
    }

    // ================= TESTES MethodArgumentNotValidException =================
    @Test
    void handleValidacao_DeveRetornarErrosQuandoExistirem() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");
        bindingResult.addError(new FieldError("usuario", "nome", "Nome obrigatório"));
        bindingResult.addError(new FieldError("usuario", "email", "Email inválido"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ApiError> response = handler.handleValidacao(ex, request);

        assertApiError(response, 400, "Erro de validação", "Nome obrigatório");
        assertTrue(response.getBody().getDetail().contains("Email inválido"));
    }

    @Test
    void handleValidacao_DeveRetornarMensagemPadraoQuandoNaoExistiremErros() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ApiError> response = handler.handleValidacao(ex, request);

        assertApiError(response, 400, "Erro de validação", "Dados inválidos fornecidos");
    }

    @Test
    void handleValidacao_DeveTratarRequestNulo() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ApiError> response = handler.handleValidacao(ex, null);

        assertEquals("N/A", response.getBody().getInstance());
    }

    // ================= TESTES RECLAMAÇÃO =================
    @Test
    void handleReclamacaoNaoEncontrada_DeveRetornar404() {
        assertApiError(handler.handleReclamacaoNaoEncontrada(new ReclamacaoNaoEncontradaException("Não encontrado"), request),
                404, "Reclamação não encontrada", "Não encontrado");
    }

    @Test
    void handleReclamacaoDesativada_DeveRetornar400() {
        assertApiError(handler.handleReclamacaoDesativada(new ReclamacaoDesativadaException("Desativada"), request),
                400, "Reclamação desativada", "Desativada");
    }

    @Test
    void handleReclamacaoDuplicada_DeveRetornar409() {
        assertApiError(handler.handleReclamacaoDuplicada(new ReclamacaoDuplicadaException(), request),
                409, "Reclamação duplicada", "Já existe uma reclamação duplicada");
    }

    @Test
    void handleAtualizacaoInvalida_DeveRetornar400() {
        assertApiError(handler.handleAtualizacaoInvalida(new ReclamacaoAtualizacaoInvalidaException("Inválida"), request),
                400, "Atualização de reclamação inválida", "Inválida");
    }

    // ================= TESTES USUÁRIO =================
    @Test
    void handleUsuarioNaoAutenticado_DeveRetornar401() {
        assertApiError(handler.handleUsuarioNaoAutenticado(new UsuarioNaoAutenticadoException("Não autenticado"), request),
                401, "Usuário não autenticado", "Não autenticado");
    }

    @Test
    void handleUsuarioSemPermissao_DeveRetornar403() {
        assertApiError(handler.handleUsuarioSemPermissao(new UsuarioSemPermissaoException("Sem permissão"), request),
                403, "Usuário sem permissão", "Sem permissão");
    }

    @Test
    void handleUsuarioNaoEncontrado_DeveRetornar404() {
        assertApiError(handler.handleUsuarioNaoEncontrado(new UsuarioNaoEncontradoException("Não encontrado"), request),
                404, "Usuário não encontrado", "Não encontrado");
    }

    // ================= TESTES CSV E EMAIL =================
    @Test
    void handleErroCsv_DeveRetornar500() {
        assertApiError(handler.handleErroCsv(new CsvGenerationException("Erro CSV"), request),
                500, "Erro ao gerar CSV", "Erro CSV");
    }

    @Test
    void handleErroEmail_DeveRetornar500() {
        assertApiError(handler.handleErroEmail(new EmailSendException("Erro e-mail"), request),
                500, "Erro ao enviar e-mail", "Erro e-mail");
    }

    // ================= TESTES ACCESS DENIED =================
    @Test
    void handleAcessoNegado_ComAccessDeniedException() {
        assertApiError(handler.handleAcessoNegado(new AccessDeniedException("Negado"), request),
                403, "Acesso negado", "Access Denied");
    }

    @Test
    void handleAcessoNegado_ComAuthorizationDeniedException() {
        assertApiError(handler.handleAcessoNegado(new AuthorizationDeniedException("Negado"), request),
                403, "Acesso negado", "Access Denied");
    }

    // ================= TESTE GENÉRICO =================
    @Test
    void handleExcecaoGenerica_DeveRetornar500() {
        assertApiError(handler.handleExcecaoGenerica(new Exception("Erro genérico"), request),
                500, "Erro interno", "Erro genérico");
    }
}
