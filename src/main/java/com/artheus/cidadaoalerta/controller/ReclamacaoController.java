package com.artheus.cidadaoalerta.controller;

import com.artheus.cidadaoalerta.dto.AtualizacaoReclamacao;
import com.artheus.cidadaoalerta.dto.CadastroReclamacao;
import com.artheus.cidadaoalerta.dto.DetalhamentoReclamacao;
import com.artheus.cidadaoalerta.dto.ReclamacaoPageResponse;
import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.service.ReclamacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/reclamacoes")
@RequiredArgsConstructor
public class ReclamacaoController {

    private final ReclamacaoService reclamacaoService;

    @PostMapping
    public ResponseEntity<DetalhamentoReclamacao> cadastrarReclamacao(
            @RequestBody @Valid CadastroReclamacao cadastroDto,
            @AuthenticationPrincipal Usuario usuario
    ) {
        DetalhamentoReclamacao reclamacao = reclamacaoService.cadastrarReclamacao(cadastroDto, usuario);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(reclamacao.id())
                .toUri();

        return ResponseEntity.created(uri).body(reclamacao);
    }




    @GetMapping
    public ResponseEntity<ReclamacaoPageResponse<DetalhamentoReclamacao>> listarReclamacoes(
            @PageableDefault(size = 5, sort = "dataCriacao", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<DetalhamentoReclamacao> reclamacoes = reclamacaoService.listarReclamacoes(pageable);

        ReclamacaoPageResponse<DetalhamentoReclamacao> response = new ReclamacaoPageResponse<>(
                reclamacoes.getContent(),
                reclamacoes.getNumber(),
                reclamacoes.getSize(),
                reclamacoes.getTotalElements(),
                reclamacoes.getTotalPages(),
                reclamacoes.isLast()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetalhamentoReclamacao> buscarReclamacaoPorId(@PathVariable Long id) {
        DetalhamentoReclamacao reclamacao = reclamacaoService.buscarPorId(id);
        return ResponseEntity.ok(reclamacao);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @reclamacaoSecurity.isOwner(#id)")
    public ResponseEntity<Void> inativarReclamacao(@PathVariable Long id) {
        reclamacaoService.inativarReclamacao(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<DetalhamentoReclamacao> atualizarReclamacao(
            @PathVariable Long id,
            @RequestBody @Valid AtualizacaoReclamacao dto
    ) {
        DetalhamentoReclamacao reclamacaoAtualizada = reclamacaoService.atualizarReclamacao(id, dto);
        return ResponseEntity.ok(reclamacaoAtualizada);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DetalhamentoReclamacao> atualizarParcialReclamacao(
            @PathVariable Long id,
            @RequestBody AtualizacaoReclamacao dto) {
        DetalhamentoReclamacao reclamacaoAtualizada = reclamacaoService.atualizarReclamacao(id, dto);
        return ResponseEntity.ok(reclamacaoAtualizada);
    }
}
