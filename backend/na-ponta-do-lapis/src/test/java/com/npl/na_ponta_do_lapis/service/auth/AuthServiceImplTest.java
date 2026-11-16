package com.npl.na_ponta_do_lapis.service.auth;

import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.npl.na_ponta_do_lapis.entity.enums.Moeda;
import com.npl.na_ponta_do_lapis.repository.ContaFinanceiraRepository;
import com.npl.na_ponta_do_lapis.repository.UsuarioRepository;
import com.npl.na_ponta_do_lapis.security.jwt.JwtUtil;
import com.npl.na_ponta_do_lapis.web.dto.auth.RegisterRequestDTO;
import com.npl.na_ponta_do_lapis.web.exception.EmailJaCadastradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ContaFinanceiraRepository contaFinanceiraRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Captor
    private ArgumentCaptor<ContaFinanceira> contaCaptor;

    @Captor
    private ArgumentCaptor<Usuario> usuarioCaptor;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                authenticationManager,
                jwtUtil,
                usuarioRepository,
                contaFinanceiraRepository,
                passwordEncoder,
                new ObjectMapper(),
                null
        );
    }

    @Test
    @DisplayName("Ao cadastrar usuário, deve criar o usuário e uma conta financeira padrão com saldo zero")
    void deveCriarUsuarioEContaFinanceiraPadraoNoSignup() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
                "João da Silva",
                "joao@email.com",
                "joaosilva",
                "84999999999",
                "senha123",
            "senha123",
            null,
            null
        );

        when(usuarioRepository.findByEmailIgnoreCase("joao@email.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha123")).thenReturn("hash123");

        Usuario usuarioSalvo = new Usuario();
        usuarioSalvo.setId(10L);
        usuarioSalvo.setNome("João da Silva");
        usuarioSalvo.setEmail("joao@email.com");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioSalvo);

        authService.signup(dto, null);

        // Verifica salvamento do usuário com senha criptografada
        verify(usuarioRepository).save(usuarioCaptor.capture());
        Usuario usuarioCriado = usuarioCaptor.getValue();
        assertThat(usuarioCriado.getEmail()).isEqualTo("joao@email.com");
        assertThat(usuarioCriado.getPassword()).isEqualTo("hash123");

        // Verifica criação da conta financeira padrão
        verify(contaFinanceiraRepository).save(contaCaptor.capture());
        ContaFinanceira contaPadrao = contaCaptor.getValue();

        assertThat(contaPadrao.getNome()).isEqualTo("Conta Principal");
        assertThat(contaPadrao.getSaldo()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(contaPadrao.getCor()).isEqualTo("#4F46E5");
        assertThat(contaPadrao.getMoeda()).isEqualTo(Moeda.BRL);
        assertThat(contaPadrao.getUsuario()).isEqualTo(usuarioSalvo);
    }

    @Test
    @DisplayName("Deve rejeitar cadastro quando o e-mail já estiver cadastrado")
    void deveRejeitarQuandoEmailJaExiste() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
                "Maria",
                "maria@email.com",
                "mariasilva",
                null,
                "senha123",
            "senha123",
            null,
            null
        );

        when(usuarioRepository.findByEmailIgnoreCase("maria@email.com")).thenReturn(Optional.of(new Usuario()));

        assertThatThrownBy(() -> authService.signup(dto, null))
                .isInstanceOf(EmailJaCadastradoException.class)
                .hasMessage("E-mail já cadastrado");

        verify(usuarioRepository, never()).save(any());
        verify(contaFinanceiraRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve rejeitar cadastro quando a confirmação de senha for diferente")
    void deveRejeitarQuandoSenhasDiferentes() {
        RegisterRequestDTO dto = new RegisterRequestDTO(
                "Maria",
                "maria@email.com",
                "mariasilva",
                null,
                "senha123",
            "outraSenha",
            null,
            null
        );

        when(usuarioRepository.findByEmailIgnoreCase("maria@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.signup(dto, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("As senhas não são iguais");

        verify(usuarioRepository, never()).save(any());
        verify(contaFinanceiraRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve retornar pergunta cadastrada sem retornar a resposta")
    void deveBuscarPerguntaSeguranca() {
        Usuario usuario = usuarioComPerguntaSeguranca();
        when(usuarioRepository.findByEmailIgnoreCase("ana@email.com")).thenReturn(Optional.of(usuario));

        var response = authService.getRecoveryQuestion("ana@email.com");

        assertThat(response.question()).isEqualTo("Qual era o nome do seu primeiro animal?");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve autorizar a recuperação quando a resposta estiver correta")
    void deveAutorizarRecuperacaoComRespostaCorreta() {
        Usuario usuario = usuarioComPerguntaSeguranca();
        when(usuarioRepository.findByEmailIgnoreCase("ana@email.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("luna", "resposta-hash")).thenReturn(true);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Long authorizedUserId = authService.verifyRecoveryAnswer(" ana@email.com ", "  Luna  ");

        assertThat(authorizedUserId).isEqualTo(42L);
        assertThat(usuario.isSenhaTemporaria()).isFalse();
        verify(passwordEncoder).matches("luna", "resposta-hash");
        verify(usuarioRepository).save(usuario);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Não deve autorizar recuperação para resposta incorreta")
    void naoDeveAutorizarRecuperacaoComRespostaIncorreta() {
        Usuario usuario = usuarioComPerguntaSeguranca();
        when(usuarioRepository.findByEmailIgnoreCase("ana@email.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("errada", "resposta-hash")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Long authorizedUserId = authService.verifyRecoveryAnswer("ana@email.com", "errada");

        assertThat(authorizedUserId).isNull();
        assertThat(usuario.isSenhaTemporaria()).isFalse();
        assertThat(usuario.getTentativasRecuperacao()).isEqualTo(1);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("Deve bloquear a recuperação após cinco respostas incorretas")
    void deveBloquearRecuperacaoAposCincoTentativas() {
        Usuario usuario = usuarioComPerguntaSeguranca();
        when(usuarioRepository.findByEmailIgnoreCase("ana@email.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("errada", "resposta-hash")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        for (int attempt = 0; attempt < 5; attempt++) {
            authService.verifyRecoveryAnswer("ana@email.com", "errada");
        }
        Long blockedUserId = authService.verifyRecoveryAnswer("ana@email.com", "luna");

        assertThat(blockedUserId).isNull();
        assertThat(usuario.getTentativasRecuperacao()).isEqualTo(5);
        assertThat(usuario.getRecuperacaoBloqueadaAte()).isAfter(Instant.now());
        verify(passwordEncoder, times(5)).matches("errada", "resposta-hash");
    }

    @Test
    @DisplayName("Não deve retornar pergunta para e-mail sem pergunta configurada")
    void naoDeveRetornarPerguntaSemConfiguracao() {
        Usuario usuario = new Usuario();
        when(usuarioRepository.findByEmailIgnoreCase("ana@email.com")).thenReturn(Optional.of(usuario));

        var response = authService.getRecoveryQuestion("ana@email.com");

        assertThat(response.question()).isNull();
    }

    private Usuario usuarioComPerguntaSeguranca() {
        Usuario usuario = new Usuario();
        usuario.setId(42L);
        usuario.setEmail("ana@email.com");
        usuario.setPerguntaSeguranca("Qual era o nome do seu primeiro animal?");
        usuario.setRespostaSegurancaHash("resposta-hash");
        return usuario;
    }
}

