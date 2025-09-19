package com.artheus.cidadaoalerta.listener;

import com.artheus.cidadaoalerta.event.ReclamacaoEvent;
import com.artheus.cidadaoalerta.model.Reclamacao;
import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.model.enums.TipoEventoReclamacao;
import com.artheus.cidadaoalerta.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReclamacaoEventListener {

    private static final String PREFIXO_RECLAMACAO = "A reclamação '";
    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReclamacaoEvent(ReclamacaoEvent event) {
        Reclamacao reclamacao = event.reclamacao();
        TipoEventoReclamacao tipo = event.tipoEvento();
        Usuario usuario = reclamacao.getUsuario();

        // Valida e-mail
        String email = (usuario.getEmail() != null) ? usuario.getEmail().trim() : null;
        if (email == null || email.isEmpty()) {
            log.warn("Não é possível enviar e-mail: usuário '{}' não possui e-mail cadastrado", usuario.getNome());
            return;
        }

        try {
            // Gera assunto e mensagem
            String assunto = gerarAssunto(tipo);
            String mensagem = gerarMensagem(reclamacao, tipo);

            // Envia e-mail
            emailService.enviarEmail(email, assunto, mensagem);
            log.info("E-mail enviado para {} sobre evento {}", email, tipo);

        } catch (Exception e) {
            log.error("Erro ao enviar e-mail para {}: {}", email, e.getMessage(), e);
        }
    }

    private String gerarAssunto(TipoEventoReclamacao tipo) {
        return switch (tipo) {
            case CRIADA -> "Nova Reclamação Registrada";
            case ATUALIZADA -> "Reclamação Atualizada";
            case INATIVADA -> "Reclamação Inativada";
            case CONCLUIDA -> "Reclamação Concluída";
        };
    }

    private String gerarMensagem(Reclamacao reclamacao, TipoEventoReclamacao tipo) {
        String titulo = reclamacao.getTitulo();
        return switch (tipo) {
            case CRIADA -> PREFIXO_RECLAMACAO + titulo + "' foi cadastrada com sucesso.";
            case ATUALIZADA -> PREFIXO_RECLAMACAO + titulo + "' foi atualizada.";
            case INATIVADA -> PREFIXO_RECLAMACAO + titulo + "' foi inativada.";
            case CONCLUIDA -> PREFIXO_RECLAMACAO + titulo + "' foi concluída.";
        };
    }
}
