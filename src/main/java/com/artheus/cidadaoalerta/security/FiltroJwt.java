package com.artheus.cidadaoalerta.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro responsável por interceptar requisições e validar JWT.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FiltroJwt extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = extrairToken(request);

        if (token != null) {
            try {
                if (jwtService.validarToken(token)) {
                    autenticarUsuario(token);
                    log.debug("Token válido para requisição [{} {}]", request.getMethod(), request.getRequestURI());
                } else {
                    log.warn("Token inválido ou expirado - IP: {}, Rota: {}",
                            request.getRemoteAddr(), request.getRequestURI());
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido ou expirado");
                    return;
                }
            } catch (Exception e) {
                log.warn("Falha na autenticação com token - IP: {}, Rota: {}, Erro: {}",
                        request.getRemoteAddr(), request.getRequestURI(), e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
                return;
            }
        } else {
            log.debug("Requisição sem token - [{} {}]", request.getMethod(), request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrai o token JWT do cabeçalho Authorization.
     */
    private String extrairToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Autentica o usuário no contexto do Spring Security.
     * Lança ServletException se o usuário não existir no banco.
     */
    private void autenticarUsuario(String token) throws ServletException {
        String email = jwtService.getEmailUsuario(token);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.info("Usuário autenticado: {}", email);

            } catch (Exception e) {
                // Usuário do token não encontrado -> retorna 401
                log.warn("Usuário do token não encontrado: {}", email);
                throw new ServletException("Usuário não encontrado");
            }
        }
    }
}
