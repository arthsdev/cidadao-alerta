package com.artheus.cidadaoalerta.listener;

import com.artheus.cidadaoalerta.event.UsuarioEvent;
import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.model.enums.TipoEventoUsuario;
import com.artheus.cidadaoalerta.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UsuarioEventListener {

    private final EmailService emailService;

    @Async
    @EventListener
    public void handleUsuarioEvent(UsuarioEvent event) {
        Usuario usuario = event.usuario();
        TipoEventoUsuario tipo = event.tipoEvento();

        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            log.warn("Não é possível enviar e-mail: usuário '{}' não possui e-mail cadastrado", usuario.getNome());
            return;
        }

        try {
            String assunto = gerarAssunto(tipo);
            String mensagem = gerarMensagem(usuario, tipo);

            emailService.enviarEmail(usuario.getEmail(), assunto, mensagem);
            log.info("E-mail enviado para {} sobre evento {}", usuario.getEmail(), tipo);
        } catch (Exception e) {
            log.error("Erro ao enviar e-mail para {}: {}", usuario.getEmail(), e.getMessage(), e);
        }
    }

    private String gerarAssunto(TipoEventoUsuario tipo) {
        return switch (tipo) {
            case CRIADO -> "Novo Usuário Registrado";
            case ATUALIZADO -> "Usuário Atualizado";
            case INATIVADO -> "Usuário Inativado";
            case DELETADO -> "Usuário Deletado";
        };
    }

    private String gerarMensagem(Usuario usuario, TipoEventoUsuario tipo) {
        return switch (tipo) {
            case CRIADO -> "O usuário '" + usuario.getNome() + "' foi registrado com sucesso.";
            case ATUALIZADO -> "O usuário '" + usuario.getNome() + "' foi atualizado com sucesso.";
            case INATIVADO -> "O usuário '" + usuario.getNome() + "' foi inativado.";
            case DELETADO -> "O usuário '" + usuario.getNome() + "' foi deletado.";
        };
    }
}
