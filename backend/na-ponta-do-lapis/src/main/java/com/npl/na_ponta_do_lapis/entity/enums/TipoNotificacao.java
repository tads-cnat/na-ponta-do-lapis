package com.npl.na_ponta_do_lapis.entity.enums;

public enum TipoNotificacao {
    META_ALERTA_50("Alerta de Meta (50%)"),
    META_ALERTA_80("Alerta de Meta (80%)"),
    META_ATINGIDA("Meta Atingida!"),
    META_ESTOURADA("Teto de Meta Ultrapassado"),
    CONQUISTA_DESBLOQUEADA("Nova Conquista Desbloqueada!"),
    FAMILIA_CONVITE("Convite Familiar"),
    SISTEMA("Notificação do Sistema");

    private final String descricao;

    TipoNotificacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}

