package com.npl.na_ponta_do_lapis.entity.enums;

public enum TipoMeta {
    POUPANCA("Poupança"),
    GASTO("Gasto"),
    DIVIDA("Dívida"),
    PRAZO_FIXO("Prazo Fixo");

    private final String nome;

    TipoMeta(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}
