package com.npl.na_ponta_do_lapis.web.dto.auth;

import jakarta.validation.constraints.NotBlank;

<<<<<<< HEAD
public record GoogleAuthRequestDTO(
        @NotBlank String idToken,
        Boolean termosAceitos
) {
=======
public record GoogleAuthRequestDTO(@NotBlank String idToken) {
>>>>>>> ad97d07 (feat(backend): permite login por email ou username e adiciona autenticacao com Google)
}

