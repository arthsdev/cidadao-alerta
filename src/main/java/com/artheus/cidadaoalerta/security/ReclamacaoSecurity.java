package com.artheus.cidadaoalerta.security;

import com.artheus.cidadaoalerta.repository.ReclamacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReclamacaoSecurity {

    private final ReclamacaoRepository reclamacaoRepository;


    public boolean isOwner(Long reclamacaoId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;

        String emailUsuarioLogado = auth.getName();
        return reclamacaoRepository.existsByIdAndUsuario_Email(reclamacaoId, emailUsuarioLogado);
    }

    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    public boolean canDelete(Long reclamacaoId) {
        return isOwner(reclamacaoId) || isAdmin();
    }
}

