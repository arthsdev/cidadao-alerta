package com.artheus.cidadaoalerta.unit.infra.filter;

import com.artheus.cidadaoalerta.infra.filter.TraceIdFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TraceIdFilterTest {

    private final TraceIdFilter filter = new TraceIdFilter();

    @Test
    void deveUsarHeaderExistenteSePresente() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        request.addHeader(TraceIdFilter.TRACE_ID_HEADER, "12345");

        filter.doFilter(request, response, chain);

        // verifica que o header foi usado
        assertEquals("12345", response.getHeader(TraceIdFilter.TRACE_ID_HEADER));
        // MDC deve ser limpo após o filtro
        assertNull(MDC.get(TraceIdFilter.MDC_TRACE_ID_KEY));
        // chain.doFilter deve ter sido chamado
        verify(chain).doFilter(request, response);
    }

    @Test
    void deveGerarNovoTraceIdSeHeaderAusente() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        String traceId = response.getHeader(TraceIdFilter.TRACE_ID_HEADER);
        assertNotNull(traceId);
        assertFalse(traceId.isEmpty());
        assertNull(MDC.get(TraceIdFilter.MDC_TRACE_ID_KEY));
        verify(chain).doFilter(request, response);
    }

    @Test
    void deveGerarNovoTraceIdSeHeaderVazio() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        request.addHeader(TraceIdFilter.TRACE_ID_HEADER, "");

        filter.doFilter(request, response, chain);

        String traceId = response.getHeader(TraceIdFilter.TRACE_ID_HEADER);
        assertNotNull(traceId);
        assertFalse(traceId.isEmpty());
        assertNull(MDC.get(TraceIdFilter.MDC_TRACE_ID_KEY));
        verify(chain).doFilter(request, response);
    }
}
