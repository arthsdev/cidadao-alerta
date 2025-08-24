package com.artheus.cidadaoalerta.dto;

import java.util.List;

public record ReclamacaoPageResponse<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean last
) {}
