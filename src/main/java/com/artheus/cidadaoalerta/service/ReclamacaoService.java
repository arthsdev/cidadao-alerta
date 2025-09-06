package com.artheus.cidadaoalerta.service;

import com.artheus.cidadaoalerta.dto.AtualizacaoReclamacao;
import com.artheus.cidadaoalerta.dto.CadastroReclamacao;
import com.artheus.cidadaoalerta.dto.DetalhamentoReclamacao;
import com.artheus.cidadaoalerta.dto.ReclamacaoPageResponse;
import com.artheus.cidadaoalerta.exception.reclamacao.ReclamacaoAtualizacaoInvalidaException;
import com.artheus.cidadaoalerta.exception.reclamacao.ReclamacaoDesativadaException;
import com.artheus.cidadaoalerta.exception.reclamacao.ReclamacaoDuplicadaException;
import com.artheus.cidadaoalerta.exception.reclamacao.ReclamacaoNaoEncontradaException;
import com.artheus.cidadaoalerta.exception.usuario.UsuarioNaoAutenticadoException;
import com.artheus.cidadaoalerta.exception.usuario.UsuarioSemPermissaoException;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReclamacaoService {

    private final ReclamacaoRepository reclamacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReclamacaoMapper reclamacaoMapper;

    // ==================== MÉTODOS PÚBLICOS ====================

    public DetalhamentoReclamacao cadastrarReclamacao(CadastroReclamacao dto) {
        Usuario usuario = obterUsuarioLogado();
        validarReclamacaoDuplicada(dto.titulo(), usuario.getId());

        Reclamacao reclamacao = reclamacaoMapper.toEntity(dto, usuario);
        return reclamacaoMapper.toDetalhamentoDto(reclamacaoRepository.save(reclamacao));
    }

    public ReclamacaoPageResponse<DetalhamentoReclamacao> listarReclamacoes(Pageable pageable) {
        Pageable pageableValidado = ajustarPageable(pageable);
        Page<DetalhamentoReclamacao> page = reclamacaoRepository.findByAtivoTrue(pageableValidado)
                .map(reclamacaoMapper::toDetalhamentoDto);

        return mapearParaResponse(page);
    }

    // TODO: criar filtros por data (dataInicio e dataFim)
    // - dataInicio: incluir registros a partir desta data (00:00:00)
    // - dataFim: incluir registros até esta data (23:59:59)

    public DetalhamentoReclamacao buscarPorId(Long id) {
        return reclamacaoMapper.toDetalhamentoDto(buscarReclamacaoAtivaPorId(id));
    }

    @Transactional
    public DetalhamentoReclamacao atualizarReclamacao(Long id, AtualizacaoReclamacao dto) {
        Reclamacao reclamacao = buscarReclamacaoAtivaPorId(id);

        if (dto.getTitulo() == null || dto.getDescricao() == null || dto.getCategoriaReclamacao() == null) {
            throw new ReclamacaoAtualizacaoInvalidaException();
        }

        reclamacaoMapper.updateReclamacaoFromDto(dto, reclamacao);
        return reclamacaoMapper.toDetalhamentoDto(reclamacao);
    }

    @Transactional
    public DetalhamentoReclamacao atualizarParcialReclamacao(Long id, AtualizacaoReclamacao dto) {
        Reclamacao reclamacao = buscarReclamacaoAtivaPorId(id);

        // Atualiza apenas campos não nulos
        if (dto.getTitulo() != null) reclamacao.setTitulo(dto.getTitulo());
        if (dto.getDescricao() != null) reclamacao.setDescricao(dto.getDescricao());
        if (dto.getCategoriaReclamacao() != null) reclamacao.setCategoriaReclamacao(dto.getCategoriaReclamacao());
        if (dto.getLocalizacao() != null) reclamacao.setLocalizacao(dto.getLocalizacao());

        return reclamacaoMapper.toDetalhamentoDto(reclamacao);
    }

    @Transactional
    public void inativarReclamacao(Long id) {
        Reclamacao reclamacao = buscarReclamacaoAtivaPorId(id);
        Usuario usuarioLogado = obterUsuarioLogado();

        if (!usuarioTemPermissaoParaInativar(usuarioLogado, reclamacao)) {
            throw new UsuarioSemPermissaoException();
        }

        reclamacao.setAtivo(false);
        reclamacaoRepository.save(reclamacao);
    }

    // ==================== MÉTODOS PRIVADOS AUXILIARES ====================

    private void validarReclamacaoDuplicada(String titulo, Long usuarioId) {
        boolean existe = reclamacaoRepository.findByTituloAndUsuarioIdAndAtivoTrue(titulo, usuarioId).isPresent();
        if (existe) throw new ReclamacaoDuplicadaException(titulo, usuarioId);
    }

    private Usuario obterUsuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) throw new UsuarioNaoAutenticadoException();

        return usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(UsuarioNaoAutenticadoException::new);
    }

    private boolean usuarioTemPermissaoParaInativar(Usuario usuario, Reclamacao reclamacao) {
        return reclamacao.getUsuario().getId().equals(usuario.getId()) ||
                usuario.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private Pageable ajustarPageable(Pageable pageable) {
        Set<String> camposPermitidos = Set.of("dataCriacao", "titulo", "status");

        Sort sortValido = pageable.getSort().stream()
                .filter(order -> camposPermitidos.contains(order.getProperty()))
                .map(order -> new Sort.Order(order.getDirection(), order.getProperty()))
                .collect(Collectors.collectingAndThen(Collectors.toList(),
                        list -> list.isEmpty()
                                ? Sort.by(Sort.Direction.DESC, "dataCriacao")
                                : Sort.by(list)));

        int pageSize = pageable.getPageSize() <= 0 ? 10 :
                Math.min(pageable.getPageSize(), 10);
        int pageNumber = pageable.getPageNumber() < 0 ? 0 : pageable.getPageNumber();

        return PageRequest.of(pageNumber, pageSize, sortValido);
    }

    private Reclamacao buscarReclamacaoAtivaPorId(Long id) {
        Reclamacao reclamacao = reclamacaoRepository.findById(id)
                .orElseThrow(() -> new ReclamacaoNaoEncontradaException(id));

        if (!reclamacao.isAtivo()) throw new ReclamacaoDesativadaException(id);
        return reclamacao;
    }

    private ReclamacaoPageResponse<DetalhamentoReclamacao> mapearParaResponse(Page<DetalhamentoReclamacao> page) {
        return new ReclamacaoPageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast());
    }



}
