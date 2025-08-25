package com.artheus.cidadaoalerta.service;

import com.artheus.cidadaoalerta.dto.AtualizacaoReclamacao;
import com.artheus.cidadaoalerta.dto.CadastroReclamacao;
import com.artheus.cidadaoalerta.dto.DetalhamentoReclamacao;
import com.artheus.cidadaoalerta.dto.ReclamacaoPageResponse;
import com.artheus.cidadaoalerta.mapper.ReclamacaoMapper;
import com.artheus.cidadaoalerta.model.Reclamacao;
import com.artheus.cidadaoalerta.model.Usuario;
import com.artheus.cidadaoalerta.repository.ReclamacaoRepository;
import com.artheus.cidadaoalerta.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReclamacaoService {

    private final UsuarioRepository usuarioRepository;
    private final ReclamacaoMapper reclamacaoMapper;
    private final ReclamacaoRepository reclamacaoRepository;

    /**
     * Cadastra uma nova reclamação para um usuário.
     *
     * @param cadastroDto dados da reclamação
     * @return DTO detalhado da reclamação cadastrada
     * @throws ResponseStatusException se o usuário não for encontrado
     */
    public DetalhamentoReclamacao cadastrarReclamacao(CadastroReclamacao cadastroDto, Usuario usuario) {
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }

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
    public ReclamacaoPageResponse<DetalhamentoReclamacao> listarReclamacoes(Pageable pageable) {
        // Campos permitidos para ordenação
        Set<String> camposPermitidos = Set.of("dataCriacao", "titulo", "status");

        // Valida o sort enviado pelo usuário, se houver
        Sort sortValido = pageable.getSort().stream()
                .filter(order -> camposPermitidos.contains(order.getProperty()))
                .findFirst()
                .map(order -> Sort.by(order.getDirection(), order.getProperty()))
                .orElse(Sort.by(Sort.Direction.DESC, "dataCriacao")); // ordenação padrão

        // Garante um tamanho mínimo de página
        int pageSize = pageable.getPageSize() <= 0 ? 10 : pageable.getPageSize();
        int pageNumber = pageable.getPageNumber() < 0 ? 0 : pageable.getPageNumber();

        Pageable pageableValido = PageRequest.of(pageNumber, pageSize, sortValido);

        // Busca todas as reclamações ativas
        Page<DetalhamentoReclamacao> page = reclamacaoRepository.findByAtivoTrue(pageableValido)
                .map(reclamacaoMapper::toDetalhamentoDto);

        return new ReclamacaoPageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }




    /**
     * Busca uma reclamação pelo ID.
     *
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
     *
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
     *
     * @throws ResponseStatusException se a reclamação não existir, o usuário não tiver permissão ou já estiver inativa
     */
    @Transactional
    public void inativarReclamacao(Long id) {
        Reclamacao reclamacao = buscarReclamacaoAtivaPorId(id);
        Usuario usuarioLogado = obterUsuarioLogado();

        validarPermissao(usuarioLogado, reclamacao);

        reclamacao.setAtivo(false);
        reclamacaoRepository.save(reclamacao);
    }

    private Reclamacao buscarReclamacaoAtivaPorId(Long id) {
        return reclamacaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Reclamação não encontrada com ID: " + id));
    }

    private Usuario obterUsuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Usuário não encontrado"));
    }

    private void validarPermissao(Usuario usuario, Reclamacao reclamacao) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isOwner = reclamacao.getUsuario().getId().equals(usuario.getId());
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
    }


}



