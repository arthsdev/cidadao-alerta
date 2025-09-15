package com.artheus.cidadaoalerta.exception.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {
    private String type;         // URI que identifica o tipo de erro
    private String title;        // Título genérico
    private int status;          // Código HTTP
    private String detail;       // Mensagem detalhada
    private String instance;     // Caminho da requisição (URI)
    private String traceId;      // para rastrear em logs
    private LocalDateTime timestamp;
}
