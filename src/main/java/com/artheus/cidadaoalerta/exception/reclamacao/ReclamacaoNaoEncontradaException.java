package com.artheus.cidadaoalerta.exception.reclamacao;

public class ReclamacaoNaoEncontradaException extends RuntimeException {

    public ReclamacaoNaoEncontradaException() {
        super("Reclamação não encontrada");
    }

    public ReclamacaoNaoEncontradaException(Long id) {
        super("Reclamação não encontrada com ID: " + id);
    }

    //Construtor padrao generico
    public ReclamacaoNaoEncontradaException(String message) {
        super(message);
    }
}
