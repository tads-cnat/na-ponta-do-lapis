package com.npl.na_ponta_do_lapis.service.auth;

import com.npl.na_ponta_do_lapis.web.dto.auth.GoogleAuthResponseDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.LoginRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.RegisterRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.TokenResponseDTO;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {


    boolean hasUserWithEmail(String email);

    public TokenResponseDTO login(LoginRequestDTO loginRequestDTO, HttpServletResponse response);
    GoogleAuthResponseDTO loginWithGoogle(String idToken, boolean termosAceitos);
    void signup(RegisterRequestDTO registerRequestDTO, HttpServletResponse response);
    void requestPasswordResetEmail(String email);
    boolean resetPasswordWithToken(String token, String newPassword);
    boolean verifyEmailToken(String token);
    void resendVerificationEmail(String email);

    public void logout();
}
