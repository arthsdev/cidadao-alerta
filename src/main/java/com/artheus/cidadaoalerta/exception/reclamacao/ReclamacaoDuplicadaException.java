package com.artheus.cidadaoalerta.exception.reclamacao;

public class ReclamacaoDuplicadaException extends RuntimeException {

    //construtor padrao generico
    public ReclamacaoDuplicadaException() {
        super("Já existe uma reclamação duplicada");
    }

    public ReclamacaoDuplicadaException(String titulo, Long usuarioId) {
        super("Já existe uma reclamação ativa com o título '" + titulo + "' para o usuário ID " + usuarioId);
    }
}
