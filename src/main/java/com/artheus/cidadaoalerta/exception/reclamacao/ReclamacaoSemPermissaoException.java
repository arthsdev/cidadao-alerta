package com.artheus.cidadaoalerta.exception.reclamacao;

public class ReclamacaoSemPermissaoException extends RuntimeException {
    public ReclamacaoSemPermissaoException() {
        super("Usuário não tem permissão para inativar esta reclamação");
    }

    // Construtor padrão genérico
    public ReclamacaoSemPermissaoException(String message){
        super(message);
    }
}