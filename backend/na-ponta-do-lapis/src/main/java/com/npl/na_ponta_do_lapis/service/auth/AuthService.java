package com.npl.na_ponta_do_lapis.service.auth;

import com.npl.na_ponta_do_lapis.web.dto.UsuarioDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.LoginRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.PasswordRecoveryResponseDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.RegisterRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.TokenResponseDTO;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {


    boolean hasUserWithEmail(String email);

    public TokenResponseDTO login(LoginRequestDTO loginRequestDTO, HttpServletResponse response);
    TokenResponseDTO loginWithGoogle(String idToken);
    void signup(RegisterRequestDTO registerRequestDTO, HttpServletResponse response);
    PasswordRecoveryResponseDTO recoverPassword(String email);

    public void logout();
}
