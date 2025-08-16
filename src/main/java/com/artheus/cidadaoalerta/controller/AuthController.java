package com.artheus.cidadaoalerta.controller;

import com.artheus.cidadaoalerta.dto.RequisicaoLogin;
import com.artheus.cidadaoalerta.dto.RespostaLogin;
import com.artheus.cidadaoalerta.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authManager, JwtService jwtService) {
        this.authManager = authManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<RespostaLogin> login(@RequestBody RequisicaoLogin login) {
        try {
            var authToken = new UsernamePasswordAuthenticationToken(login.email(), login.senha());
            authManager.authenticate(authToken);

            String token = jwtService.gerarToken(login.email());

            // Retorna o token no padrão "Bearer <token>"
            return ResponseEntity.ok(new RespostaLogin("Bearer " + token));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(new RespostaLogin("Credenciais inválidas"));
        }
    }
}
