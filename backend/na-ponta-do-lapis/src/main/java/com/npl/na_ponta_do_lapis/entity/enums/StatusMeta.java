package com.npl.na_ponta_do_lapis.entity.enums;

public enum StatusMeta {
    EM_ANDAMENTO("Em Andamento"),
    CONCLUIDA("Concluída"),
    ESTOURADA("Estourada"),
    EXPIRADA("Expirada"),
    CANCELADA("Cancelada");

    private final String descricao;

    StatusMeta(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}

