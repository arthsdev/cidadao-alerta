package com.artheus.cidadaoalerta.exception.reclamacao;

public class ReclamacaoDesativadaException extends RuntimeException {

    // Construtor padrão genérico (facilita testes)
    public ReclamacaoDesativadaException() {
        super("Reclamação está desativada");
    }

    // Construtor com ID da reclamação (mensagem detalhada)
    public ReclamacaoDesativadaException(Long id) {
        super("Reclamação com ID " + id + " está desativada");
    }

    // Construtor com mensagem customizada
    public ReclamacaoDesativadaException(String message){
        super(message);
    }
}
