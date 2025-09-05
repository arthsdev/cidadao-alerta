package com.artheus.cidadaoalerta.unit.exception.Csv;

import com.artheus.cidadaoalerta.exception.csv.CsvGenerationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvGenerationExceptionTest {

    @Test
    void deveCriarExcecaoComMensagem() {
        CsvGenerationException ex = new CsvGenerationException("Erro no CSV");
        assertEquals("Erro no CSV", ex.getMessage());
    }

    @Test
    void deveCriarExcecaoComMensagemECausa() {
        Throwable causa = new RuntimeException("Causa original");
        CsvGenerationException ex = new CsvGenerationException("Erro no CSV", causa);
        assertEquals("Erro no CSV", ex.getMessage());
        assertEquals(causa, ex.getCause());
    }
}
