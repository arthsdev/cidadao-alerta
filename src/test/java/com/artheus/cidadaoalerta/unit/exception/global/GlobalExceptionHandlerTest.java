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
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setup() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/teste");
    }

    // ===================== MÉTODO AUXILIAR =====================
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

    // ===================== TESTE MÉTODO PRIVADO converterTituloEmSlug =====================
    @Test
    void testConverterTituloEmSlugViaReflection() throws Exception {
        Method metodo = GlobalExceptionHandler.class.getDeclaredMethod("converterTituloEmSlug", String.class);
        metodo.setAccessible(true);

        // instancia do handler
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        // caso normal
        String resultado = (String) metodo.invoke(handler, "Título de Teste 123!");
        assertEquals("titulo-de-teste-123", resultado);

        // string vazia
        String resultadoVazio = (String) metodo.invoke(handler, "");
        assertEquals("", resultadoVazio);

        // null
        String resultadoNull = (String) metodo.invoke(handler, (Object) null);
        assertEquals("", resultadoNull);

        // com espaços e underscores
        String resultadoEspacos = (String) metodo.invoke(handler, "Exemplo_Título 456");
        assertEquals("exemplo-titulo-456", resultadoEspacos);

        // com caracteres especiais
        String resultadoEspeciais = (String) metodo.invoke(handler, "Título!@#%&*()");
        assertEquals("titulo", resultadoEspeciais);
    }

    // ===================== TESTES VALIDAÇÃO =====================
    @Test
    void testHandleValidacaoComErros() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("usuario", "nome", "Nome obrigatório"),
                new FieldError("usuario", "email", "Email inválido")
        ));
        MethodParameter mp = mock(MethodParameter.class);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(mp, bindingResult);

        ResponseEntity<ApiError> response = handler.handleValidacao(ex, request);

        assertApiError(response, 400, "Erro de validação", "Nome obrigatório");
        assertTrue(response.getBody().getDetail().contains("Email inválido"));
    }

    @Test
    void testHandleValidacaoSemFieldErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());
        MethodParameter mp = mock(MethodParameter.class);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(mp, bindingResult);

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
