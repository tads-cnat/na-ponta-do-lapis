package com.npl.na_ponta_do_lapis.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ConsultorRequestDTO(
        @NotBlank(message = "Digite uma pergunta para o consultor.")
        @Size(max = 1000, message = "A pergunta deve ter no máximo 1000 caracteres.")
        String pergunta,

        List<MensagemHistorico> historico,

        String provedor,
        String apiKey,
        Long chaveIaId,
        boolean manterNaSessao
) {
    public record MensagemHistorico(
            String role,
            String content
    ) {
    }
}
