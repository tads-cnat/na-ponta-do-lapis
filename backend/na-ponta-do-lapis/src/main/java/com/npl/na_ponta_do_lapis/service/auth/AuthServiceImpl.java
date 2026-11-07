package com.npl.na_ponta_do_lapis.service.auth;

import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.Moeda;
import com.npl.na_ponta_do_lapis.repository.ContaFinanceiraRepository;
import com.npl.na_ponta_do_lapis.repository.UsuarioRepository;
import com.npl.na_ponta_do_lapis.security.jwt.JwtUtil;
import com.npl.na_ponta_do_lapis.web.dto.UsuarioDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.LoginRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.RegisterRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.auth.TokenResponseDTO;
import com.npl.na_ponta_do_lapis.web.exception.EmailJaCadastradoException;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;

@Service
public class AuthServiceImpl implements AuthService{

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;
    private final ContaFinanceiraRepository contaFinanceiraRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil,
                           UsuarioRepository usuarioRepository,
                           ContaFinanceiraRepository contaFinanceiraRepository,
                           PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
        this.contaFinanceiraRepository = contaFinanceiraRepository;
        this.passwordEncoder = passwordEncoder;
    }





    @Override
    public boolean hasUserWithEmail(String email) {
        return false;
    }

    @Override
    public TokenResponseDTO login(LoginRequestDTO loginRequestDTO, HttpServletResponse response) {

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginRequestDTO.email(), loginRequestDTO.password());
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Usuario usuario = usuarioRepository.findByEmail(loginRequestDTO.email()).orElseThrow(
                    () -> new UsernameNotFoundException("Usuário de email: " + loginRequestDTO.email()+ " não encontrado")
            );

            String token = jwtUtil.generateToken(usuario, jwtUtil.EXPIRATION_TOKEN(Date.from(Instant.now())), usuario.getEmail());

            return new TokenResponseDTO(token);
    }

    @Override
    @Transactional
    public void signup(RegisterRequestDTO registerRequestDTO, HttpServletResponse response) {
        if (usuarioRepository.findByEmail(registerRequestDTO.email()).isPresent()) {
            throw new EmailJaCadastradoException("E-mail já cadastrado");
        }

        if (!registerRequestDTO.password().equals(registerRequestDTO.passwordConfirmation())){
            throw new RuntimeException("As senhas não são iguais");
        }

        Usuario novoUsuario = registerRequestDTO.toEntity();
        novoUsuario.setSenha(passwordEncoder.encode(registerRequestDTO.password()));

        Usuario usuarioSalvo = usuarioRepository.save(novoUsuario);

        ContaFinanceira contaPadrao = new ContaFinanceira();
        contaPadrao.setNome("Conta Principal");
        contaPadrao.setSaldo(BigDecimal.ZERO);
        contaPadrao.setCor("#4F46E5");
        contaPadrao.setMoeda(Moeda.BRL);
        contaPadrao.setUsuario(usuarioSalvo);

        contaFinanceiraRepository.save(contaPadrao);
    }

    @Override
    public void logout() {

    }
}
