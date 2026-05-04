package com.npl.na_ponta_do_lapis.service.auth;

import com.npl.na_ponta_do_lapis.web.dto.UsuarioDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.LoginRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.RegisterRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.TokenResponseDTO;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    void criarContaAdmin();

    boolean hasUserWithEmail(String email);

    public TokenResponseDTO login(LoginRequestDTO loginRequestDTO, HttpServletResponse response);
    void signup(RegisterRequestDTO registerRequestDTO, HttpServletResponse response);

    public void logout();
}
