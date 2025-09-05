package com.artheus.cidadaoalerta.exception.reclamacao;

public class ReclamacaoDesativadaException extends RuntimeException {
    public ReclamacaoDesativadaException(Long id) {
        super("Reclamação com ID " + id + " está desativada");
    }

    // Construtor padrão genérico
    public ReclamacaoDesativadaException(String message){
        super(message);
    }
}