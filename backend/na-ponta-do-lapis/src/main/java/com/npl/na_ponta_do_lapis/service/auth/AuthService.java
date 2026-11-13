package com.npl.na_ponta_do_lapis.service.auth;

import com.npl.na_ponta_do_lapis.web.dto.UsuarioDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.GoogleAuthResponseDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.LoginRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.RegisterRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.TokenResponseDTO;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {


    boolean hasUserWithEmail(String email);

    public TokenResponseDTO login(LoginRequestDTO loginRequestDTO, HttpServletResponse response);
<<<<<<< HEAD
    GoogleAuthResponseDTO loginWithGoogle(String idToken, boolean termosAceitos);
=======
    TokenResponseDTO loginWithGoogle(String idToken);
>>>>>>> ad97d07 (feat(backend): permite login por email ou username e adiciona autenticacao com Google)
    void signup(RegisterRequestDTO registerRequestDTO, HttpServletResponse response);

    public void logout();
}
