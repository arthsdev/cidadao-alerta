package com.artheus.cidadaoalerta.controller;

import com.artheus.cidadaoalerta.dto.RequisicaoLogin;
import com.artheus.cidadaoalerta.dto.RespostaLogin;
import com.artheus.cidadaoalerta.security.JwtService;
import com.artheus.cidadaoalerta.security.UsuarioDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UsuarioDetailsService usuarioDetailsService;

    public AuthController(AuthenticationManager authManager, JwtService jwtService, UsuarioDetailsService usuarioDetailsService) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.usuarioDetailsService = usuarioDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<RespostaLogin> login(@RequestBody RequisicaoLogin login) {
        try {
            var authToken = new UsernamePasswordAuthenticationToken(login.email(), login.senha());
            authManager.authenticate(authToken);

            // carrega os detalhes do usuário autenticado
            UserDetails userDetails = usuarioDetailsService.loadUserByUsername(login.email());

            // gera token com role incluída
            String token = jwtService.gerarToken(userDetails);

            return ResponseEntity.ok(new RespostaLogin(token));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(new RespostaLogin("Credenciais inválidas"));
        }
    }
}
