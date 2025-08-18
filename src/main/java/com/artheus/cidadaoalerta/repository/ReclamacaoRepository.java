package com.artheus.cidadaoalerta.repository;

import com.artheus.cidadaoalerta.model.Reclamacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReclamacaoRepository extends JpaRepository<Reclamacao, Long> {
    List<Reclamacao> findByAtivoTrue();

}
