package com.artheus.cidadaoalerta.model.enums;

public enum TipoEventoReclamacao {
    CRIADA("foi criada"),
    ATUALIZADA("foi atualizada"),
    CONCLUIDA("foi concluída"),
    INATIVADA("foi inativada");

    private final String descricao;

    TipoEventoReclamacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
