package com.artheus.cidadaoalerta.controller;

import com.artheus.cidadaoalerta.dto.AtualizacaoReclamacao;
import com.artheus.cidadaoalerta.dto.CadastroReclamacao;
import com.artheus.cidadaoalerta.dto.DetalhamentoReclamacao;
import com.artheus.cidadaoalerta.mapper.ReclamacaoMapper;
import com.artheus.cidadaoalerta.security.ReclamacaoSecurity;
import com.artheus.cidadaoalerta.service.ReclamacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/reclamacoes")
@RequiredArgsConstructor
public class ReclamacaoController {

    private final ReclamacaoService reclamacaoService;
    private final ReclamacaoSecurity reclamacaoSecurity;
    private final ReclamacaoMapper reclamacaoMapper;

    @PostMapping
    public ResponseEntity<DetalhamentoReclamacao> cadastrarReclamacao(
            @RequestBody @Valid CadastroReclamacao dados
    ) {
        DetalhamentoReclamacao reclamacao = reclamacaoService.cadastrarReclamacao(dados);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(reclamacao.id())
                .toUri();

        return ResponseEntity.created(uri).body(reclamacao);
    }


    @GetMapping
    public ResponseEntity<List<DetalhamentoReclamacao>> listarReclamacoes() {
        List<DetalhamentoReclamacao> reclamacoes = reclamacaoService.listarReclamacoes();
        return ResponseEntity.ok(reclamacoes);
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
        var reclamacaoAtualizada = reclamacaoService.atualizarReclamacao(id, dto);
        return ResponseEntity.ok(reclamacaoAtualizada);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DetalhamentoReclamacao> atualizarParcialReclamacao(
            @PathVariable Long id,
            @RequestBody AtualizacaoReclamacao dto) {
        var reclamacaoAtualizada = reclamacaoService.atualizarReclamacao(id, dto);
        return ResponseEntity.ok(reclamacaoAtualizada);
    }
}
