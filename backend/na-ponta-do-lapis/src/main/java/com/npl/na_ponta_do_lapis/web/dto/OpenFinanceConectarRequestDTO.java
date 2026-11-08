package com.npl.na_ponta_do_lapis.web.dto;

import jakarta.validation.constraints.NotBlank;

public record OpenFinanceConectarRequestDTO(
        @NotBlank(message = "O Client ID é obrigatório")
        String clientId,

        @NotBlank(message = "O Client Secret é obrigatório")
        String clientSecret,

        String itemIds
) {
    public OpenFinanceConectarRequestDTO(String clientId, String clientSecret) {
        this(clientId, clientSecret, null);
    }
}

