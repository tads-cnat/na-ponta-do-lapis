package com.npl.na_ponta_do_lapis.web.dto;

import com.npl.na_ponta_do_lapis.service.fatura.ia.ProvedorIa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChaveIaRequestDTO(
        @NotBlank(message = "O apelido da chave é obrigatório")
        @Size(max = 100, message = "O apelido pode ter no máximo 100 caracteres")
        String apelido,

        @NotNull(message = "O provedor de IA é obrigatório")
        ProvedorIa provedor,

        @NotBlank(message = "A chave de API é obrigatória")
        String apiKey,

        boolean padrao
) {
}

