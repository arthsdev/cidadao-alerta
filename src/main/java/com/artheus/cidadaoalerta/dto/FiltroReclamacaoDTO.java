package com.artheus.cidadaoalerta.dto;

import com.artheus.cidadaoalerta.model.enums.CategoriaReclamacao;
import com.artheus.cidadaoalerta.model.enums.StatusReclamacao;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * DTO usado para receber filtros de reclamações via endpoint.
 * Fornece métodos para conversão segura de datas para LocalDateTime.
 * Configurado com exemplos para Swagger/OpenAPI.
 */
public record FiltroReclamacaoDTO(
        @Schema(description = "Status da reclamação", example = "ABERTA")
        StatusReclamacao status,

        @Schema(description = "ID do usuário", example = "0")
        Long usuarioId,

        @Schema(description = "Categoria da reclamação", example = "ILUMINACAO")
        CategoriaReclamacao categoria,

        @Schema(description = "Data inicial do filtro", example = "2025-09-17", type = "string", format = "date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dataInicio,

        @Schema(description = "Data final do filtro", example = "2025-09-17", type = "string", format = "date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate dataFim
) {

    /**
     * Converte a data inicial para LocalDateTime no início do dia (00:00:00).
     * Não será exposta no JSON.
     */
    @JsonIgnore
    public Optional<LocalDateTime> getDataInicioLdt() {
        return Optional.ofNullable(dataInicio).map(LocalDate::atStartOfDay);
    }

    /**
     * Converte a data final para LocalDateTime no final do dia (23:59:59).
     * Não será exposta no JSON.
     */
    @JsonIgnore
    public Optional<LocalDateTime> getDataFimLdt() {
        return Optional.ofNullable(dataFim).map(d -> d.atTime(23, 59, 59));
    }
}
