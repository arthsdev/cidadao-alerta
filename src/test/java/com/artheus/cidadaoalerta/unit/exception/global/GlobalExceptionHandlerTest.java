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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setup() {
        handler = new GlobalExceptionHandler(false); // ambiente dev
        request = new MockHttpServletRequest();
        request.setRequestURI("/teste");
    }

    // ===================== MÉTODOS AUXILIARES =====================
    private void assertApiError(ResponseEntity<ApiError> response, int status, String titulo, String detalheContem) {
        assertNotNull(response);
        assertNotNull(response.getBody());
        ApiError body = response.getBody();
        assertEquals(status, body.getStatus());
        assertEquals(titulo, body.getTitle());
        if (detalheContem != null) {
            assertTrue(body.getDetail().contains(detalheContem));
        }
        assertEquals("/teste", body.getInstance());
        assertNotNull(body.getTraceId());
    }

    private Object invocarMetodoPrivado(Object alvo, String nomeMetodo, Object... args) throws Exception {
        Class<?> clazz = alvo.getClass();

        // Identifica tipos de parâmetro (HttpServletRequest como interface)
        Class<?>[] tiposParametros = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof HttpServletRequest) {
                tiposParametros[i] = HttpServletRequest.class;
            } else {
                tiposParametros[i] = args[i].getClass();
            }
        }

        Method metodo = clazz.getDeclaredMethod(nomeMetodo, tiposParametros);
        metodo.setAccessible(true);
        return metodo.invoke(alvo, args);
    }

    // ===================== TESTES MÉTODOS PRIVADOS =====================
    @Test
    void testConverterTituloEmSlug() throws Exception {
        String input = "Outro título 123";
        String esperado = "outro-titulo-123";
        String resultado = (String) invocarMetodoPrivado(handler, "converterTituloEmSlug", input);
        assertEquals(esperado, resultado);
    }

    @Test
    void testObterOuGerarTraceId() throws Exception {
        String traceId = (String) invocarMetodoPrivado(handler, "obterOuGerarTraceId");
        assertNotNull(traceId);
        assertDoesNotThrow(() -> UUID.fromString(traceId));
    }

    @Test
    void testConstruirRespostaViaReflection() throws Exception {
        ResponseEntity<ApiError> response = (ResponseEntity<ApiError>) invocarMetodoPrivado(
                handler,
                "construirResposta",
                "Título Teste",
                "Detalhe Teste",
                HttpStatus.BAD_REQUEST,
                request
        );
        assertApiError(response, 400, "Título Teste", "Detalhe Teste");
    }

    @Test
    void testDeveExibirDetalhesErro() throws Exception {
        boolean resultado = (boolean) invocarMetodoPrivado(handler, "deveExibirDetalhesErro", "Mensagem qualquer");
        assertTrue(resultado); // ambiente dev
    }

    // ===================== TESTES VALIDAÇÃO =====================
    @Test
    void testHandleValidacao() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("usuario", "nome", "Nome obrigatório"),
                new FieldError("usuario", "email", "Email inválido")
        ));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
        ResponseEntity<ApiError> response = handler.handleValidacao(ex, request);

        assertApiError(response, 400, "Erro de validação", "Nome obrigatório");
        assertTrue(response.getBody().getDetail().contains("Email inválido"));
    }

    @Test
    void testHandleValidacaoSemFieldErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
        ResponseEntity<ApiError> response = handler.handleValidacao(ex, request);

        assertApiError(response, 400, "Erro de validação", "Dados inválidos fornecidos");
    }

    // ===================== TESTES RECLAMAÇÃO =====================
    @Test
    void testHandleReclamacaoNaoEncontrada() {
        assertApiError(handler.handleReclamacaoNaoEncontrada(new ReclamacaoNaoEncontradaException("Não encontrado"), request),
                404, "Reclamação não encontrada", "Não encontrado");
    }

    @Test
    void testHandleReclamacaoDesativada() {
        assertApiError(handler.handleReclamacaoDesativada(new ReclamacaoDesativadaException("Desativada"), request),
                400, "Reclamação desativada", "Desativada");
    }

    @Test
    void testHandleReclamacaoDuplicada() {
        assertApiError(handler.handleReclamacaoDuplicada(new ReclamacaoDuplicadaException(), request),
                409, "Reclamação duplicada", "Já existe uma reclamação duplicada");
    }

    @Test
    void testHandleAtualizacaoInvalida() {
        assertApiError(handler.handleAtualizacaoInvalida(new ReclamacaoAtualizacaoInvalidaException("Inválida"), request),
                400, "Atualização de reclamação inválida", "Inválida");
    }

    // ===================== TESTES USUÁRIO =====================
    @Test
    void testHandleUsuarioNaoAutenticado() {
        assertApiError(handler.handleUsuarioNaoAutenticado(new UsuarioNaoAutenticadoException("Não autenticado"), request),
                401, "Usuário não autenticado", "Não autenticado");
    }

    @Test
    void testHandleUsuarioSemPermissao() {
        assertApiError(handler.handleUsuarioSemPermissao(new UsuarioSemPermissaoException("Sem permissão"), request),
                403, "Usuário sem permissão", "Sem permissão");
    }

    @Test
    void testHandleUsuarioNaoEncontrado() {
        assertApiError(handler.handleUsuarioNaoEncontrado(new UsuarioNaoEncontradoException("Não encontrado"), request),
                404, "Usuário não encontrado", "Não encontrado");
    }

    // ===================== TESTES CSV E EMAIL =====================
    @Test
    void testHandleCsvGenerationException() {
        assertApiError(handler.handleErroCsv(new CsvGenerationException("Erro CSV"), request),
                500, "Erro ao gerar CSV", "Erro CSV");
    }

    @Test
    void testHandleEmailSendException() {
        assertApiError(handler.handleErroEmail(new EmailSendException("Erro e-mail"), request),
                500, "Erro ao enviar e-mail", "Erro e-mail");
    }

    // ===================== TESTES ACCESS DENIED =====================
    @Test
    void testHandleAcessoNegadoComAccessDeniedException() {
        assertApiError(handler.handleAcessoNegado(new AccessDeniedException("Negado"), request),
                403, "Acesso negado", "Access Denied");
    }

    @Test
    void testHandleAcessoNegadoComAuthorizationDeniedException() {
        assertApiError(handler.handleAcessoNegado(new AuthorizationDeniedException("Negado"), request),
                403, "Acesso negado", "Access Denied");
    }

    // ===================== TESTE GENÉRICO =====================
    @Test
    void testHandleExcecaoGenerica() {
        assertApiError(handler.handleExcecaoGenerica(new Exception("Erro genérico"), request),
                500, "Erro interno", "Erro genérico");
    }
}
