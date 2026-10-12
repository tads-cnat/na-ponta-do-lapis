package com.npl.na_ponta_do_lapis.web.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.Marcador;
import com.npl.na_ponta_do_lapis.entity.TipoCategoria;
import com.npl.na_ponta_do_lapis.entity.Transacao;
import com.npl.na_ponta_do_lapis.entity.enums.EstadoTransacao;
import com.npl.na_ponta_do_lapis.entity.enums.TipoTransacao;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransacoesResponseDTOTest {

    private static final LocalDateTime DATA_HORA = LocalDateTime.of(2026, 10, 1, 14, 30, 0);

    private static ObjectMapper mapper;

    private Transacao transacao;

    @BeforeAll
    static void setUpMapper() {
        mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @BeforeEach
    void setUp() {
        TipoCategoria categoria = new TipoCategoria();
        categoria.setId(10L);
        categoria.setNome("Alimentação");

        ContaFinanceira conta = new ContaFinanceira();
        conta.setId(1L);
        conta.setNome("Conta Corrente");

        transacao = new Transacao();
        transacao.setId(100L);
        transacao.setDescricao("Mercado");
        transacao.setValor(new BigDecimal("150.75"));
        transacao.setTipo(TipoTransacao.DESPESA);
        transacao.setEstado(EstadoTransacao.values()[0]);
        transacao.setDataHora(DATA_HORA);
        transacao.setCategoria(categoria);
        transacao.setContaFinanceira(conta);
    }

    private Marcador criarMarcador() {
        Marcador m = new Marcador();
        m.setId(20L);
        m.setNome("Urgente");
        m.setCor("#00FF00");
        return m;
    }

    // =====================================================================
    // SAÍDA: campos simples
    // =====================================================================

    @Test
    void deveCopiarId() {
        assertThat(new TransacoesResponseDTO(transacao).id()).as("id").isEqualTo(100L);
    }

    @Test
    void deveCopiarDescricao() {
        assertThat(new TransacoesResponseDTO(transacao).descricao()).as("descrição").isEqualTo("Mercado");
    }

    @Test
    void deveCopiarValor() {
        assertThat(new TransacoesResponseDTO(transacao).valor()).as("valor").isEqualByComparingTo("150.75");
    }

    @Test
    void deveCopiarTipo() {
        assertThat(new TransacoesResponseDTO(transacao).tipo()).as("tipo").isEqualTo(TipoTransacao.DESPESA);
    }

    @Test
    void deveCopiarEstado() {
        assertThat(new TransacoesResponseDTO(transacao).estado()).as("estado").isEqualTo(EstadoTransacao.values()[0]);
    }

    @Test
    void deveCopiarDataHora() {
        assertThat(new TransacoesResponseDTO(transacao).dataHora()).as("dataHora").isEqualTo(DATA_HORA);
    }

    // =====================================================================
    // SAÍDA: objetos resumidos
    // =====================================================================

    @Test
    void deveResumirIdDaCategoria() {
        assertThat(new TransacoesResponseDTO(transacao).categoria().id()).as("id da categoria").isEqualTo(10L);
    }

    @Test
    void deveResumirNomeDaCategoria() {
        assertThat(new TransacoesResponseDTO(transacao).categoria().nome()).as("nome da categoria").isEqualTo("Alimentação");
    }

    @Test
    void deveResumirIdDaConta() {
        assertThat(new TransacoesResponseDTO(transacao).conta().id()).as("id da conta").isEqualTo(1L);
    }

    @Test
    void deveResumirNomeDaConta() {
        assertThat(new TransacoesResponseDTO(transacao).conta().nome()).as("nome da conta").isEqualTo("Conta Corrente");
    }

    // =====================================================================
    // SAÍDA: marcador opcional
    // =====================================================================

    @Test
    void deveManterMarcadorNuloQuandoTransacaoNaoTemMarcador() {
        assertThat(new TransacoesResponseDTO(transacao).marcador()).as("marcador ausente").isNull();
    }

    @Test
    void deveResumirIdDoMarcador() {
        transacao.setMarcador(criarMarcador());
        assertThat(new TransacoesResponseDTO(transacao).marcador().id()).as("id do marcador").isEqualTo(20L);
    }

    @Test
    void deveResumirNomeDoMarcador() {
        transacao.setMarcador(criarMarcador());
        assertThat(new TransacoesResponseDTO(transacao).marcador().nome()).as("nome do marcador").isEqualTo("Urgente");
    }

    @Test
    void deveResumirCorDoMarcador() {
        transacao.setMarcador(criarMarcador());
        assertThat(new TransacoesResponseDTO(transacao).marcador().cor()).as("cor do marcador").isEqualTo("#00FF00");
    }

    // =====================================================================
    // FALHA: dados obrigatórios ausentes na entity
    // =====================================================================

    @Test
    void deveFalharQuandoTransacaoNaoTemCategoria() {
        transacao.setCategoria(null);
        assertThatThrownBy(() -> new TransacoesResponseDTO(transacao))
                .as("categoria nula na entity")
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void deveFalharQuandoTransacaoNaoTemContaFinanceira() {
        transacao.setContaFinanceira(null);
        assertThatThrownBy(() -> new TransacoesResponseDTO(transacao))
                .as("conta financeira nula na entity")
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void deveFalharQuandoTransacaoForNula() {
        assertThatThrownBy(() -> new TransacoesResponseDTO(null))
                .as("transação nula")
                .isInstanceOf(NullPointerException.class);
    }

    // =====================================================================
    // JSON de saída: @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    // =====================================================================

    @Test
    void deveSerializarDataHoraSemMilissegundosENemFuso() throws Exception {
        JsonNode json = mapper.readTree(mapper.writeValueAsString(new TransacoesResponseDTO(transacao)));
        assertThat(json.get("dataHora").asText()).as("dataHora no JSON").isEqualTo("2026-10-01T14:30:00");
    }

    @Test
    void deveSerializarMarcadorNuloQuandoAusente() throws Exception {
        JsonNode json = mapper.readTree(mapper.writeValueAsString(new TransacoesResponseDTO(transacao)));
        assertThat(json.get("marcador").isNull()).as("marcador no JSON").isTrue();
    }

    @Test
    void deveSerializarTipoComoTexto() throws Exception {
        JsonNode json = mapper.readTree(mapper.writeValueAsString(new TransacoesResponseDTO(transacao)));
        assertThat(json.get("tipo").asText()).as("tipo no JSON").isEqualTo("DESPESA");
    }
}