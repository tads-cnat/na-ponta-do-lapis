package com.npl.na_ponta_do_lapis.web.controller;

import com.npl.na_ponta_do_lapis.service.auth.AuthRateLimitService;
import com.npl.na_ponta_do_lapis.service.auth.AuthService;
import com.npl.na_ponta_do_lapis.service.email.EmailSenderService;
import com.npl.na_ponta_do_lapis.web.dto.auth.GoogleAuthRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.GoogleAuthResponseDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.LoginRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.PasswordRecoveryEmailRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.PasswordResetByTokenRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.RegisterRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.TokenResponseDTO;
import com.npl.na_ponta_do_lapis.web.exception.EmailNaoVerificadoException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private AuthService authService;
    private EmailSenderService emailSenderService;
    private AuthRateLimitService authRateLimitService;

    @Value("${google.client-id:}")
    private String googleClientId;

    public AuthController(AuthService authService) {
        this(authService, null, new AuthRateLimitService());
    }

    public AuthController(AuthService authService, EmailSenderService emailSenderService) {
        this(authService, emailSenderService, new AuthRateLimitService());
    }

    @org.springframework.beans.factory.annotation.Autowired
    public AuthController(AuthService authService, EmailSenderService emailSenderService, AuthRateLimitService authRateLimitService) {
        this.authService = authService;
        this.emailSenderService = emailSenderService;
        this.authRateLimitService = authRateLimitService != null ? authRateLimitService : new AuthRateLimitService();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequestDTO, HttpServletResponse response) {
        try {
           TokenResponseDTO token = authService.login(loginRequestDTO, response);
           return ResponseEntity.ok(token);
        } catch (EmailNaoVerificadoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "message", e.getMessage(),
                    "emailNaoVerificado", true,
                    "email", e.getEmail() != null ? e.getEmail() : ""
            ));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("message", "Email ou senha inválido."));
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
    public ResponseEntity<GoogleAuthResponseDTO> googleAuth(@RequestBody @Valid GoogleAuthRequestDTO request) {
        return ResponseEntity.ok(
                authService.loginWithGoogle(request.idToken(), Boolean.TRUE.equals(request.termosAceitos()))
        );
    }
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody @Valid RegisterRequestDTO registerRequestDTO,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        if (!authRateLimitService.allowIpRequest("register", resolverClientIp(httpRequest), 5)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Collections.singletonMap("message", "Muitas tentativas de cadastro. Aguarde alguns minutos e tente novamente."));
        }
        authService.signup(registerRequestDTO, response);
        boolean requiresEmailVerification = emailSenderService != null && emailSenderService.isEnabled();
        String message = requiresEmailVerification
                ? "Conta criada! Enviamos um link de confirmação para o seu e-mail."
                : "Usuário cadastrado com sucesso.";
        return ResponseEntity.ok(Map.of(
                "message", message,
                "requiresEmailVerification", requiresEmailVerification
        ));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestBody Map<String, String> request) {
        String token = request != null ? request.get("token") : null;
        boolean verified = authService.verifyEmailToken(token);
        if (!verified) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("message", "Link de confirmação inválido ou expirado. Solicite um novo envio."));
        }
        return ResponseEntity.ok(Collections.singletonMap("message", "E-mail confirmado com sucesso! Agora você já pode entrar."));
    }

    @PostMapping("/verify-email/resend")
    public ResponseEntity<Map<String, String>> resendVerificationEmail(
            @RequestBody @Valid PasswordRecoveryEmailRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        if (!authRateLimitService.allowIpRequest("verify-email", resolverClientIp(httpRequest), 5)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Collections.singletonMap("message", "Muitas solicitações. Aguarde alguns minutos e tente novamente."));
        }
        authService.resendVerificationEmail(request.email());
        return ResponseEntity.ok(Collections.singletonMap(
                "message",
                "Se a conta estiver pendente de confirmação, enviaremos um novo link para o seu e-mail."
        ));
    }

    @GetMapping("/recover-password/config")
    public ResponseEntity<Map<String, Object>> recoveryConfig() {
        boolean emailEnabled = emailSenderService != null && emailSenderService.isEnabled();
        return ResponseEntity.ok(Map.of("emailEnabled", emailEnabled));
    }

    @PostMapping("/recover-password/email")
    public ResponseEntity<Map<String, String>> requestRecoveryEmail(
            @RequestBody @Valid PasswordRecoveryEmailRequestDTO request,
            HttpServletRequest httpRequest
    ) {
        if (!authRateLimitService.allowIpRequest("recover-password", resolverClientIp(httpRequest), 8)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Collections.singletonMap("message", "Muitas solicitações. Aguarde alguns minutos e tente novamente."));
        }
        authService.requestPasswordResetEmail(request.email());
        return ResponseEntity.ok(Collections.singletonMap(
                "message",
                "Se o e-mail estiver cadastrado e confirmado, enviaremos um link de recuperação válido por 15 minutos."
        ));
    }

    private String resolverClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].strip();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.strip();
        }
        return request.getRemoteAddr();
    }

    @PostMapping("/recover-password/reset")
    public ResponseEntity<Map<String, String>> resetPasswordByToken(
            @RequestBody @Valid PasswordResetByTokenRequestDTO request
    ) {
        boolean resetOk = authService.resetPasswordWithToken(request.token(), request.newPassword());
        if (!resetOk) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("message", "Link de recuperação inválido ou expirado. Solicite um novo."));
        }
        return ResponseEntity.ok(Collections.singletonMap("message", "Senha alterada com sucesso. Entre com sua nova senha."));
    }
}
