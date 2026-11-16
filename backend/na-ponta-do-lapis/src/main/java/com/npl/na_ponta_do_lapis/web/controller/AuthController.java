package com.npl.na_ponta_do_lapis.web.controller;

import com.npl.na_ponta_do_lapis.service.auth.AuthService;
import com.npl.na_ponta_do_lapis.web.dto.auth.GoogleAuthRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.LoginRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.PasswordRecoveryEmailRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.PasswordRecoveryChangeRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.PasswordRecoveryQuestionResponseDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.PasswordRecoveryRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.PasswordRecoveryResponseDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.RegisterRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.TokenResponseDTO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String RECOVERY_USER_ID = "passwordRecoveryUserId";

    private AuthService authService;

    @Value("${google.client-id:}")
    private String googleClientId;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequestDTO, HttpServletResponse response) {
        try {
           TokenResponseDTO token = authService.login(loginRequestDTO, response);
           return ResponseEntity.ok(token);
        } catch (AuthenticationCredentialsNotFoundException e) {
            return ResponseEntity.ok(Collections.singletonMap("message", "Email ou senha inválido."));
        }
    }

    @GetMapping("/google/config")
    public ResponseEntity<Map<String, Object>> googleConfig() {
        boolean enabled = googleClientId != null && !googleClientId.isBlank();
        return ResponseEntity.ok(Map.of(
                "enabled", enabled,
                "clientId", enabled ? googleClientId : ""
        ));
    }

    @PostMapping("/google")
    public ResponseEntity<TokenResponseDTO> googleAuth(@RequestBody @Valid GoogleAuthRequestDTO request) {
        return ResponseEntity.ok(authService.loginWithGoogle(request.idToken()));
    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequestDTO registerRequestDTO, HttpServletResponse response) {
        authService.signup(registerRequestDTO, response);
        return ResponseEntity.ok(Collections.singletonMap("message", "Usuário cadastrado com sucesso."));
    }

    @PostMapping("/recover-password/question")
    public ResponseEntity<PasswordRecoveryQuestionResponseDTO> recoveryQuestion(
            @RequestBody @Valid PasswordRecoveryEmailRequestDTO request
    ) {
        return ResponseEntity.ok(authService.getRecoveryQuestion(request.email()));
    }

    @PostMapping("/recover-password")
    public ResponseEntity<PasswordRecoveryResponseDTO> recoverPassword(
            @RequestBody @Valid PasswordRecoveryRequestDTO request,
            HttpServletRequest servletRequest
    ) {
        Long userId = authService.verifyRecoveryAnswer(request.email(), request.answer());
        if (userId == null) {
            return ResponseEntity.ok(new PasswordRecoveryResponseDTO(false, "Não foi possível confirmar os dados informados."));
        }

        HttpSession session = servletRequest.getSession(true);
        servletRequest.changeSessionId();
        session.setMaxInactiveInterval(5 * 60);
        session.setAttribute(RECOVERY_USER_ID, userId);
        return ResponseEntity.ok(new PasswordRecoveryResponseDTO(true, "Resposta confirmada. Defina uma nova senha no perfil."));
    }

    @PostMapping("/recover-password/change")
    public ResponseEntity<?> changeRecoveredPassword(
            @RequestBody @Valid PasswordRecoveryChangeRequestDTO request,
            HttpServletRequest servletRequest
    ) {
        HttpSession session = servletRequest.getSession(false);
        Object userId = session == null ? null : session.getAttribute(RECOVERY_USER_ID);
        if (!(userId instanceof Long id)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("message", "A sessão de recuperação expirou. Inicie novamente."));
        }

        authService.changePasswordAfterRecovery(id, request.newPassword());
        session.invalidate();
        return ResponseEntity.ok(Collections.singletonMap("message", "Senha alterada. Entre com sua nova senha."));
    }
}
