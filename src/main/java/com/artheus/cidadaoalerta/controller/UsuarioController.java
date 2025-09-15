package com.artheus.cidadaoalerta.controller;

import com.artheus.cidadaoalerta.dto.RespostaEmail;
import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.dto.AtualizacaoUsuario;
import com.artheus.cidadaoalerta.dto.CadastroUsuario;
import com.artheus.cidadaoalerta.dto.DetalhamentoUsuario;
import com.artheus.cidadaoalerta.mapper.UsuarioMapper;
import com.artheus.cidadaoalerta.service.EmailService;
import com.artheus.cidadaoalerta.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth") // todos endpoints exigem auth por padrão
@Tag(name = "Gestão de Usuários", description = "Endpoints para cadastro, listagem, atualização, busca e inativação de usuários")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;
    private final EmailService emailService;

    @PostMapping
    @Operation(summary = "Cadastrar um novo usuário",
            description = "Endpoint público para cadastro de novos usuários (sem autenticação)",
            security = {}) // remove bearerAuth
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados necessários para cadastrar um usuário",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CadastroUsuario.class),
                    examples = @ExampleObject(value = "{ \"nome\": \"João Silva\", \"email\": \"joao@email.com\", \"senha\": \"123456\" }")
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DetalhamentoUsuario.class),
                            examples = @ExampleObject(value = "{ \"id\": 1, \"nome\": \"João Silva\", \"email\": \"joao@email.com\" }")
                    )),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<DetalhamentoUsuario> cadastrarUsuario(
            @RequestBody @Valid CadastroUsuario dados) {
        log.info("Recebida requisição para criar usuário: {}", dados.email());
        DetalhamentoUsuario usuario = usuarioService.cadastrarUsuario(dados);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(usuario.id()).toUri();
        log.info("Usuário criado com sucesso: id={}", usuario.id());
        return ResponseEntity.created(uri).body(usuario);
    }

    @GetMapping
    @Operation(summary = "Listar usuários",
            description = "Lista todos os usuários ativos. Requer autenticação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de usuários",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DetalhamentoUsuario.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<List<DetalhamentoUsuario>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuário por ID",
            description = "Retorna os detalhes de um usuário específico. Requer autenticação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalhes do usuário",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DetalhamentoUsuario.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<DetalhamentoUsuario> buscarUsuarioPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Inativar usuário (soft delete)",
            description = "Inativa um usuário existente. Requer autenticação e privilégios adequados")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuário inativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<Void> inativarUsuario(@PathVariable Long id) {
        usuarioService.inativarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar dados de um usuário",
            description = "Atualiza informações de um usuário existente. Requer autenticação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DetalhamentoUsuario.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<DetalhamentoUsuario> atualizarUsuario(
            @PathVariable Long id,
            @RequestBody @Valid AtualizacaoUsuario dto) {
        log.info("Recebida requisição para atualizar usuário: id={}", id);
        DetalhamentoUsuario usuarioAtualizado = usuarioService.atualizarUsuario(id, dto);
        log.info("Usuário atualizado com sucesso: id={}", id);
        return ResponseEntity.ok(usuarioAtualizado);
    }

    @GetMapping("/me")
    @Operation(summary = "Buscar dados do usuário autenticado",
            description = "Retorna os dados do usuário logado (token JWT)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados do usuário logado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DetalhamentoUsuario.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<DetalhamentoUsuario> buscarUsuarioLogado(Authentication authentication) {
        Usuario usuario = (Usuario) authentication.getPrincipal();
        return ResponseEntity.ok(usuarioMapper.toDetalhamentoDto(usuario));
    }
}