package com.artheus.cidadaoalerta.infra.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class TraceIdFilter extends HttpFilter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String MDC_TRACE_ID_KEY = "traceId";

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            // Tenta pegar traceId do header (se vier em chamada externa)
            String traceId = request.getHeader(TRACE_ID_HEADER);
            if (traceId == null || traceId.isEmpty()) {
                traceId = UUID.randomUUID().toString(); // gera um novo
            }

            // Coloca o traceId no MDC para estar disponível nos logs
            MDC.put(MDC_TRACE_ID_KEY, traceId);

            // Também adiciona no response header para o cliente receber
            response.setHeader(TRACE_ID_HEADER, traceId);

            chain.doFilter(request, response);
        } finally {
            // Limpa o MDC para evitar vazamento de dados entre requisições
            MDC.remove(MDC_TRACE_ID_KEY);
        }
    }
}
