package com.npl.na_ponta_do_lapis.web.dto.auth;

public record PasswordRecoveryQuestionResponseDTO(
        String message,
        String question
) {
}