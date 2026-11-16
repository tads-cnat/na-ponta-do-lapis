package com.npl.na_ponta_do_lapis.web.dto.auth;

public record GoogleAuthResponseDTO(
        String token,
        boolean requerTermos,
        String nome,
        String email,
        String fotoUrl
) {
    public static GoogleAuthResponseDTO comToken(String token) {
        return new GoogleAuthResponseDTO(token, false, null, null, null);
    }

    public static GoogleAuthResponseDTO pendenteTermos(String nome, String email, String fotoUrl) {
        return new GoogleAuthResponseDTO(null, true, nome, email, fotoUrl);
    }
}

