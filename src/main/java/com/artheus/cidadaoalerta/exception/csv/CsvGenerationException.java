package com.artheus.cidadaoalerta.exception.csv;

/** Exceção específica para falha na geração de CSV */
public class CsvGenerationException extends RuntimeException {

    public CsvGenerationException(String message) {
        super(message);
    }

    public CsvGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
