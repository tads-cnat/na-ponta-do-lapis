package com.npl.na_ponta_do_lapis.service.auth;

import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.Usuario;
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
                passwordEncoder
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
                "senha123"
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
                "senha123"
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
                "outraSenha"
        );

        when(usuarioRepository.findByEmailIgnoreCase("maria@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.signup(dto, null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("As senhas não são iguais");

        verify(usuarioRepository, never()).save(any());
        verify(contaFinanceiraRepository, never()).save(any());
    }

        @Test
        @DisplayName("Deve gerar senha temporária e marcar o usuário para redefinição")
        void deveGerarSenhaTemporaria() {
                Usuario usuario = new Usuario();
                usuario.setEmail("ana@email.com");
                when(usuarioRepository.findByEmailIgnoreCase("ana@email.com")).thenReturn(Optional.of(usuario));
                when(passwordEncoder.encode(anyString())).thenReturn("senha-criptografada");
                when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

                var response = authService.recoverPassword(" ana@email.com ");

                assertThat(response.temporaryPassword()).startsWith("NPL-");
                assertThat(usuario.getPassword()).isEqualTo("senha-criptografada");
                assertThat(usuario.isSenhaTemporaria()).isTrue();
                verify(usuarioRepository).save(usuario);
        }

        @Test
        @DisplayName("Não deve revelar se um e-mail não cadastrado existe")
        void deveResponderGenericamenteParaEmailNaoCadastrado() {
                when(usuarioRepository.findByEmailIgnoreCase("naoexiste@email.com")).thenReturn(Optional.empty());

                var response = authService.recoverPassword("naoexiste@email.com");

                assertThat(response.temporaryPassword()).isNull();
                assertThat(response.message()).contains("Se o e-mail estiver cadastrado");
                verify(usuarioRepository, never()).save(any());
                verify(passwordEncoder, never()).encode(anyString());
        }
}

