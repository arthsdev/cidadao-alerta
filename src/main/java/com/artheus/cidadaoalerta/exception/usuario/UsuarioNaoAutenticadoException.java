package com.artheus.cidadaoalerta.exception.usuario;

public class UsuarioNaoAutenticadoException extends RuntimeException {
    public UsuarioNaoAutenticadoException() {
        super("Usuário não autenticado");
    }

    // Construtor padrão genérico
    public UsuarioNaoAutenticadoException(String message) {
        super(message);
    }
}