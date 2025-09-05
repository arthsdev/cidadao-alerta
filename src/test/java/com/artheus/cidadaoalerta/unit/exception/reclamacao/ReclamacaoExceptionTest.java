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
                    () -> { throw new ReclamacaoDuplicadaException(); } // <<== lança aqui
            );

            assertEquals(
                    "Já existe uma reclamação duplicada",
                    exception.getMessage()
            );
        }


    }

    // ====================== RECLAMAÇÃO NÃO ENCONTRADA ======================
    @Test
    void deveLancarReclamacaoNaoEncontrada() {
        ReclamacaoNaoEncontradaException exception = assertThrows(
                ReclamacaoNaoEncontradaException.class,
                () -> { throw new ReclamacaoNaoEncontradaException(10L); }
        );

        assertEquals(
                "Reclamação não encontrada com ID: 10",
                exception.getMessage()
        );
    }

    // ====================== RECLAMAÇÃO DESATIVADA ======================
    @Test
    void deveLancarReclamacaoDesativada() {
        ReclamacaoDesativadaException exception = assertThrows(
                ReclamacaoDesativadaException.class,
                () -> { throw new ReclamacaoDesativadaException(5L); }
        );

        assertEquals(
                "Reclamação com ID 5 está desativada",
                exception.getMessage()
        );
    }

    // ====================== RECLAMAÇÃO ATUALIZAÇÃO INVÁLIDA ======================
    @Test
    void deveLancarReclamacaoAtualizacaoInvalida() {
        ReclamacaoAtualizacaoInvalidaException exception = assertThrows(
                ReclamacaoAtualizacaoInvalidaException.class,
                () -> { throw new ReclamacaoAtualizacaoInvalidaException(7L); }
        );

        assertEquals(
                "Atualização da reclamação com ID 7 é inválida",
                exception.getMessage()
        );
    }

    // ====================== RECLAMAÇÃO SEM PERMISSÃO ======================
    @Test
    void deveLancarReclamacaoSemPermissao() {
        // construtor vazio
        ReclamacaoSemPermissaoException exceptionVazia = assertThrows(
                ReclamacaoSemPermissaoException.class,
                () -> { throw new ReclamacaoSemPermissaoException(); }
        );
        assertEquals("Usuário não tem permissão para inativar esta reclamação", exceptionVazia.getMessage());

        // construtor com mensagem customizada
        ReclamacaoSemPermissaoException exceptionComMensagem = assertThrows(
                ReclamacaoSemPermissaoException.class,
                () -> { throw new ReclamacaoSemPermissaoException("Mensagem customizada"); }
        );
        assertEquals("Mensagem customizada", exceptionComMensagem.getMessage());
    }


}
