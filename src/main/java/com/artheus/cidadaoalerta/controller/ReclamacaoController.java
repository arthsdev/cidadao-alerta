package com.artheus.cidadaoalerta.controller;

import com.artheus.cidadaoalerta.dto.AtualizacaoReclamacao;
import com.artheus.cidadaoalerta.dto.CadastroReclamacao;
import com.artheus.cidadaoalerta.dto.DetalhamentoReclamacao;
import com.artheus.cidadaoalerta.dto.ReclamacaoPageResponse;
import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.model.enums.CategoriaReclamacao;
import com.artheus.cidadaoalerta.model.enums.StatusReclamacao;
import com.artheus.cidadaoalerta.service.CsvService;
import com.artheus.cidadaoalerta.service.ReclamacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/reclamacoes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Gestão de Reclamações", description = "Endpoints para cadastro, listagem, atualização e exclusão de reclamações")
public class ReclamacaoController {

    private final ReclamacaoService reclamacaoService;
    private final CsvService csvService;

    // -------------------- CADASTRO --------------------

    @PostMapping
    @Operation(summary = "Cadastrar uma nova reclamação", description = "Permite cadastrar uma nova reclamação. Requer autenticação JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reclamação cadastrada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DetalhamentoReclamacao.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    public ResponseEntity<DetalhamentoReclamacao> cadastrarReclamacao(
            @RequestBody @Valid CadastroReclamacao cadastroDto,
            @AuthenticationPrincipal Usuario usuario) {

        DetalhamentoReclamacao reclamacao = reclamacaoService.cadastrarReclamacao(cadastroDto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(reclamacao.id())
                .toUri();

        return ResponseEntity.created(uri).body(reclamacao);
    }

    // -------------------- LISTAGEM --------------------

    @GetMapping
    @Operation(summary = "Listar reclamações", description = "Retorna uma página de reclamações, ordenadas por data de criação. Requer autenticação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de reclamações",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReclamacaoPageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content)
    })
    public ResponseEntity<ReclamacaoPageResponse<DetalhamentoReclamacao>> listarReclamacoes(
            @PageableDefault(page = 0, size = 10, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        ReclamacaoPageResponse<DetalhamentoReclamacao> response = reclamacaoService.listarReclamacoes(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar reclamação por ID", description = "Retorna os detalhes de uma reclamação específica. Requer autenticação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalhes da reclamação",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DetalhamentoReclamacao.class))),
            @ApiResponse(responseCode = "404", description = "Reclamação não encontrada", content = @Content)
    })
    public ResponseEntity<DetalhamentoReclamacao> buscarReclamacaoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(reclamacaoService.buscarPorId(id));
    }

    // -------------------- ATUALIZAÇÃO --------------------

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar reclamação", description = "Atualiza todos os dados de uma reclamação existente. Requer autenticação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reclamação atualizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DetalhamentoReclamacao.class))),
            @ApiResponse(responseCode = "400", description = "Campos obrigatórios ausentes", content = @Content),
            @ApiResponse(responseCode = "404", description = "Reclamação não encontrada", content = @Content)
    })
    public ResponseEntity<DetalhamentoReclamacao> atualizarReclamacao(
            @PathVariable Long id,
            @RequestBody @Valid AtualizacaoReclamacao dto) {

        return ResponseEntity.ok(reclamacaoService.atualizarReclamacao(id, dto));
    }

    // -------------------- ATUALIZAÇÃO PARCIAL --------------------
    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar parcialmente uma reclamação", description = "Atualiza parcialmente os dados de uma reclamação existente. Requer autenticação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reclamação atualizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DetalhamentoReclamacao.class))),
            @ApiResponse(responseCode = "404", description = "Reclamação não encontrada", content = @Content)
    })
    public ResponseEntity<DetalhamentoReclamacao> atualizarParcialReclamacao(
            @PathVariable Long id,
            @RequestBody AtualizacaoReclamacao dto) {

        return ResponseEntity.ok(reclamacaoService.atualizarParcialReclamacao(id, dto));
    }

    // -------------------- EXCLUSÃO --------------------

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @reclamacaoSecurity.isOwner(#id)")
    @Operation(summary = "Inativar reclamação", description = "Inativa uma reclamação existente. Requer autenticação e privilégios adequados")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Reclamação inativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Reclamação não encontrada", content = @Content)
    })
    public ResponseEntity<Void> inativarReclamacao(@PathVariable Long id) {
        reclamacaoService.inativarReclamacao(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------- EXPORTAÇÃO --------------------

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Exportar reclamações em CSV", description = "Exporta todas as reclamações em CSV. Apenas admins")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo CSV gerado com sucesso", content = @Content(mediaType = "text/csv")),
            @ApiResponse(responseCode = "401", description = "Não autorizado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso proibido", content = @Content)
    })
    public ResponseEntity<Resource> exportarReclamacoes(
            @RequestParam(required = false) StatusReclamacao status,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) CategoriaReclamacao categoria,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime dataFim
    ) {
        return csvService.gerarResponseCsv(status, usuarioId, categoria, dataInicio, dataFim);
    }

}

