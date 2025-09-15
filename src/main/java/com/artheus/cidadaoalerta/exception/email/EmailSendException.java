package com.artheus.cidadaoalerta.exception.email;

public class EmailSendException extends RuntimeException {

    public EmailSendException() {
        super("Erro ao enviar e-mail");
    }

    public EmailSendException(String mensagem) {
        super(mensagem);
    }

    public EmailSendException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
