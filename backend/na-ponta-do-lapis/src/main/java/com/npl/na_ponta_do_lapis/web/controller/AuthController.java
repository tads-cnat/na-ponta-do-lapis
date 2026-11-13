package com.npl.na_ponta_do_lapis.web.controller;

import com.npl.na_ponta_do_lapis.service.auth.AuthService;
import com.npl.na_ponta_do_lapis.web.dto.auth.GoogleAuthRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.LoginRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.RegisterRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.TokenResponseDTO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

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
}
