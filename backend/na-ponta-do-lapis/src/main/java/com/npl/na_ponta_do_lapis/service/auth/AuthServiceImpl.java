package com.npl.na_ponta_do_lapis.service.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.Moeda;
import com.npl.na_ponta_do_lapis.entity.enums.Papel;
import com.npl.na_ponta_do_lapis.repository.ContaFinanceiraRepository;
import com.npl.na_ponta_do_lapis.repository.UsuarioRepository;
import com.npl.na_ponta_do_lapis.security.UsuarioAuthCacheService;
import com.npl.na_ponta_do_lapis.security.jwt.JwtUtil;
import com.npl.na_ponta_do_lapis.service.email.EmailSenderService;
import com.npl.na_ponta_do_lapis.web.dto.auth.GoogleAuthResponseDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.LoginRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.RegisterRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.TokenResponseDTO;
import com.npl.na_ponta_do_lapis.web.exception.EmailJaCadastradoException;
import com.npl.na_ponta_do_lapis.web.exception.EmailNaoVerificadoException;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService{

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;
    private final ContaFinanceiraRepository contaFinanceiraRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private static final Duration EMAIL_RESET_TOKEN_DURATION = Duration.ofMinutes(15);
    private static final Duration EMAIL_VERIFICATION_TOKEN_DURATION = Duration.ofHours(24);
    private static final Duration EMAIL_RESEND_COOLDOWN = Duration.ofSeconds(60);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final HttpClient httpClient;
    private final UsuarioAuthCacheService usuarioAuthCacheService;
    private final EmailSenderService emailSenderService;
    private final AuthRateLimitService authRateLimitService;

    @Autowired(required = false)
    private com.npl.na_ponta_do_lapis.service.admin.AdminBootstrapService adminBootstrapService;

    @Value("${google.client-id:}")
    private String googleClientId;

    @Value("${app.web-url:http://localhost:4200}")
    private String appWebUrl;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil,
                           UsuarioRepository usuarioRepository,
                           ContaFinanceiraRepository contaFinanceiraRepository,
                           PasswordEncoder passwordEncoder) {
        this(authenticationManager, jwtUtil, usuarioRepository, contaFinanceiraRepository, passwordEncoder, new ObjectMapper(), null, null);
    }

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil,
                           UsuarioRepository usuarioRepository,
                           ContaFinanceiraRepository contaFinanceiraRepository,
                           PasswordEncoder passwordEncoder,
                           ObjectMapper objectMapper) {
        this(authenticationManager, jwtUtil, usuarioRepository, contaFinanceiraRepository, passwordEncoder, objectMapper, null, null);
    }

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil,
                           UsuarioRepository usuarioRepository,
                           ContaFinanceiraRepository contaFinanceiraRepository,
                           PasswordEncoder passwordEncoder,
                           ObjectMapper objectMapper,
                           UsuarioAuthCacheService usuarioAuthCacheService) {
        this(authenticationManager, jwtUtil, usuarioRepository, contaFinanceiraRepository, passwordEncoder, objectMapper, usuarioAuthCacheService, null);
    }

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil,
                           UsuarioRepository usuarioRepository,
                           ContaFinanceiraRepository contaFinanceiraRepository,
                           PasswordEncoder passwordEncoder,
                           ObjectMapper objectMapper,
                           UsuarioAuthCacheService usuarioAuthCacheService,
                           EmailSenderService emailSenderService) {
        this(authenticationManager, jwtUtil, usuarioRepository, contaFinanceiraRepository, passwordEncoder, objectMapper, usuarioAuthCacheService, emailSenderService, new AuthRateLimitService());
    }

    @Autowired
    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil,
                           UsuarioRepository usuarioRepository,
                           ContaFinanceiraRepository contaFinanceiraRepository,
                           PasswordEncoder passwordEncoder,
                           ObjectMapper objectMapper,
                           UsuarioAuthCacheService usuarioAuthCacheService,
                           EmailSenderService emailSenderService,
                           AuthRateLimitService authRateLimitService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
        this.contaFinanceiraRepository = contaFinanceiraRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
        this.usuarioAuthCacheService = usuarioAuthCacheService;
        this.emailSenderService = emailSenderService;
        this.authRateLimitService = authRateLimitService != null ? authRateLimitService : new AuthRateLimitService();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();
    }





    @Override
    public boolean hasUserWithEmail(String email) {
        return false;
    }

    @Override
    public TokenResponseDTO login(LoginRequestDTO loginRequestDTO, HttpServletResponse response) {
        String identifier = loginRequestDTO.email() == null ? "" : loginRequestDTO.email().strip();

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(identifier, loginRequestDTO.password());
        authenticationManager.authenticate(authenticationToken);

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(identifier)
                .or(() -> usuarioRepository.findByUsernameIgnoreCase(identifier))
                .orElseThrow(
                        () -> new UsernameNotFoundException("Usuário: " + identifier + " não encontrado")
                );

        if (!usuario.isEmailVerificado()) {
            throw new EmailNaoVerificadoException(
                    "Confirme seu e-mail antes de entrar. Verifique sua caixa de entrada ou solicite um novo link.",
                    usuario.getEmail()
            );
        }

        if (adminBootstrapService != null) {
            adminBootstrapService.promoverSeElegivel(usuario);
        }
        usuario.setUltimoAcessoEm(LocalDateTime.now());
        usuarioRepository.save(usuario);

        String token = jwtUtil.generateToken(usuario, jwtUtil.EXPIRATION_TOKEN(Date.from(Instant.now())), usuario.getEmail());
        return new TokenResponseDTO(token);
    }

    @Override
    @Transactional
    public GoogleAuthResponseDTO loginWithGoogle(String idToken, boolean termosAceitos) {
        JsonNode tokenInfo = verifyGoogleIdToken(idToken);
        String email = tokenInfo.path("email").asText("").strip().toLowerCase();
        String nome = tokenInfo.path("name").asText("").strip();
        String picture = tokenInfo.path("picture").asText(null);

        if (email.isBlank()) {
            throw new AuthenticationCredentialsNotFoundException("Token do Google sem e-mail válido.");
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElse(null);
        if (usuario == null) {
            if (!termosAceitos) {
                String nomeExibicao = nome.isBlank() ? email.substring(0, email.indexOf('@')) : nome;
                return GoogleAuthResponseDTO.pendenteTermos(nomeExibicao, email, picture);
            }
            usuario = registrarUsuarioGoogle(email, nome, picture);
        } else {
            boolean precisaSalvar = false;
            if (!usuario.isEmailVerificado()) {
                usuario.setEmailVerificado(true);
                usuario.setEmailVerificacaoTokenHash(null);
                usuario.setEmailVerificacaoExpiraEm(null);
                precisaSalvar = true;
            }
            if ((usuario.getFotoPerfil() == null || usuario.getFotoPerfil().isBlank())
                    && picture != null && !picture.isBlank()) {
                usuario.setFotoPerfil(picture);
                precisaSalvar = true;
            }
            if (precisaSalvar) {
                usuarioRepository.save(usuario);
            }
        }

        if (!usuario.isAtivo()) {
            throw new org.springframework.security.authentication.DisabledException("Sua conta está suspensa. Entre em contato com o suporte.");
        }

        if (adminBootstrapService != null) {
            adminBootstrapService.promoverSeElegivel(usuario);
        }
        usuario.setUltimoAcessoEm(LocalDateTime.now());
        usuarioRepository.save(usuario);

        String token = jwtUtil.generateToken(usuario, jwtUtil.EXPIRATION_TOKEN(Date.from(Instant.now())), usuario.getEmail());
        return GoogleAuthResponseDTO.comToken(token);
    }

    @Override
    @Transactional
    public void signup(RegisterRequestDTO registerRequestDTO, HttpServletResponse response) {
        String email = registerRequestDTO.email() == null ? "" : registerRequestDTO.email().strip();
        String username = registerRequestDTO.username() == null ? "" : registerRequestDTO.username().strip();

        if (usuarioRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new EmailJaCadastradoException("E-mail já cadastrado");
        }
        if (usuarioRepository.findByUsernameIgnoreCase(username).isPresent()) {
            throw new IllegalArgumentException("Nome de usuário já está em uso.");
        }

        if (Boolean.FALSE.equals(registerRequestDTO.termosAceitos())) {
            throw new IllegalArgumentException("É necessário aceitar os Termos de Uso e a Política de Privacidade.");
        }

        if (!registerRequestDTO.password().equals(registerRequestDTO.passwordConfirmation())){
            throw new RuntimeException("As senhas não são iguais");
        }

        Usuario novoUsuario = registerRequestDTO.toEntity();
        novoUsuario.setEmail(email);
        novoUsuario.setUsername(username);
        novoUsuario.setSenha(passwordEncoder.encode(registerRequestDTO.password()));
        novoUsuario.setVersaoTermos(Usuario.VERSAO_TERMOS_ATUAL);
        novoUsuario.setTermosAceitosEm(LocalDateTime.now());

        boolean requerConfirmacao = emailSenderService != null && emailSenderService.isEnabled();
        String rawVerificationToken = null;
        if (requerConfirmacao) {
            rawVerificationToken = gerarTokenAleatorio();
            novoUsuario.setEmailVerificado(false);
            novoUsuario.setEmailVerificacaoTokenHash(sha256Hex(rawVerificationToken));
            novoUsuario.setEmailVerificacaoExpiraEm(Instant.now().plus(EMAIL_VERIFICATION_TOKEN_DURATION));
        } else {
            novoUsuario.setEmailVerificado(true);
        }

        Usuario usuarioSalvo = usuarioRepository.save(novoUsuario);
        criarContaPadrao(usuarioSalvo);

        if (requerConfirmacao) {
            authRateLimitService.tryConsumeEmailSend("verify-email", email);
            String verificationLink = resolverBaseUrl() + "/confirmar-email?token="
                    + URLEncoder.encode(rawVerificationToken, StandardCharsets.UTF_8);
            emailSenderService.sendEmailVerificationEmail(
                    usuarioSalvo.getEmail(),
                    resolverNomeExibicao(usuarioSalvo),
                    verificationLink
            );
        }
    }

    private JsonNode verifyGoogleIdToken(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new AuthenticationCredentialsNotFoundException("Token do Google não informado.");
        }
        try {
            String encodedToken = URLEncoder.encode(idToken.strip(), StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + encodedToken))
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();

            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (httpResponse.statusCode() != 200) {
                throw new AuthenticationCredentialsNotFoundException("Token do Google inválido ou expirado.");
            }

            JsonNode json = objectMapper.readTree(httpResponse.body());
            if (!"true".equalsIgnoreCase(json.path("email_verified").asText(""))) {
                throw new AuthenticationCredentialsNotFoundException("E-mail do Google não verificado.");
            }
            if (googleClientId == null || googleClientId.isBlank()) {
                throw new AuthenticationCredentialsNotFoundException("Login com Google não está configurado no servidor.");
            }
            String aud = json.path("aud").asText("");
            if (!googleClientId.equals(aud)) {
                throw new AuthenticationCredentialsNotFoundException("Token do Google não pertence a este aplicativo.");
            }
            return json;
        } catch (AuthenticationCredentialsNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Falha ao validar token do Google: {}", e.getMessage());
            throw new AuthenticationCredentialsNotFoundException("Não foi possível validar o login com Google.");
        }
    }

    private Usuario registrarUsuarioGoogle(String email, String nome, String picture) {
        String nomeFinal = (nome == null || nome.isBlank()) ? email.substring(0, email.indexOf('@')) : nome;
        String baseUsername = email.substring(0, email.indexOf('@'))
                .replaceAll("[^a-zA-Z0-9._-]", "")
                .toLowerCase();
        if (baseUsername.length() < 3) {
            baseUsername = "user" + baseUsername;
        }

        String candidate = baseUsername;
        int suffix = 1;
        while (usuarioRepository.existsByUsernameIgnoreCase(candidate)) {
            candidate = baseUsername + suffix++;
        }

        Usuario usuario = new Usuario();
        usuario.setNome(nomeFinal);
        usuario.setEmail(email);
        usuario.setUsername(candidate);
        usuario.setFotoPerfil(picture);
        usuario.setPapel(Papel.USUARIO);
        usuario.setSenha(passwordEncoder.encode(UUID.randomUUID().toString()));
        usuario.setVersaoTermos(Usuario.VERSAO_TERMOS_ATUAL);
        usuario.setTermosAceitosEm(LocalDateTime.now());
        usuario.setEmailVerificado(true);

        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        criarContaPadrao(usuarioSalvo);
        return usuarioSalvo;
    }

    private void criarContaPadrao(Usuario usuarioSalvo) {
        ContaFinanceira contaPadrao = new ContaFinanceira();
        contaPadrao.setNome("Conta Principal");
        contaPadrao.setSaldo(BigDecimal.ZERO);
        contaPadrao.setCor("#4F46E5");
        contaPadrao.setMoeda(Moeda.BRL);
        contaPadrao.setUsuario(usuarioSalvo);
        contaFinanceiraRepository.save(contaPadrao);
    }

    private void changePasswordAfterRecovery(Usuario usuario, String newPassword) {
        usuario.setSenha(passwordEncoder.encode(newPassword));
        usuario.setRecuperacaoTokenHash(null);
        usuario.setRecuperacaoTokenExpiraEm(null);
        usuarioRepository.save(usuario);
        if (usuarioAuthCacheService != null) {
            usuarioAuthCacheService.evictPorUsuario(usuario);
        }
    }

    @Override
    @Transactional
    public void requestPasswordResetEmail(String email) {
        if (emailSenderService == null || !emailSenderService.isEnabled()) {
            return;
        }
        String normalizedEmail = email == null ? "" : email.strip();
        if (normalizedEmail.isBlank()) {
            return;
        }

        Optional<Usuario> usuarioOptional = usuarioRepository.findByEmailIgnoreCase(normalizedEmail);
        if (usuarioOptional.isEmpty()) {
            return;
        }

        Usuario usuario = usuarioOptional.get();
        if (!usuario.isEmailVerificado()) {
            log.info("Recuperação ignorada para conta com e-mail ainda não confirmado id={}.", usuario.getId());
            return;
        }

        Instant agora = Instant.now();
        if (usuario.getRecuperacaoTokenExpiraEm() != null) {
            Instant ultimoEnvio = usuario.getRecuperacaoTokenExpiraEm().minus(EMAIL_RESET_TOKEN_DURATION);
            if (ultimoEnvio.plus(EMAIL_RESEND_COOLDOWN).isAfter(agora)) {
                log.info("Cooldown de reenvio de recuperação ativo para o usuário id={}.", usuario.getId());
                return;
            }
        }

        long backoffSegundos = authRateLimitService.tryConsumeEmailSend("recover-password", usuario.getEmail());
        if (backoffSegundos > 0) {
            log.info("Backoff progressivo de recuperação ativo ({}s) para usuário id={}.", backoffSegundos, usuario.getId());
            return;
        }

        String rawToken = gerarTokenAleatorio();
        usuario.setRecuperacaoTokenHash(sha256Hex(rawToken));
        usuario.setRecuperacaoTokenExpiraEm(agora.plus(EMAIL_RESET_TOKEN_DURATION));
        usuarioRepository.save(usuario);

        String resetLink = resolverBaseUrl() + "/redefinir-senha?token=" + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
        emailSenderService.sendPasswordResetEmail(usuario.getEmail(), resolverNomeExibicao(usuario), resetLink);
    }

    @Override
    @Transactional
    public boolean resetPasswordWithToken(String token, String newPassword) {
        if (token == null || token.isBlank() || newPassword == null || newPassword.isBlank()) {
            return false;
        }
        String tokenHash = sha256Hex(token.strip());
        Optional<Usuario> usuarioOptional = usuarioRepository.findByRecuperacaoTokenHash(tokenHash);
        if (usuarioOptional.isEmpty()) {
            return false;
        }

        Usuario usuario = usuarioOptional.get();
        Instant expiraEm = usuario.getRecuperacaoTokenExpiraEm();
        if (expiraEm == null || !expiraEm.isAfter(Instant.now())) {
            usuario.setRecuperacaoTokenHash(null);
            usuario.setRecuperacaoTokenExpiraEm(null);
            usuarioRepository.save(usuario);
            return false;
        }

        changePasswordAfterRecovery(usuario, newPassword);
        authRateLimitService.resetEmailSend("recover-password", usuario.getEmail());
        return true;
    }

    @Override
    @Transactional
    public boolean verifyEmailToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        String tokenHash = sha256Hex(token.strip());
        Optional<Usuario> usuarioOptional = usuarioRepository.findByEmailVerificacaoTokenHash(tokenHash);
        if (usuarioOptional.isEmpty()) {
            return false;
        }

        Usuario usuario = usuarioOptional.get();
        Instant expiraEm = usuario.getEmailVerificacaoExpiraEm();
        if (expiraEm == null || !expiraEm.isAfter(Instant.now())) {
            usuario.setEmailVerificacaoTokenHash(null);
            usuario.setEmailVerificacaoExpiraEm(null);
            usuarioRepository.save(usuario);
            return false;
        }

        usuario.setEmailVerificado(true);
        usuario.setEmailVerificacaoTokenHash(null);
        usuario.setEmailVerificacaoExpiraEm(null);
        usuarioRepository.save(usuario);
        if (adminBootstrapService != null) {
            adminBootstrapService.promoverSeElegivel(usuario);
        }
        authRateLimitService.resetEmailSend("verify-email", usuario.getEmail());
        return true;
    }

    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        if (emailSenderService == null || !emailSenderService.isEnabled()) {
            return;
        }
        String normalizedEmail = email == null ? "" : email.strip();
        if (normalizedEmail.isBlank()) {
            return;
        }

        Optional<Usuario> usuarioOptional = usuarioRepository.findByEmailIgnoreCase(normalizedEmail);
        if (usuarioOptional.isEmpty()) {
            return;
        }

        Usuario usuario = usuarioOptional.get();
        if (usuario.isEmailVerificado()) {
            return;
        }

        long backoffSegundos = authRateLimitService.tryConsumeEmailSend("verify-email", usuario.getEmail());
        if (backoffSegundos > 0) {
            log.info("Backoff progressivo de confirmação de e-mail ativo ({}s) para usuário id={}.", backoffSegundos, usuario.getId());
            return;
        }

        String rawToken = gerarTokenAleatorio();
        usuario.setEmailVerificacaoTokenHash(sha256Hex(rawToken));
        usuario.setEmailVerificacaoExpiraEm(Instant.now().plus(EMAIL_VERIFICATION_TOKEN_DURATION));
        usuarioRepository.save(usuario);

        String verificationLink = resolverBaseUrl() + "/confirmar-email?token="
                + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
        emailSenderService.sendEmailVerificationEmail(usuario.getEmail(), resolverNomeExibicao(usuario), verificationLink);
    }

    private String gerarTokenAleatorio() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String resolverBaseUrl() {
        return (appWebUrl == null || appWebUrl.isBlank())
                ? "http://localhost:4200"
                : appWebUrl.strip().replaceAll("/+$", "");
    }

    private String resolverNomeExibicao(Usuario usuario) {
        return (usuario.getNome() != null && !usuario.getNome().isBlank())
                ? usuario.getNome()
                : usuario.getUsername();
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 indisponível.", e);
        }
    }

    @Override
    public void logout() {

    }
}
