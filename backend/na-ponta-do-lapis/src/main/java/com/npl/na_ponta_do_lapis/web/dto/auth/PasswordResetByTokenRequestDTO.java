package com.npl.na_ponta_do_lapis.web.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetByTokenRequestDTO(
        @NotBlank(message = "O token de recuperação é obrigatório.")
        String token,
        @NotBlank(message = "A nova senha é obrigatória.")
        @Size(min = 6, max = 72, message = "A nova senha deve ter entre 6 e 72 caracteres.")
        String newPassword
) {
}

