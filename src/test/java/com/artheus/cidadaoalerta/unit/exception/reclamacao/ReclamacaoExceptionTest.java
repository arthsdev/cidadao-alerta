package com.artheus.cidadaoalerta.unit.exception.reclamacao;

import com.artheus.cidadaoalerta.exception.reclamacao.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReclamacaoExceptionTest {

    // ====================== RECLAMAÇÃO DUPLICADA ======================
    @Nested
    @DisplayName("ReclamacaoDuplicadaException")
    class ReclamacaoDuplicadaTests {

        @Test
        void deveLancarComMensagemDetalhada() {
            String titulo = "Buraco na rua";
            Long usuarioId = 1L;

            ReclamacaoDuplicadaException exception = assertThrows(
                    ReclamacaoDuplicadaException.class,
                    () -> { throw new ReclamacaoDuplicadaException(titulo, usuarioId); }
            );

            assertEquals(
                    "Já existe uma reclamação ativa com o título 'Buraco na rua' para o usuário ID 1",
                    exception.getMessage()
            );
        }

        @Test
        void deveLancarComMensagemGenerica() {
            ReclamacaoDuplicadaException exception = assertThrows(
                    ReclamacaoDuplicadaException.class,
                    () -> { throw new ReclamacaoDuplicadaException(); }
            );

            assertEquals(
                    "Já existe uma reclamação duplicada",
                    exception.getMessage()
            );
        }
    }

    // ====================== RECLAMAÇÃO NÃO ENCONTRADA ======================
    @Test
    void deveLancarReclamacaoNaoEncontradaConstrutores() {
        // Long
        ReclamacaoNaoEncontradaException exId = assertThrows(
                ReclamacaoNaoEncontradaException.class,
                () -> { throw new ReclamacaoNaoEncontradaException(10L); }
        );
        assertEquals("Reclamação não encontrada com ID: 10", exId.getMessage());

        // String
        ReclamacaoNaoEncontradaException exMsg = assertThrows(
                ReclamacaoNaoEncontradaException.class,
                () -> { throw new ReclamacaoNaoEncontradaException("Custom not found"); }
        );
        assertEquals("Custom not found", exMsg.getMessage());

        // Sem args
        ReclamacaoNaoEncontradaException exPadrao = assertThrows(
                ReclamacaoNaoEncontradaException.class,
                () -> { throw new ReclamacaoNaoEncontradaException(); }
        );
        assertEquals("Reclamação não encontrada", exPadrao.getMessage());
    }

    // ====================== RECLAMAÇÃO DESATIVADA ======================
    @Test
    void deveLancarReclamacaoDesativadaConstrutores() {
        // Long
        ReclamacaoDesativadaException exId = assertThrows(
                ReclamacaoDesativadaException.class,
                () -> { throw new ReclamacaoDesativadaException(5L); }
        );
        assertEquals("Reclamação com ID 5 está desativada", exId.getMessage());

        // String
        ReclamacaoDesativadaException exMsg = assertThrows(
                ReclamacaoDesativadaException.class,
                () -> { throw new ReclamacaoDesativadaException("Custom"); }
        );
        assertEquals("Custom", exMsg.getMessage());

        // Sem args
        ReclamacaoDesativadaException exVazia = assertThrows(
                ReclamacaoDesativadaException.class,
                () -> { throw new ReclamacaoDesativadaException(); }
        );
        assertEquals("Reclamação está desativada", exVazia.getMessage());
    }

    // ====================== RECLAMAÇÃO ATUALIZAÇÃO INVÁLIDA ======================
    @Test
    void deveLancarReclamacaoAtualizacaoInvalidaConstrutores() {
        // Long
        ReclamacaoAtualizacaoInvalidaException exId = assertThrows(
                ReclamacaoAtualizacaoInvalidaException.class,
                () -> { throw new ReclamacaoAtualizacaoInvalidaException(7L); }
        );
        assertEquals("Atualização da reclamação com ID 7 é inválida", exId.getMessage());

        // String
        ReclamacaoAtualizacaoInvalidaException exMsg = assertThrows(
                ReclamacaoAtualizacaoInvalidaException.class,
                () -> { throw new ReclamacaoAtualizacaoInvalidaException("Custom invalid update"); }
        );
        assertEquals("Custom invalid update", exMsg.getMessage());
    }

    // ====================== RECLAMAÇÃO SEM PERMISSÃO ======================
    @Test
    void deveLancarReclamacaoSemPermissaoConstrutores() {
        // Construtor vazio
        ReclamacaoSemPermissaoException exVazia = assertThrows(
                ReclamacaoSemPermissaoException.class,
                () -> { throw new ReclamacaoSemPermissaoException(); }
        );
        assertEquals("Usuário não tem permissão para inativar esta reclamação", exVazia.getMessage());

        // Construtor com mensagem customizada
        ReclamacaoSemPermissaoException exMsg = assertThrows(
                ReclamacaoSemPermissaoException.class,
                () -> { throw new ReclamacaoSemPermissaoException("Mensagem customizada"); }
        );
        assertEquals("Mensagem customizada", exMsg.getMessage());
    }
}
