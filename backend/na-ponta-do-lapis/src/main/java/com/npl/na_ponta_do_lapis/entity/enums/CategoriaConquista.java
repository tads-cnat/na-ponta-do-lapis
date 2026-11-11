package com.npl.na_ponta_do_lapis.entity.enums;

public enum CategoriaConquista {
    METAS("Metas"),
    ECONOMIA("Economia"),
    DISCIPLINA("Disciplina"),
    FAMILIA("Família");

    private final String nome;

    CategoriaConquista(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}

