package com.npl.na_ponta_do_lapis.service.auth;

import com.npl.na_ponta_do_lapis.web.dto.UsuarioDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.GoogleAuthResponseDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.LoginRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.PasswordRecoveryResponseDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.PasswordRecoveryQuestionResponseDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.RegisterRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.TokenResponseDTO;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {


    boolean hasUserWithEmail(String email);

    public TokenResponseDTO login(LoginRequestDTO loginRequestDTO, HttpServletResponse response);
    GoogleAuthResponseDTO loginWithGoogle(String idToken, boolean termosAceitos);
    void signup(RegisterRequestDTO registerRequestDTO, HttpServletResponse response);
    PasswordRecoveryQuestionResponseDTO getRecoveryQuestion(String email);
    Long verifyRecoveryAnswer(String email, String answer);
    void changePasswordAfterRecovery(Long userId, String newPassword);

    public void logout();
}
