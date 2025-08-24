package com.artheus.cidadaoalerta.repository;

import com.artheus.cidadaoalerta.model.Reclamacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReclamacaoRepository extends JpaRepository<Reclamacao, Long> {

    List<Reclamacao> findByAtivoTrue();

    Page<Reclamacao> findByAtivoTrue(Pageable pageable); // ← método paginado

    boolean existsByIdAndUsuario_Email(Long reclamacaoId, String email);

    Optional<Reclamacao> findByTituloAndUsuarioId(String titulo, Long usuarioId);

    Optional<Reclamacao> findByTituloAndUsuarioIdAndAtivoTrue(String titulo, Long usuarioId);

}
