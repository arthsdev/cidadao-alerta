package com.artheus.cidadaoalerta.exception.usuario;

public class UsuarioSemPermissaoException extends RuntimeException {

    public UsuarioSemPermissaoException() {
        super("Usuário não tem permissão para executar esta ação");
    }

    // Construtor padrão genérico
    public UsuarioSemPermissaoException(String message) {
        super(message);
    }
}
