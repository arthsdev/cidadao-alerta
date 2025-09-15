package com.artheus.cidadaoalerta.unit.model;

import com.artheus.cidadaoalerta.exception.model.ApiError;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ApiErrorTest {

    @Test
    void deveCobrirBuilderEGetters() {
        LocalDateTime now = LocalDateTime.now();

        ApiError apiError = ApiError.builder()
                .type("tipo")
                .title("titulo")
                .status(500)
                .detail("detalhe")
                .instance("/endpoint")
                .traceId("trace-123")
                .timestamp(now)
                .build();

        // Verifica todos os getters
        assertAll(
                () -> assertEquals("tipo", apiError.getType()),
                () -> assertEquals("titulo", apiError.getTitle()),
                () -> assertEquals(500, apiError.getStatus()),
                () -> assertEquals("detalhe", apiError.getDetail()),
                () -> assertEquals("/endpoint", apiError.getInstance()),
                () -> assertEquals("trace-123", apiError.getTraceId()),
                () -> assertEquals(now, apiError.getTimestamp())
        );
    }

    @Test
    void builderToStringNaoLancaErro() {
        ApiError.ApiErrorBuilder builder = ApiError.builder()
                .type("tipo")
                .title("titulo")
                .status(400)
                .detail("detalhe");

        // Apenas garante que chamar toString() não quebra o builder
        String str = builder.toString();
        assertNotNull(str);
    }

    @Test
    void doisBuildersComMesmosCamposTemMesmosValores() {
        LocalDateTime now = LocalDateTime.now();

        ApiError apiError1 = ApiError.builder()
                .type("tipo")
                .title("titulo")
                .status(200)
                .detail("detalhe")
                .instance("/endpoint")
                .traceId("trace-456")
                .timestamp(now)
                .build();

        ApiError apiError2 = ApiError.builder()
                .type("tipo")
                .title("titulo")
                .status(200)
                .detail("detalhe")
                .instance("/endpoint")
                .traceId("trace-456")
                .timestamp(now)
                .build();

        // Compara todos os campos individualmente
        assertAll(
                () -> assertEquals(apiError1.getType(), apiError2.getType()),
                () -> assertEquals(apiError1.getTitle(), apiError2.getTitle()),
                () -> assertEquals(apiError1.getStatus(), apiError2.getStatus()),
                () -> assertEquals(apiError1.getDetail(), apiError2.getDetail()),
                () -> assertEquals(apiError1.getInstance(), apiError2.getInstance()),
                () -> assertEquals(apiError1.getTraceId(), apiError2.getTraceId()),
                () -> assertEquals(apiError1.getTimestamp(), apiError2.getTimestamp())
        );
    }

    @Test
    void builderSemCamposObrigatoriosNaoLancaErro() {
        // Apenas constrói sem setar nada
        ApiError apiError = ApiError.builder().build();
        assertNotNull(apiError);
    }
}
