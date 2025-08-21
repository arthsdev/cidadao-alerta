package com.artheus.cidadaoalerta.controller;

import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.dto.AtualizacaoUsuario;
import com.artheus.cidadaoalerta.dto.CadastroUsuario;
import com.artheus.cidadaoalerta.dto.DetalhamentoUsuario;
import com.artheus.cidadaoalerta.mapper.UsuarioMapper;
import com.artheus.cidadaoalerta.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;

    @PostMapping
    public ResponseEntity<DetalhamentoUsuario> cadastrarUsuario(
            @RequestBody @Valid CadastroUsuario dados) {

        DetalhamentoUsuario usuario = usuarioService.cadastrarUsuario(dados);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(usuario.id())
                .toUri();

        return ResponseEntity.created(uri).body(usuario);
    }

    @GetMapping
    public ResponseEntity<List<DetalhamentoUsuario>> listarUsuarios() {
        List<DetalhamentoUsuario> usuarios = usuarioService.listarUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetalhamentoUsuario> buscarUsuarioPorId(@PathVariable Long id) {
        DetalhamentoUsuario usuario = usuarioService.buscarPorId(id);
        return ResponseEntity.ok(usuario);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativarUsuario(@PathVariable Long id) {
        usuarioService.desativarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DetalhamentoUsuario> atualizarUsuario(
            @PathVariable Long id,
            @RequestBody @Valid AtualizacaoUsuario dto) {

        var usuarioAtualizado = usuarioService.atualizarUsuario(id, dto);
        return ResponseEntity.ok(usuarioAtualizado);
    }

    @GetMapping("/me")
    public ResponseEntity<DetalhamentoUsuario> buscarUsuarioLogado(Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal(); // cast direto para Usuario

        DetalhamentoUsuario dto = usuarioMapper.toDetalhamentoDto(usuario);
        return ResponseEntity.ok(dto);
    }

}
