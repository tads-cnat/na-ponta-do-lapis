package com.npl.na_ponta_do_lapis.web.dto;

import jakarta.validation.constraints.NotBlank;

public record OpenFinanceAdicionarItemRequestDTO(
        @NotBlank(message = "O Item ID é obrigatório")
        String itemId
) {
}

