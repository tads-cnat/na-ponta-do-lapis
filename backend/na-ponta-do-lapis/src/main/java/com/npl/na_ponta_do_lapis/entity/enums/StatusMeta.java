package com.npl.na_ponta_do_lapis.entity.enums;

public enum StatusMeta {
    EM_ANDAMENTO("Em Andamento"),
    CONCLUIDA("Concluída"),
    ESTOURADA("Estourada"),
<<<<<<< HEAD
<<<<<<< HEAD
    EXPIRADA("Expirada"),
=======
>>>>>>> 5fa35d6 (feat(metas): consolida 3 tipos essenciais, limpa tipo legado e implementa notificacoes e aportes)
=======
    EXPIRADA("Expirada"),
>>>>>>> f1d5028 (feat(metas): implementa tratamento de metas expiradas, fechamento de prazo e ritmo sugerido)
    CANCELADA("Cancelada");

    private final String descricao;

    StatusMeta(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}

