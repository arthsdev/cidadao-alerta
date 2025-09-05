package com.artheus.cidadaoalerta.exception.reclamacao;

public class ReclamacaoNaoEncontradaException extends RuntimeException {
    public ReclamacaoNaoEncontradaException(Long id) {
        super("Reclamação não encontrada com ID: " + id);
    }

    // Construtor padrão genérico
    public ReclamacaoNaoEncontradaException(String message){
        super(message);
    }
}