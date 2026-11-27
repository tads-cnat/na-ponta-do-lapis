package com.npl.na_ponta_do_lapis.web.dto;

import jakarta.validation.constraints.NotNull;

public record ConsultorFeedbackRequestDTO(
        Long feedbackId,
        @NotNull(message = "Informe se a resposta foi útil.")
        Boolean util,
        String provedor,
        String perguntaResumo,
        String respostaResumo
) {
}
