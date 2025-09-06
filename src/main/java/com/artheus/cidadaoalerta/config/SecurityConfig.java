package com.artheus.cidadaoalerta.config;

import com.artheus.cidadaoalerta.security.FiltroJwt;
import com.artheus.cidadaoalerta.security.UsuarioDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final FiltroJwt filtroJwt;
    private final UsuarioDetailsService usuarioDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // desabilita CSRF (API REST)
                .csrf(csrf -> csrf.disable())

                // configura autorização das rotas
                .authorizeHttpRequests(auth -> auth
                        // rotas públicas (Swagger e autenticação)
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/auth/**"
                        ).permitAll()

                        // cadastro de usuário é público
                        .requestMatchers(HttpMethod.POST, "/usuarios/**").permitAll()

                        // somente ADMIN pode exportar CSV
                        .requestMatchers(HttpMethod.GET, "/reclamacoes/export").hasRole("ADMIN")

                        // qualquer outra rota exige autenticação
                        .anyRequest().authenticated()
                )

                // tratamento centralizado para erros de autenticação/autorização
                .exceptionHandling(ex -> ex
                        // 401 → não autenticado
                        .authenticationEntryPoint((req, res, e) ->
                                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
                        // 403 → autenticado, mas sem permissão
                        .accessDeniedHandler((req, res, e) ->
                                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden"))
                )

                // aplica o filtro JWT antes do filtro padrão do Spring
                .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(usuarioDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
