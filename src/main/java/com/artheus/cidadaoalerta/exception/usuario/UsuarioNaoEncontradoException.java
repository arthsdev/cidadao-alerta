package com.artheus.cidadaoalerta.exception.usuario;

public class UsuarioNaoEncontradoException extends RuntimeException {
    public UsuarioNaoEncontradoException(String email) {
        super("Usuário não encontrado com email: " + email);
    }

    // Construtor padrão genérico
    public UsuarioNaoEncontradoException() {
        super("Usuário não encontrado");
    }
}