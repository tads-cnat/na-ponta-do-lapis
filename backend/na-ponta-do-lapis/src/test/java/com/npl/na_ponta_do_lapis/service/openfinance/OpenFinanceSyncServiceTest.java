package com.npl.na_ponta_do_lapis.service.openfinance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.TipoCategoria;
import com.npl.na_ponta_do_lapis.entity.Transacao;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.UsuarioPluggyIntegracao;
import com.npl.na_ponta_do_lapis.entity.enums.EstadoTransacao;
import com.npl.na_ponta_do_lapis.entity.enums.TipoTransacao;
import com.npl.na_ponta_do_lapis.repository.ContaFinanceiraRepository;
import com.npl.na_ponta_do_lapis.repository.TransacaoRepository;
import com.npl.na_ponta_do_lapis.repository.UsuarioPluggyIntegracaoRepository;
import com.npl.na_ponta_do_lapis.service.crypto.CryptoService;
import com.npl.na_ponta_do_lapis.web.dto.OpenFinanceConectarRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.OpenFinanceStatusResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenFinanceSyncServiceTest {

    @Mock
    private UsuarioPluggyIntegracaoRepository integracaoRepository;

    @Mock
    private ContaFinanceiraRepository contaRepository;

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private PluggyClient pluggyClient;

    @Mock
    private CryptoService cryptoService;

    @Mock
    private PluggyCategoriaMapper categoriaMapper;

    @InjectMocks
    private OpenFinanceSyncService syncService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Dias");
        usuario.setEmail("dias@example.com");
    }

    @Test
    @DisplayName("Deve conectar com credenciais válidas e disparar sincronização inicial")
    void deveConectarComSucesso() throws Exception {
        OpenFinanceConectarRequestDTO dto = new OpenFinanceConectarRequestDTO("client-123", "secret-456");

        when(pluggyClient.autenticar("client-123", "secret-456")).thenReturn("api-key-test");
        when(cryptoService.encrypt("client-123")).thenReturn("enc-client");
        when(cryptoService.encrypt("secret-456")).thenReturn("enc-secret");
        when(cryptoService.mask("client-123")).thenReturn("cli...123");
        when(integracaoRepository.findByUsuarioId(1L)).thenReturn(Optional.empty());

        // Para a sincronização disparada internamente:
        when(cryptoService.decrypt("enc-client")).thenReturn("client-123");
        when(cryptoService.decrypt("enc-secret")).thenReturn("secret-456");
        when(pluggyClient.listarItems("api-key-test")).thenReturn(objectMapper.readTree("[]"));

        OpenFinanceStatusResponseDTO status = syncService.conectar(usuario, dto);

        assertNotNull(status);
        assertTrue(status.conectado());
        assertEquals("cli...123", status.clientIdMascarado());
        verify(integracaoRepository, atLeastOnce()).save(any(UsuarioPluggyIntegracao.class));
    }

    @Test
    @DisplayName("Deve falhar ao conectar quando credenciais forem inválidas na Pluggy")
    void deveFalharAoConectarCredenciaisInvalidas() {
        OpenFinanceConectarRequestDTO dto = new OpenFinanceConectarRequestDTO("client-inv", "secret-inv");

        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Credenciais inválidas"))
                .when(pluggyClient).autenticar("client-inv", "secret-inv");

        assertThrows(ResponseStatusException.class, () -> syncService.conectar(usuario, dto));
        verify(integracaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve desconectar removendo integração salva")
    void deveDesconectar() {
        UsuarioPluggyIntegracao integracao = new UsuarioPluggyIntegracao();
        integracao.setUsuario(usuario);
        when(integracaoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(integracao));

        syncService.desconectar(usuario);

        verify(integracaoRepository).delete(integracao);
    }

    @Test
    @DisplayName("Deve bloquear sincronização manual quando atingir limite de 5x ao dia")
    void deveBloquearLimiteManual() {
        UsuarioPluggyIntegracao integracao = new UsuarioPluggyIntegracao();
        integracao.setUsuario(usuario);
        integracao.setDataContadorManual(LocalDate.now());
        integracao.setSincronizacoesManuaisHoje(5);

        when(integracaoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(integracao));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> syncService.sincronizarManual(usuario));
        assertEquals(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS, ex.getStatusCode());
        assertTrue(ex.getReason().contains("limite de 5 sincronizações"));
    }

    @Test
    @DisplayName("Deve sincronizar contas e transações com deduplicação correta")
    void deveSincronizarContasETransacoes() throws Exception {
        UsuarioPluggyIntegracao integracao = new UsuarioPluggyIntegracao();
        integracao.setId(10L);
        integracao.setUsuario(usuario);
        integracao.setClientIdCriptografado("enc-client");
        integracao.setClientSecretCriptografado("enc-secret");
        integracao.setDataContadorManual(LocalDate.now());
        integracao.setSincronizacoesManuaisHoje(1);

        when(integracaoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(integracao));
        when(cryptoService.decrypt("enc-client")).thenReturn("client-123");
        when(cryptoService.decrypt("enc-secret")).thenReturn("secret-456");
        when(pluggyClient.autenticar("client-123", "secret-456")).thenReturn("valid-api-key");

        // Mock Pluggy Items e Accounts
        String itemsJson = "[{\"id\":\"item-1\"}]";
        when(pluggyClient.listarItems("valid-api-key")).thenReturn(objectMapper.readTree(itemsJson));

        // Mock Pluggy Account Json
        String accountsJson = "[{\"id\":\"pluggy-acc-1\",\"name\":\"Conta Corrente Nubank\",\"balance\":1500.00}]";
        JsonNode accountsNode = objectMapper.readTree(accountsJson);
        when(pluggyClient.listarContasPorItem("valid-api-key", "item-1")).thenReturn(accountsNode);

        when(contaRepository.findByPluggyAccountIdAndUsuarioId("pluggy-acc-1", 1L)).thenReturn(Optional.empty());
        ContaFinanceira savedConta = new ContaFinanceira();
        savedConta.setId(99L);
        savedConta.setNome("Conta Corrente Nubank (Open Finance)");
        savedConta.setPluggyAccountId("pluggy-acc-1");
        savedConta.setSincronizada(true);
        when(contaRepository.save(any(ContaFinanceira.class))).thenReturn(savedConta);

        // Mock Pluggy Transaction Json
        String txJson = "[{\"id\":\"tx-pluggy-100\",\"description\":\"Uber viagem\",\"amount\":-25.50,\"date\":\"2026-11-05T10:00:00Z\",\"category\":\"Transportation\",\"status\":\"POSTED\"}]";
        JsonNode txNode = objectMapper.readTree(txJson);

        when(pluggyClient.listarTransacoes(eq("valid-api-key"), eq("pluggy-acc-1"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(txNode);

        when(transacaoRepository.findByPluggyTransactionId("tx-pluggy-100")).thenReturn(Optional.empty());

        TipoCategoria catTransporte = new TipoCategoria();
        catTransporte.setNome("Transporte");
        when(categoriaMapper.mapearCategoria("Transportation", "Uber viagem")).thenReturn(catTransporte);

        OpenFinanceStatusResponseDTO status = syncService.sincronizarManual(usuario);

        assertNotNull(status);
        assertEquals(2, integracao.getSincronizacoesManuaisHoje());
        assertEquals(3, status.sincronizacoesRestantesHoje());

        verify(transacaoRepository).save(argThat(t ->
                t.getPluggyTransactionId().equals("tx-pluggy-100") &&
                t.getTipo() == TipoTransacao.DESPESA &&
                t.getValor().compareTo(new BigDecimal("25.5")) == 0 &&
                t.getEstado() == EstadoTransacao.REALIZADA
        ));
    }

    @Test
    @DisplayName("Deve gerar connect token para abrir Pluggy Connect")
    void deveGerarConnectToken() {
        UsuarioPluggyIntegracao integracao = new UsuarioPluggyIntegracao();
        integracao.setUsuario(usuario);
        integracao.setClientIdCriptografado("enc-client");
        integracao.setClientSecretCriptografado("enc-secret");
        when(integracaoRepository.findByUsuarioId(1L)).thenReturn(Optional.of(integracao));
        when(cryptoService.decrypt("enc-client")).thenReturn("client-123");
        when(cryptoService.decrypt("enc-secret")).thenReturn("secret-456");
        when(pluggyClient.autenticar("client-123", "secret-456")).thenReturn("apiKey-123");
        when(pluggyClient.criarConnectToken("apiKey-123")).thenReturn("connect-token-xyz");

        String token = syncService.gerarConnectToken(usuario);

        assertEquals("connect-token-xyz", token);
    }
}
