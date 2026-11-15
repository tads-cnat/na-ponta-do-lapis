package com.npl.na_ponta_do_lapis.web.dto.auth;

import jakarta.validation.constraints.NotBlank;

<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 8aa2880 (feat(auth): exigir aceite de termos LGPD no primeiro cadastro via Google)
public record GoogleAuthRequestDTO(
        @NotBlank String idToken,
        Boolean termosAceitos
) {
<<<<<<< HEAD
=======
public record GoogleAuthRequestDTO(@NotBlank String idToken) {
>>>>>>> ad97d07 (feat(backend): permite login por email ou username e adiciona autenticacao com Google)
=======
>>>>>>> 8aa2880 (feat(auth): exigir aceite de termos LGPD no primeiro cadastro via Google)
}

