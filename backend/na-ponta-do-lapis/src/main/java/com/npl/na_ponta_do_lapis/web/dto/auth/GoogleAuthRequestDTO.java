package com.npl.na_ponta_do_lapis.web.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record GoogleAuthRequestDTO(@NotBlank String idToken) {
}

