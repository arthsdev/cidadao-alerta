package com.artheus.cidadaoalerta.controller;

import com.artheus.cidadaoalerta.dto.RequisicaoLogin;
import com.artheus.cidadaoalerta.dto.RespostaLogin;
import com.artheus.cidadaoalerta.security.JwtService;
import com.artheus.cidadaoalerta.security.UsuarioDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Endpoint para login e geração de token JWT")
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
    @Operation(summary = "Autenticar usuário e gerar token JWT",
            description = "Recebe email e senha, autentica o usuário e retorna um JWT válido para acesso aos endpoints protegidos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RespostaLogin.class))),
            @ApiResponse(responseCode = "400", description = "Campos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RespostaLogin.class)))
    })
    public ResponseEntity<RespostaLogin> login(@RequestBody @Valid RequisicaoLogin login) {
        try {
            var authToken = new UsernamePasswordAuthenticationToken(login.email(), login.senha());
            authManager.authenticate(authToken);

            UserDetails userDetails = usuarioDetailsService.loadUserByUsername(login.email());
            String token = jwtService.gerarToken(userDetails);

            return ResponseEntity.ok(new RespostaLogin("Login realizado", token));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(new RespostaLogin("Credenciais inválidas", null));
        }
    }
}
