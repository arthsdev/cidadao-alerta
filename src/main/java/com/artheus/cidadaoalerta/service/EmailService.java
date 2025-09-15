package com.artheus.cidadaoalerta.service;

import com.artheus.cidadaoalerta.exception.email.EmailSendException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void enviarEmail(String destinatario, String assunto, String mensagem) {
        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(destinatario);
            email.setSubject(assunto);
            email.setText(mensagem);
            email.setFrom(System.getenv("MAIL_USERNAME"));
            mailSender.send(email);
        } catch (Exception e) {
            throw new EmailSendException("Falha ao enviar e-mail para: " + destinatario, e);
        }
    }
}
