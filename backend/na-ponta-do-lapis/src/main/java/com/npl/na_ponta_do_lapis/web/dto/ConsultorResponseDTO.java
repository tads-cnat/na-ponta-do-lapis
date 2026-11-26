package com.npl.na_ponta_do_lapis.web.dto;

public record ConsultorResponseDTO(
        String resposta,
        String provedorUsado,
        boolean usouChavePlataforma
) {
}
