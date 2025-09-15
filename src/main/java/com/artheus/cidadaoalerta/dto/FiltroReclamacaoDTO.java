package com.artheus.cidadaoalerta.dto;

import com.artheus.cidadaoalerta.model.enums.CategoriaReclamacao;
import com.artheus.cidadaoalerta.model.enums.StatusReclamacao;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * DTO usado para receber filtros de reclamações via endpoint.
 * Fornece métodos para conversão segura de datas para LocalDateTime.
 */
public record FiltroReclamacaoDTO(
        StatusReclamacao status,                  // Status da reclamação (opcional)
        Long usuarioId,                            // ID do usuário (opcional)
        CategoriaReclamacao categoria,            // Categoria da reclamação (opcional)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio, // Data inicial do filtro
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim     // Data final do filtro
) {

    /**
     * Converte a data inicial para LocalDateTime no início do dia (00:00:00).
     * Retorna Optional.empty() se não houver valor definido.
     */
    public Optional<LocalDateTime> getDataInicioLdt() {
        return Optional.ofNullable(dataInicio).map(LocalDate::atStartOfDay);
    }

    /**
     * Converte a data final para LocalDateTime no final do dia (23:59:59).
     * Retorna Optional.empty() se não houver valor definido.
     */
    public Optional<LocalDateTime> getDataFimLdt() {
        return Optional.ofNullable(dataFim).map(d -> d.atTime(23, 59, 59));
    }
}
