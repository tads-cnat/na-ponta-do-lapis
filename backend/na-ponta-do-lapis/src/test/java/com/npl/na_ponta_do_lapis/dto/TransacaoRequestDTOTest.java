package com.npl.na_ponta_do_lapis.web.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.npl.na_ponta_do_lapis.entity.enums.EstadoTransacao;
import com.npl.na_ponta_do_lapis.entity.enums.TipoTransacao;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransacaoRequestDTOTest {

    private static final String DESCRICAO_PADRAO = "Mercado";
    private static final BigDecimal VALOR_PADRAO = new BigDecimal("150.75");
    private static final LocalDateTime DATA_PADRAO = LocalDateTime.of(2026, 5, 9, 19, 41, 49);

    private static Validator validator;
    private static ObjectMapper mapper;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    // ---------- Métodos auxiliares ----------

    private TransacaoRequestDTO dto(String descricao, BigDecimal valor, TipoTransacao tipo,
                                    Long idCategoria, Long idContaFinanceira, LocalDateTime dataHora) {
        return new TransacaoRequestDTO(descricao, valor, tipo, EstadoTransacao.values()[0],
                idCategoria, idContaFinanceira, null, dataHora);
    }

    private TransacaoRequestDTO dtoValido() {
        return dto(DESCRICAO_PADRAO, VALOR_PADRAO, TipoTransacao.DESPESA, 10L, 1L, DATA_PADRAO);
    }

    private TransacaoRequestDTO comDescricao(String descricao) {
        return dto(descricao, VALOR_PADRAO, TipoTransacao.DESPESA, 10L, 1L, DATA_PADRAO);
    }

    private TransacaoRequestDTO comValor(BigDecimal valor) {
        return dto(DESCRICAO_PADRAO, valor, TipoTransacao.DESPESA, 10L, 1L, DATA_PADRAO);
    }

    /** Mensagens de violação apenas do campo informado. */
    private List<String> mensagensDe(TransacaoRequestDTO dto, String campo) {
        return validator.validate(dto).stream()
                .filter(v -> v.getPropertyPath().toString().equals(campo))
                .map(ConstraintViolation::getMessage)
                .toList();
    }

    private String json(String dataHora) {
        return """
                {"descricao":"Mercado","valor":150.75,"tipo":"DESPESA",
                 "idCategoria":10,"idContaFinanceira":1,"dataHora":"%s"}
                """.formatted(dataHora);
    }

    // =====================================================================
    // SUCESSO
    // =====================================================================

    @Test
    void deveAceitarDtoValido() {
        assertThat(validator.validate(dtoValido())).as("violações de um DTO válido").isEmpty();
    }

    @Test
    void deveAceitarEstadoNulo() { // estado é opcional
        TransacaoRequestDTO dto = new TransacaoRequestDTO(DESCRICAO_PADRAO, VALOR_PADRAO,
                TipoTransacao.DESPESA, null, 10L, 1L, null, DATA_PADRAO);
        assertThat(mensagensDe(dto, "estado")).as("estado é opcional").isEmpty();
    }

    @Test
    void deveAceitarMarcadorNulo() { // marcadorId é opcional
        assertThat(mensagensDe(dtoValido(), "marcadorId")).as("marcador é opcional").isEmpty();
    }

    // =====================================================================
    // DESCRIÇÃO: nula, vazia e em branco (inválidas) | 1..254 (válida) | > 254 (inválida)
    // =====================================================================

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void deveRejeitarDescricaoNulaVaziaOuEmBranco(String descricao) {
        assertThat(mensagensDe(comDescricao(descricao), "descricao"))
                .as("descrição '%s' deve ser rejeitada", descricao).isNotEmpty();
    }

    @Test
    void deveExibirMensagemParaDescricaoEmBranco() {
        assertThat(mensagensDe(comDescricao("   "), "descricao"))
                .as("mensagem de descrição em branco").contains("O campo não pode estar vazio!");
    }

    @Test
    void deveAceitarDescricaoComUmCaractere() { // limite inferior
        assertThat(mensagensDe(comDescricao("a"), "descricao"))
                .as("descrição com 1 caractere").isEmpty();
    }

    @Test
    void deveAceitarDescricaoComExatamente254Caracteres() { // limite superior
        assertThat(mensagensDe(comDescricao("a".repeat(254)), "descricao"))
                .as("descrição com 254 caracteres").isEmpty();
    }

    @Test
    void deveRejeitarDescricaoComMaisDe254Caracteres() { // logo acima do limite
        assertThat(mensagensDe(comDescricao("a".repeat(255)), "descricao"))
                .as("descrição com 255 caracteres").isNotEmpty();
    }

    // =====================================================================
    // VALOR: nulo | negativo | zero (inválidos) | 0.01 e positivo (válidos)
    // =====================================================================

    @Test
    void deveRejeitarValorNulo() {
        assertThat(mensagensDe(comValor(null), "valor")).as("valor nulo").isNotEmpty();
    }

    @Test
    void deveRejeitarValorNegativo() { // fronteira logo abaixo de zero
        assertThat(mensagensDe(comValor(new BigDecimal("-0.01")), "valor")).as("valor -0.01").isNotEmpty();
    }

    @Test
    void deveRejeitarValorZero() { // fronteira
        assertThat(mensagensDe(comValor(BigDecimal.ZERO), "valor")).as("valor zero").isNotEmpty();
    }

    @Test
    void deveExibirMensagemParaValorZero() {
        assertThat(mensagensDe(comValor(BigDecimal.ZERO), "valor"))
                .as("mensagem de valor zero").contains("O valor da transação deve ser maior que zero!");
    }

    @Test
    void deveAceitarValorMinimoPositivo() { // fronteira logo acima de zero
        assertThat(mensagensDe(comValor(new BigDecimal("0.01")), "valor")).as("valor 0.01").isEmpty();
    }

    @Test
    void deveAceitarValorPositivoComum() {
        assertThat(mensagensDe(comValor(new BigDecimal("1000.00")), "valor")).as("valor 1000.00").isEmpty();
    }

    // =====================================================================
    // Demais campos obrigatórios
    // =====================================================================

    @Test
    void deveRejeitarTipoNulo() {
        TransacaoRequestDTO dto = dto(DESCRICAO_PADRAO, VALOR_PADRAO, null, 10L, 1L, DATA_PADRAO);
        assertThat(mensagensDe(dto, "tipo")).as("tipo nulo").isNotEmpty();
    }

    @Test
    void deveRejeitarIdCategoriaNulo() {
        TransacaoRequestDTO dto = dto(DESCRICAO_PADRAO, VALOR_PADRAO, TipoTransacao.DESPESA, null, 1L, DATA_PADRAO);
        assertThat(mensagensDe(dto, "idCategoria")).as("idCategoria nulo").isNotEmpty();
    }

    @Test
    void deveRejeitarIdContaFinanceiraNulo() {
        TransacaoRequestDTO dto = dto(DESCRICAO_PADRAO, VALOR_PADRAO, TipoTransacao.DESPESA, 10L, null, DATA_PADRAO);
        assertThat(mensagensDe(dto, "idContaFinanceira")).as("idContaFinanceira nulo").isNotEmpty();
    }

    @Test
    void deveRejeitarDataHoraNula() {
        TransacaoRequestDTO dto = dto(DESCRICAO_PADRAO, VALOR_PADRAO, TipoTransacao.DESPESA, 10L, 1L, null);
        assertThat(mensagensDe(dto, "dataHora")).as("dataHora nula").isNotEmpty();
    }

    @Test
    void deveReportarUmaViolacaoPorCampoInvalido() {
        TransacaoRequestDTO dto = dto(DESCRICAO_PADRAO, VALOR_PADRAO, null, 10L, 1L, DATA_PADRAO);
        assertThat(validator.validate(dto)).as("apenas o tipo está inválido").hasSize(1);
    }

    // =====================================================================
    // JSON de entrada: @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    // =====================================================================

    @Test
    void deveDesserializarDataHoraNoFormatoDeclarado() throws Exception {
        TransacaoRequestDTO dto = mapper.readValue(json("2026-05-09T19:41:49.000Z"), TransacaoRequestDTO.class);
        assertThat(dto.dataHora()).as("dataHora lida do JSON").isEqualTo(DATA_PADRAO);
    }

    @Test
    void deveDesserializarValorDecimal() throws Exception {
        TransacaoRequestDTO dto = mapper.readValue(json("2026-05-09T19:41:49.000Z"), TransacaoRequestDTO.class);
        assertThat(dto.valor()).as("valor lido do JSON").isEqualByComparingTo("150.75");
    }

    @Test
    void deveRejeitarDataHoraSemMilissegundosENoFormatoDoExemploDoSwagger() {
        // O @Schema do DTO sugere "2026-05-09T19:41:49", mas o @JsonFormat exige ".SSS'Z'"
        assertThatThrownBy(() -> mapper.readValue(json("2026-05-09T19:41:49"), TransacaoRequestDTO.class))
                .as("exemplo do Swagger não bate com o padrão do @JsonFormat")
                .isInstanceOf(JsonProcessingException.class);
    }

    @Test
    void deveRejeitarTipoInexistenteNoJson() {
        String jsonInvalido = json("2026-05-09T19:41:49.000Z").replace("DESPESA", "INEXISTENTE");
        assertThatThrownBy(() -> mapper.readValue(jsonInvalido, TransacaoRequestDTO.class))
                .as("valor de enum desconhecido")
                .isInstanceOf(JsonProcessingException.class);
    }
}