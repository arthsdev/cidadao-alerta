package com.artheus.cidadaoalerta.repository;

import com.artheus.cidadaoalerta.model.Reclamacao;
import com.artheus.cidadaoalerta.model.enums.CategoriaReclamacao;
import com.artheus.cidadaoalerta.model.enums.StatusReclamacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReclamacaoRepository extends JpaRepository<Reclamacao, Long> {

    // ======================= MÉTODOS PÚBLICOS SIMPLES =======================

    /** Lista todas as reclamações ativas (ativo = true). */
    List<Reclamacao> findByAtivoTrue();

    /** Lista reclamações ativas com suporte a paginação. */
    Page<Reclamacao> findByAtivoTrue(Pageable pageable);

    /** Verifica se uma reclamação pertence a um usuário específico (por e-mail). */
    boolean existsByIdAndUsuario_Email(Long reclamacaoId, String email);

    /** Busca uma reclamação (ativa ou inativa) pelo título e ID do usuário. */
    Optional<Reclamacao> findByTituloAndUsuarioId(String titulo, Long usuarioId);

    /** Busca uma reclamação ativa pelo título e ID do usuário. */
    Optional<Reclamacao> findByTituloAndUsuarioIdAndAtivoTrue(String titulo, Long usuarioId);

    // ======================= CONSULTAS POR FILTROS ESPECÍFICOS =======================

    List<Reclamacao> findByStatus(StatusReclamacao status);

    List<Reclamacao> findByStatusAndUsuarioId(StatusReclamacao status, Long usuarioId);

    List<Reclamacao> findByStatusAndUsuarioIdAndCategoriaReclamacao(
            StatusReclamacao status,
            Long usuarioId,
            CategoriaReclamacao categoria
    );

    List<Reclamacao> findByDataCriacaoBetween(LocalDate startDate, LocalDate endDate);

    List<Reclamacao> findByStatusAndUsuarioIdAndDataCriacaoBetween(
            StatusReclamacao status,
            Long usuarioId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Reclamacao> findByCategoriaReclamacao(CategoriaReclamacao categoria);

    List<Reclamacao> findByLocalizacao_LatitudeBetweenAndLocalizacao_LongitudeBetween(
            double latMin, double latMax, double lonMin, double lonMax
    );

    List<Reclamacao> findByTituloContainingOrDescricaoContaining(String titulo, String descricao);

    List<Reclamacao> findByUsuarioId(Long usuarioId);

    List<Reclamacao> findByStatusIn(List<StatusReclamacao> statuses);

    // ======================= CONSULTAS PERSONALIZADAS COM @Query =======================

    /**
     * Consulta completa com filtros de status, usuário e intervalo de datas.
     * Útil para relatórios CSV ou dashboards.
     */
    @Query("SELECT r FROM Reclamacao r WHERE " +
            "(:status IS NULL OR r.status = :status) AND " +
            "(:usuarioId IS NULL OR r.usuario.id = :usuarioId) AND " +
            "(:startDate IS NULL OR :endDate IS NULL OR r.dataCriacao BETWEEN :startDate AND :endDate)")
    List<Reclamacao> buscarReclamacoesFiltradas(
            @Param("status") StatusReclamacao status,
            @Param("usuarioId") Long usuarioId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Consulta mais completa ainda, com todos os filtros principais.
     * Inclui categoria, status, usuário e intervalo de datas.
     * Ideal para exportação CSV.
     */
    @Query("SELECT r FROM Reclamacao r WHERE " +
            "(:status IS NULL OR r.status = :status) AND " +
            "(:usuarioId IS NULL OR r.usuario.id = :usuarioId) AND " +
            "(:categoria IS NULL OR r.categoriaReclamacao = :categoria) AND " +
            "(:startDate IS NULL OR r.dataCriacao >= :startDate) AND " +
            "(:endDate IS NULL OR r.dataCriacao <= :endDate)")
    List<Reclamacao> buscarReclamacoesPorFiltrosCompletos(
            @Param("status") StatusReclamacao status,
            @Param("usuarioId") Long usuarioId,
            @Param("categoria") CategoriaReclamacao categoria,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
