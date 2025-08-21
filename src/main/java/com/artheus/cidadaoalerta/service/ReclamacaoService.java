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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReclamacaoService {

    private final UsuarioRepository usuarioRepository;
    private final ReclamacaoMapper reclamacaoMapper;
    private final ReclamacaoRepository reclamacaoRepository;

    /**
     * Cadastra uma nova reclamação para um usuário.
     * @param cadastroDto dados da reclamação
     * @return DTO detalhado da reclamação cadastrada
     * @throws ResponseStatusException se o usuário não for encontrado
     */
    public DetalhamentoReclamacao cadastrarReclamacao(CadastroReclamacao cadastroDto, Usuario usuario) {

        // Valida se já existe uma reclamação ativa com o mesmo título para este usuário
        validarReclamacaoDuplicada(cadastroDto.titulo(), usuario.getId());

        // Cria e salva a reclamação associada ao usuário logado
        Reclamacao reclamacao = reclamacaoMapper.toEntity(cadastroDto, usuario);
        return reclamacaoMapper.toDetalhamentoDto(reclamacaoRepository.save(reclamacao));
    }

    private void validarReclamacaoDuplicada(String titulo, Long usuarioId) {
        boolean existe = reclamacaoRepository
                .findByTituloAndUsuarioIdAndAtivoTrue(titulo, usuarioId)
                .isPresent();

        if (existe) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe uma reclamação ativa com esse título para este usuário."
            );
        }
    }


    /**
     * Retorna todas as reclamações ativas.
     */
    public List<DetalhamentoReclamacao> listarReclamacoes() {
        return reclamacaoRepository.findByAtivoTrue()
                .stream()
                .map(reclamacaoMapper::toDetalhamentoDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca uma reclamação pelo ID.
     * @throws ResponseStatusException se não encontrada ou desativada
     */
    public DetalhamentoReclamacao buscarPorId(Long id) {
        Reclamacao reclamacao = reclamacaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Reclamação não encontrada com ID: " + id));

        if (!reclamacao.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reclamação está desativada");
        }

        return reclamacaoMapper.toDetalhamentoDto(reclamacao);
    }

    /**
     * Atualiza uma reclamação existente.
     * @throws ResponseStatusException se não encontrada ou desativada
     */
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

    /**
     * Inativa uma reclamação. Apenas o dono ou um administrador podem executar.
     * @throws ResponseStatusException se a reclamação não existir, o usuário não tiver permissão ou já estiver inativa
     */
    @Transactional
    public void inativarReclamacao(Long id) {
        Reclamacao reclamacao = reclamacaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Reclamação não encontrada com ID: " + id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuarioLogado = (Usuario) auth.getPrincipal();

        boolean isOwner = reclamacao.getUsuario().getId().equals(usuarioLogado.getId());
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Usuário não pode deletar essa reclamação");
        }

        if (!reclamacao.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Reclamação já está desativada");
        }

        reclamacao.setAtivo(false);
    }

}



