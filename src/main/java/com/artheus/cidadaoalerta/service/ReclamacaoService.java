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

    // ==================== MÉTODOS PÚBLICOS (ACESSÍVEIS PELO CONTROLLER) ====================

    /**
     * Cadastra uma nova reclamação associada ao usuário autenticado.
     * Valida duplicidade por título e usuário.
     */
    public DetalhamentoReclamacao cadastrarReclamacao(CadastroReclamacao cadastroDto, Usuario usuario) {
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }

        validarReclamacaoDuplicada(cadastroDto.titulo(), usuario.getId());

        Reclamacao reclamacao = reclamacaoMapper.toEntity(cadastroDto, usuario);
        return reclamacaoMapper.toDetalhamentoDto(reclamacaoRepository.save(reclamacao));
    }

    /**
     * Lista todas as reclamações ativas com suporte a paginação e ordenação.
     */
    public ReclamacaoPageResponse<DetalhamentoReclamacao> listarReclamacoes(Pageable pageable) {
        Pageable pageableValidado = ajustarPageable(pageable);

        Page<DetalhamentoReclamacao> page = reclamacaoRepository.findByAtivoTrue(pageableValidado)
                .map(reclamacaoMapper::toDetalhamentoDto);

        return mapearParaResponse(page);
    }

    /**
     * Busca uma reclamação ativa pelo ID.
     * Lança exceção se não existir ou estiver desativada.
     */
    public DetalhamentoReclamacao buscarPorId(Long id) {
        Reclamacao reclamacao = buscarReclamacaoAtivaPorId(id);
        return reclamacaoMapper.toDetalhamentoDto(reclamacao);
    }

    /**
     * Atualiza completamente uma reclamação existente (PUT).
     * Todos os campos do DTO são obrigatórios. Campos nulos no DTO
     * resultarão em erro de validação (BAD_REQUEST).
     */
    @Transactional
    public DetalhamentoReclamacao atualizarReclamacao(Long id, AtualizacaoReclamacao dto) {
        Reclamacao reclamacao = buscarReclamacaoAtivaPorId(id);

        if (dto.getTitulo() == null || dto.getDescricao() == null || dto.getCategoriaReclamacao() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Todos os campos são obrigatórios para atualização completa");
        }

        reclamacaoMapper.updateReclamacaoFromDto(dto, reclamacao);
        return reclamacaoMapper.toDetalhamentoDto(reclamacao);
    }

    /**
     * Atualiza parcialmente uma reclamação existente (PATCH).
     * Apenas os campos presentes no DTO são modificados; campos nulos
     * permanecem inalterados na entidade.
     */
    @Transactional
    public DetalhamentoReclamacao atualizarParcialReclamacao(Long id, AtualizacaoReclamacao dto) {
        Reclamacao reclamacao = buscarReclamacaoAtivaPorId(id);

        if (dto.getTitulo() != null) reclamacao.setTitulo(dto.getTitulo());
        if (dto.getDescricao() != null) reclamacao.setDescricao(dto.getDescricao());
        if (dto.getCategoriaReclamacao() != null) reclamacao.setCategoriaReclamacao(dto.getCategoriaReclamacao());
        if (dto.getLocalizacao() != null) reclamacao.setLocalizacao(dto.getLocalizacao());

        return reclamacaoMapper.toDetalhamentoDto(reclamacao);
    }



    /**
     * Inativa (soft delete) uma reclamação.
     * Permite apenas se for o dono ou um administrador.
     */
    @Transactional
    public void inativarReclamacao(Long id) {
        Reclamacao reclamacao = buscarReclamacaoAtivaPorId(id);
        Usuario usuarioLogado = obterUsuarioLogado();

        if (!usuarioTemPermissaoParaInativar(usuarioLogado, reclamacao)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário não tem permissão para inativar esta reclamação");
        }

        reclamacao.setAtivo(false);
        reclamacaoRepository.save(reclamacao);
    }

    // ==================== MÉTODOS AUXILIARES PRIVADOS (USADOS INTERNAMENTE) ====================

    // ---- Validações internas ----

    /**
     * Verifica se já existe uma reclamação ativa com o mesmo título para o mesmo usuário.
     */
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

    // ---- Obtenção de dados do usuário logado ----

    /**
     * Obtém o usuário logado a partir do contexto de segurança (Spring Security).
     */
    private Usuario obterUsuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Usuário não encontrado"));
    }

    /**
     * Verifica se o usuário logado pode inativar a reclamação (dono ou admin).
     */
    private boolean usuarioTemPermissaoParaInativar(Usuario usuario, Reclamacao reclamacao) {
        return reclamacao.getUsuario().getId().equals(usuario.getId()) ||
                usuario.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // ---- Ajuste de paginação e ordenação ----

    /**
     * Valida e ajusta o Pageable recebido, aplicando ordenação apenas por campos permitidos.
     */
    private Pageable ajustarPageable(Pageable pageable) {
        Set<String> camposPermitidos = Set.of("dataCriacao", "titulo", "status");

        Sort sortValido = pageable.getSort().stream()
                .filter(order -> camposPermitidos.contains(order.getProperty()))
                .findFirst()
                .map(order -> Sort.by(order.getDirection(), order.getProperty()))
                .orElse(Sort.by(Sort.Direction.DESC, "dataCriacao"));

        int pageSize = pageable.getPageSize() <= 0 ? 10 : pageable.getPageSize();
        int pageNumber = pageable.getPageNumber() < 0 ? 0 : pageable.getPageNumber();

        return PageRequest.of(pageNumber, pageSize, sortValido);
    }

    // ---- Busca interna de reclamação ----

    /**
     * Busca uma reclamação ativa pelo ID.
     * Lança exceção se não existir ou estiver desativada.
     */
    private Reclamacao buscarReclamacaoAtivaPorId(Long id) {
        Reclamacao reclamacao = reclamacaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Reclamação não encontrada com ID: " + id));

        if (!reclamacao.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reclamação está desativada");
        }

        return reclamacao;
    }

    // ---- Mapeamento de resposta paginada ----

    /**
     * Constrói uma resposta paginada customizada com os dados da reclamação.
     */
    private ReclamacaoPageResponse<DetalhamentoReclamacao> mapearParaResponse(Page<DetalhamentoReclamacao> page) {
        return new ReclamacaoPageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast());
    }
}
