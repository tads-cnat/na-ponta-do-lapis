package com.npl.na_ponta_do_lapis.entity.enums;

public enum TipoMeta {
    POUPANCA("Juntar Dinheiro"),
    GASTO("Teto de Gastos"),
    DIVIDA("Quitar Dívida");

    private final String nome;

    TipoMeta(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}
