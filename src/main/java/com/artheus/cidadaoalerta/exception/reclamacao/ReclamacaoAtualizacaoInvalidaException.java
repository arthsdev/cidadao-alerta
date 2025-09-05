package com.artheus.cidadaoalerta.exception.reclamacao;

public class ReclamacaoAtualizacaoInvalidaException extends RuntimeException {
    public ReclamacaoAtualizacaoInvalidaException() {
        super("Todos os campos são obrigatórios para atualização completa");
    }

    // Construtor padrão genérico
    public ReclamacaoAtualizacaoInvalidaException(String message) {
        super(message);
    }

    //recebe um Long e monta a mensagem automaticamente
    public ReclamacaoAtualizacaoInvalidaException(Long id) {
        super("Atualização da reclamação com ID " + id + " é inválida");
    }
}
