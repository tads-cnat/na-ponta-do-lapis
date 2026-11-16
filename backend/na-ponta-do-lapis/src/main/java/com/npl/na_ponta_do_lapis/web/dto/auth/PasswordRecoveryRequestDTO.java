package com.npl.na_ponta_do_lapis.web.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordRecoveryRequestDTO(
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Informe um e-mail válido.")
        String email,
        @NotBlank(message = "A resposta de segurança é obrigatória.")
        @Size(max = 200, message = "A resposta excede o limite permitido.")
        String answer
) {
}
