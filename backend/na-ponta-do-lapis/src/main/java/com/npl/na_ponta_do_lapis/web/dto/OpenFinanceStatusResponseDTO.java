package com.npl.na_ponta_do_lapis.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public record OpenFinanceStatusResponseDTO(
        boolean conectado,
        LocalDateTime ultimaSincronizacao,
        int sincronizacoesRestantesHoje,
        int limiteDiario,
        String clientIdMascarado,
        int totalContasSincronizadas
) {
    public OpenFinanceStatusResponseDTO(boolean conectado, LocalDateTime ultimaSincronizacao, int sincronizacoesRestantesHoje, int limiteDiario) {
        this(conectado, ultimaSincronizacao, sincronizacoesRestantesHoje, limiteDiario, null, 0);
    }

    public OpenFinanceStatusResponseDTO(boolean conectado, LocalDateTime ultimaSincronizacao, int sincronizacoesRestantesHoje, int limiteDiario, String clientIdMascarado) {
        this(conectado, ultimaSincronizacao, sincronizacoesRestantesHoje, limiteDiario, clientIdMascarado, 0);
    }

    @JsonProperty("syncsRestantesHoje")
    public int getSyncsRestantesHoje() {
        return sincronizacoesRestantesHoje;
    }
}
