package com.npl.na_ponta_do_lapis.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record OpenFinanceStatusResponseDTO(
        boolean conectado,
        LocalDateTime ultimaSincronizacao,
        int sincronizacoesRestantesHoje,
        int limiteDiario,
        String clientIdMascarado,
<<<<<<< HEAD
<<<<<<< HEAD
        int totalContasSincronizadas,
        int totalItensConectados,
        int limiteItensConexao
) {
    public OpenFinanceStatusResponseDTO(boolean conectado, LocalDateTime ultimaSincronizacao, int sincronizacoesRestantesHoje, int limiteDiario) {
        this(conectado, ultimaSincronizacao, sincronizacoesRestantesHoje, limiteDiario, null, 0, 0, 5);
    }

    public OpenFinanceStatusResponseDTO(boolean conectado, LocalDateTime ultimaSincronizacao, int sincronizacoesRestantesHoje, int limiteDiario, String clientIdMascarado) {
        this(conectado, ultimaSincronizacao, sincronizacoesRestantesHoje, limiteDiario, clientIdMascarado, 0, 0, 5);
    }

    public OpenFinanceStatusResponseDTO(boolean conectado, LocalDateTime ultimaSincronizacao, int sincronizacoesRestantesHoje, int limiteDiario, String clientIdMascarado, int totalContasSincronizadas) {
        this(conectado, ultimaSincronizacao, sincronizacoesRestantesHoje, limiteDiario, clientIdMascarado, totalContasSincronizadas, 0, 5);
=======
        int totalContasSincronizadas
=======
        int totalContasSincronizadas,
        int totalItensConectados,
        int limiteItensConexao
>>>>>>> 2b2d120 (fix(open-finance): remove trava artificial de 5 syncs diarios e reflete cota de 5 bancos da pluggy)
) {
    public OpenFinanceStatusResponseDTO(boolean conectado, LocalDateTime ultimaSincronizacao, int sincronizacoesRestantesHoje, int limiteDiario) {
        this(conectado, ultimaSincronizacao, sincronizacoesRestantesHoje, limiteDiario, null, 0, 0, 5);
    }

    public OpenFinanceStatusResponseDTO(boolean conectado, LocalDateTime ultimaSincronizacao, int sincronizacoesRestantesHoje, int limiteDiario, String clientIdMascarado) {
<<<<<<< HEAD
        this(conectado, ultimaSincronizacao, sincronizacoesRestantesHoje, limiteDiario, clientIdMascarado, 0);
>>>>>>> 9653540 (feat(open-finance): integracao pluggy, gestao de chaves IA, discriminacao de faturas e filtro de parcelamento)
=======
        this(conectado, ultimaSincronizacao, sincronizacoesRestantesHoje, limiteDiario, clientIdMascarado, 0, 0, 5);
    }

    public OpenFinanceStatusResponseDTO(boolean conectado, LocalDateTime ultimaSincronizacao, int sincronizacoesRestantesHoje, int limiteDiario, String clientIdMascarado, int totalContasSincronizadas) {
        this(conectado, ultimaSincronizacao, sincronizacoesRestantesHoje, limiteDiario, clientIdMascarado, totalContasSincronizadas, 0, 5);
>>>>>>> 2b2d120 (fix(open-finance): remove trava artificial de 5 syncs diarios e reflete cota de 5 bancos da pluggy)
    }

    @JsonProperty("syncsRestantesHoje")
    public int getSyncsRestantesHoje() {
        return sincronizacoesRestantesHoje;
    }
}
