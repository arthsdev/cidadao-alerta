package com.artheus.cidadaoalerta.service;

import com.artheus.cidadaoalerta.dto.AtualizacaoReclamacao;
import com.artheus.cidadaoalerta.dto.CadastroReclamacao;
import com.artheus.cidadaoalerta.dto.DetalhamentoReclamacao;
import com.artheus.cidadaoalerta.mapper.ReclamacaoMapper;
import com.artheus.cidadaoalerta.model.Reclamacao;
import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.repository.ReclamacaoRepository;
import com.artheus.cidadaoalerta.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReclamacaoService {

    private final UsuarioRepository usuarioRepository;
    private final ReclamacaoMapper reclamacaoMapper;
    private final ReclamacaoRepository reclamacaoRepository;

    public DetalhamentoReclamacao cadastrarReclamacao(CadastroReclamacao cadastroDto) {
        Usuario usuario = usuarioRepository.findById(cadastroDto.usuarioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuário não encontrado com ID: " + cadastroDto.usuarioId()));

        Reclamacao reclamacao = reclamacaoMapper.toEntity(cadastroDto, usuario);

        Reclamacao reclamacaoSalva = reclamacaoRepository.save(reclamacao);

        return reclamacaoMapper.toDetalhamentoDto(reclamacaoSalva);
    }

    public List<DetalhamentoReclamacao> listarReclamacoes() {
        return reclamacaoRepository.findByAtivoTrue()
                .stream()
                .map(reclamacaoMapper::toDetalhamentoDto)
                .collect(Collectors.toList());
    }

    public DetalhamentoReclamacao buscarPorId(Long id) {
        Reclamacao reclamacao = reclamacaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Reclamação não encontrada com ID: " + id));

        if (!reclamacao.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Reclamação está desativada");
        }

        return reclamacaoMapper.toDetalhamentoDto(reclamacao);
    }

    @Transactional
    public DetalhamentoReclamacao atualizarReclamacao(Long id, AtualizacaoReclamacao dto) {
        Reclamacao reclamacao = reclamacaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Reclamação não encontrada com ID: " + id));

        if (!reclamacao.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não é possível atualizar uma reclamação desativada");
        }

        reclamacaoMapper.updateReclamacaoFromDto(dto, reclamacao);
        return reclamacaoMapper.toDetalhamentoDto(reclamacao);
    }

    @Transactional
    public void desativarReclamacao(Long id) {
        Reclamacao reclamacao = reclamacaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Reclamação não encontrada com ID: " + id));

        if (!reclamacao.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Reclamação já está desativada");
        }

        reclamacao.setAtivo(false);
    }
}

