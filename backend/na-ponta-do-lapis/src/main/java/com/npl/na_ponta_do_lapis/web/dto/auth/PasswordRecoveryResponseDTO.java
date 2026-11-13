package com.npl.na_ponta_do_lapis.web.dto.auth;

public record PasswordRecoveryResponseDTO(
        String message,
        String temporaryPassword
) {
}
