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
import com.npl.na_ponta_do_lapis.web.dto.auth.LoginRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.PasswordRecoveryResponseDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.RegisterRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.TokenResponseDTO;
import com.npl.na_ponta_do_lapis.web.exception.EmailJaCadastradoException;
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
import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
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
    private final HttpClient httpClient;
    private final UsuarioAuthCacheService usuarioAuthCacheService;

    @Value("${google.client-id:}")
    private String googleClientId;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil,
                           UsuarioRepository usuarioRepository,
                           ContaFinanceiraRepository contaFinanceiraRepository,
                           PasswordEncoder passwordEncoder) {
        this(authenticationManager, jwtUtil, usuarioRepository, contaFinanceiraRepository, passwordEncoder, new ObjectMapper(), null);
    }

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil,
                           UsuarioRepository usuarioRepository,
                           ContaFinanceiraRepository contaFinanceiraRepository,
                           PasswordEncoder passwordEncoder,
                           ObjectMapper objectMapper) {
        this(authenticationManager, jwtUtil, usuarioRepository, contaFinanceiraRepository, passwordEncoder, objectMapper, null);
    }

    @Autowired
    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil,
                           UsuarioRepository usuarioRepository,
                           ContaFinanceiraRepository contaFinanceiraRepository,
                           PasswordEncoder passwordEncoder,
                           ObjectMapper objectMapper,
                           UsuarioAuthCacheService usuarioAuthCacheService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
        this.contaFinanceiraRepository = contaFinanceiraRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
        this.usuarioAuthCacheService = usuarioAuthCacheService;
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

        String token = jwtUtil.generateToken(usuario, jwtUtil.EXPIRATION_TOKEN(Date.from(Instant.now())), usuario.getEmail());
        return new TokenResponseDTO(token);
    }

    @Override
    @Transactional
    public TokenResponseDTO loginWithGoogle(String idToken) {
        JsonNode tokenInfo = verifyGoogleIdToken(idToken);
        String email = tokenInfo.path("email").asText("").strip().toLowerCase();
        String nome = tokenInfo.path("name").asText("").strip();
        String picture = tokenInfo.path("picture").asText(null);

        if (email.isBlank()) {
            throw new AuthenticationCredentialsNotFoundException("Token do Google sem e-mail válido.");
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> registrarUsuarioGoogle(email, nome, picture));

        String token = jwtUtil.generateToken(usuario, jwtUtil.EXPIRATION_TOKEN(Date.from(Instant.now())), usuario.getEmail());
        return new TokenResponseDTO(token);
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

        if (!registerRequestDTO.password().equals(registerRequestDTO.passwordConfirmation())){
            throw new RuntimeException("As senhas não são iguais");
        }

        Usuario novoUsuario = registerRequestDTO.toEntity();
        novoUsuario.setEmail(email);
        novoUsuario.setUsername(username);
        novoUsuario.setSenha(passwordEncoder.encode(registerRequestDTO.password()));

        Usuario usuarioSalvo = usuarioRepository.save(novoUsuario);
        criarContaPadrao(usuarioSalvo);
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
            if (googleClientId != null && !googleClientId.isBlank()) {
                String aud = json.path("aud").asText("");
                if (!googleClientId.equals(aud)) {
                    throw new AuthenticationCredentialsNotFoundException("Token do Google não pertence a este aplicativo.");
                }
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

    @Override
    @Transactional
    public PasswordRecoveryResponseDTO recoverPassword(String email) {
        String normalizedEmail = email == null ? "" : email.strip();
        Optional<Usuario> usuarioOptional = usuarioRepository.findByEmailIgnoreCase(normalizedEmail);
        if (usuarioOptional.isEmpty()) {
            return new PasswordRecoveryResponseDTO(
                    "Se o e-mail estiver cadastrado, uma senha temporária foi gerada.",
                    null
            );
        }

        Usuario usuario = usuarioOptional.get();
        String temporaryPassword = "NPL-" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 16).toUpperCase(Locale.ROOT);
        usuario.setSenha(passwordEncoder.encode(temporaryPassword));
        usuario.setSenhaTemporaria(true);
        usuarioRepository.save(usuario);
        if (usuarioAuthCacheService != null) {
            usuarioAuthCacheService.evictPorUsuario(usuario);
        }

        return new PasswordRecoveryResponseDTO(
                "Se o e-mail estiver cadastrado, uma senha temporária foi gerada. Use-a para entrar e redefina sua senha no perfil.",
                temporaryPassword
        );
    }

    @Override
    public void logout() {

    }
}
