package com.npl.na_ponta_do_lapis.entity;

import com.npl.na_ponta_do_lapis.entity.enums.EstadoTransacao;
import com.npl.na_ponta_do_lapis.entity.enums.Moeda;
import com.npl.na_ponta_do_lapis.entity.enums.TipoTransacao;
import com.npl.na_ponta_do_lapis.repository.TransacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("h2")
@TestPropertySource(properties = "spring.sql.init.mode=never")
class TransacaoEntityTest {

    private static final LocalDateTime DATA_HORA = LocalDateTime.of(2026, 10, 1, 14, 30);

    @Autowired TestEntityManager em;
    @Autowired TransacaoRepository repository;

    private Usuario usuario;
    private TipoCategoria categoria;
    private ContaFinanceira conta;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setNome("Pedro");
        usuario.setEmail("pedro@email.com");
        usuario.setUsername("pedro");
        usuario.setSenha("123456");
        em.persist(usuario);

        categoria = new TipoCategoria();
        categoria.setNome("Alimentação");
        em.persist(categoria);

        conta = new ContaFinanceira();
        conta.setNome("Conta Corrente");
        conta.setSaldo(new BigDecimal("1000.00"));
        conta.setCor("#FF0000");
        conta.setMoeda(Moeda.values()[0]); // ou um valor explícito, ex.: Moeda.BRL
        conta.setUsuario(usuario);
        em.persist(conta);
    }

    // ---------- Métodos auxiliares ----------

    private Marcador criarMarcador() {
        Marcador m = new Marcador();
        m.setNome("Urgente");
        m.setCor("#00FF00");
        m.setUsuario(usuario);
        return em.persistAndFlush(m);
    }

    private Transacao transacaoValida() {
        Transacao t = new Transacao();
        t.setDescricao("Mercado");
        t.setValor(new BigDecimal("150.75"));
        t.setCategoria(categoria);
        t.setContaFinanceira(conta);
        t.setEstado(EstadoTransacao.values()[0]); // ou um valor explícito do seu enum
        t.setTipo(TipoTransacao.DESPESA);
        t.setDataHora(DATA_HORA);
        return t;
    }

    /** Salva, limpa o cache de 1º nível e relê do banco. */
    private Transacao salvarELer(Transacao t) {
        Long id = repository.saveAndFlush(t).getId();
        em.clear();
        return repository.findById(id).orElseThrow();
    }

    private Object consultarColuna(String coluna, Long id) {
        return em.getEntityManager()
                .createNativeQuery("select " + coluna + " from transacao where id = ?1")
                .setParameter(1, id)
                .getSingleResult();
    }

    // =====================================================================
    // SUCESSO: persistência com dados válidos (um método por atributo)
    // =====================================================================

    @Test
    void devePersistirTransacaoValidaGerandoId() {
        Transacao lida = salvarELer(transacaoValida());
        assertThat(lida.getId()).as("o id deve ser gerado ao salvar").isNotNull();
    }

    @Test
    void devePreservarDescricaoAoPersistir() {
        Transacao lida = salvarELer(transacaoValida());
        assertThat(lida.getDescricao()).as("descrição persistida").isEqualTo("Mercado");
    }

    @Test
    void devePreservarValorAoPersistir() {
        Transacao lida = salvarELer(transacaoValida());
        assertThat(lida.getValor()).as("valor persistido").isEqualByComparingTo("150.75");
    }

    @Test
    void devePreservarTipoAoPersistir() {
        Transacao lida = salvarELer(transacaoValida());
        assertThat(lida.getTipo()).as("tipo persistido").isEqualTo(TipoTransacao.DESPESA);
    }

    @Test
    void devePreservarEstadoAoPersistir() {
        Transacao lida = salvarELer(transacaoValida());
        assertThat(lida.getEstado()).as("estado persistido").isEqualTo(EstadoTransacao.values()[0]);
    }

    @Test
    void devePreservarDataHoraAoPersistir() {
        Transacao lida = salvarELer(transacaoValida());
        assertThat(lida.getDataHora()).as("data e hora persistidas").isEqualTo(DATA_HORA);
    }

    @Test
    void devePreservarCategoriaAoPersistir() {
        Transacao lida = salvarELer(transacaoValida());
        assertThat(lida.getCategoria().getId())
                .as("id da categoria associada").isEqualTo(categoria.getId());
    }

    @Test
    void devePreservarContaFinanceiraAoPersistir() {
        Transacao lida = salvarELer(transacaoValida());
        assertThat(lida.getContaFinanceira().getId())
                .as("id da conta financeira associada").isEqualTo(conta.getId());
    }

    // =====================================================================
    // Marcador (opcional) e @OnDelete(SET_NULL)
    // =====================================================================

    @Test
    void devePersistirSemMarcador() {
        Transacao lida = salvarELer(transacaoValida());
        assertThat(lida.getMarcador()).as("marcador é opcional, deve ficar nulo").isNull();
    }

    @Test
    void devePreservarMarcadorAoPersistir() {
        Marcador marcador = criarMarcador();
        Transacao t = transacaoValida();
        t.setMarcador(marcador);

        Transacao lida = salvarELer(t);

        assertThat(lida.getMarcador().getId())
                .as("id do marcador associado").isEqualTo(marcador.getId());
    }

    @Test
    void deveZerarMarcadorQuandoMarcadorForExcluido() {
        Marcador marcador = criarMarcador();
        Transacao t = transacaoValida();
        t.setMarcador(marcador);
        Long id = repository.saveAndFlush(t).getId();

        em.clear();
        Marcador marcadorGerenciado = em.find(Marcador.class, marcador.getId());
        em.remove(marcadorGerenciado);
        em.flush();
        em.clear(); // força reler do banco

        Transacao recarregada = repository.findById(id).orElseThrow();
        assertThat(recarregada.getMarcador())
                .as("@OnDelete(SET_NULL) deve anular o marcador excluído").isNull();
    }

    // =====================================================================
    // Enums: gravados como String e todos os valores aceitos
    // =====================================================================

    @Test
    void deveGravarTipoComoString() {
        Long id = repository.saveAndFlush(transacaoValida()).getId();
        assertThat(consultarColuna("tipo", id)).as("coluna tipo no banco").isEqualTo("DESPESA");
    }

    @Test
    void deveGravarEstadoComoString() {
        Long id = repository.saveAndFlush(transacaoValida()).getId();
        assertThat(consultarColuna("estado", id))
                .as("coluna estado no banco").isEqualTo(EstadoTransacao.values()[0].name());
    }

    @ParameterizedTest
    @EnumSource(TipoTransacao.class)
    void devePersistirQualquerTipoDeTransacao(TipoTransacao tipo) {
        Transacao t = transacaoValida();
        t.setTipo(tipo);
        assertThat(repository.saveAndFlush(t).getId()).as("tipo %s aceito", tipo).isNotNull();
    }

    @ParameterizedTest
    @EnumSource(EstadoTransacao.class)
    void devePersistirQualquerEstadoDeTransacao(EstadoTransacao estado) {
        Transacao t = transacaoValida();
        t.setEstado(estado);
        assertThat(repository.saveAndFlush(t).getId()).as("estado %s aceito", estado).isNotNull();
    }

    // =====================================================================
    // FALHA: campos obrigatórios (nullable = false), um método por campo
    // =====================================================================

    @Test
    void naoDevePersistirSemDescricao() {
        Transacao t = transacaoValida();
        t.setDescricao(null);
        assertThatThrownBy(() -> repository.saveAndFlush(t))
                .as("descrição nula deve ser rejeitada")
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void naoDevePersistirSemValor() {
        Transacao t = transacaoValida();
        t.setValor(null);
        assertThatThrownBy(() -> repository.saveAndFlush(t))
                .as("valor nulo deve ser rejeitado")
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void naoDevePersistirSemCategoria() {
        Transacao t = transacaoValida();
        t.setCategoria(null);
        assertThatThrownBy(() -> repository.saveAndFlush(t))
                .as("categoria nula deve ser rejeitada")
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void naoDevePersistirSemEstado() {
        Transacao t = transacaoValida();
        t.setEstado(null);
        assertThatThrownBy(() -> repository.saveAndFlush(t))
                .as("estado nulo deve ser rejeitado")
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void naoDevePersistirSemTipo() {
        Transacao t = transacaoValida();
        t.setTipo(null);
        assertThatThrownBy(() -> repository.saveAndFlush(t))
                .as("tipo nulo deve ser rejeitado")
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void naoDevePersistirSemDataHora() {
        Transacao t = transacaoValida();
        t.setDataHora(null);
        assertThatThrownBy(() -> repository.saveAndFlush(t))
                .as("data e hora nulas devem ser rejeitadas")
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void naoDevePersistirSemContaFinanceira() {
        Transacao t = transacaoValida();
        t.setContaFinanceira(null);
        assertThatThrownBy(() -> repository.saveAndFlush(t))
                .as("conta financeira nula deve ser rejeitada")
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // =====================================================================
    // DESCRIÇÃO: classes de equivalência e valor limite (length = 254)
    //   válida: 1..254 | inválida: > 254
    // =====================================================================

    @Test
    void devePersistirDescricaoComUmCaractere() { // limite inferior
        Transacao t = transacaoValida();
        t.setDescricao("a");
        assertThat(repository.saveAndFlush(t).getId())
                .as("descrição com 1 caractere aceita").isNotNull();
    }

    @Test
    void devePersistirDescricaoComExatamente254Caracteres() { // limite superior
        Transacao t = transacaoValida();
        t.setDescricao("a".repeat(254));
        assertThat(repository.saveAndFlush(t).getId())
                .as("descrição com 254 caracteres aceita").isNotNull();
    }

    @Test
    void naoDevePersistirDescricaoComMaisDe254Caracteres() { // logo acima do limite
        Transacao t = transacaoValida();
        t.setDescricao("a".repeat(255));
        assertThatThrownBy(() -> repository.saveAndFlush(t))
                .as("descrição com 255 caracteres deve ser rejeitada")
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // =====================================================================
    // VALOR: classes de equivalência e valor limite (precision 19, scale 2)
    //   negativo | zero | positivo | máximo (17 inteiros + 2 decimais) | acima do máximo
    // =====================================================================

    @Test
    void devePersistirValorPositivo() {
        Transacao t = transacaoValida();
        t.setValor(new BigDecimal("0.01")); // fronteira logo acima de zero
        assertThat(repository.saveAndFlush(t).getId())
                .as("valor positivo aceito").isNotNull();
    }

    @Test
    void devePersistirValorZero() {
        Transacao t = transacaoValida();
        t.setValor(BigDecimal.ZERO); // fronteira
        assertThat(repository.saveAndFlush(t).getId())
                .as("valor zero aceito").isNotNull();
    }

    @Test
    void devePersistirValorNegativo() {
        // A entity não tem @Positive, então o banco aceita: o teste documenta isso.
        Transacao t = transacaoValida();
        t.setValor(new BigDecimal("-0.01")); // fronteira logo abaixo de zero
        assertThat(repository.saveAndFlush(t).getId())
                .as("valor negativo aceito (entity sem @Positive)").isNotNull();
    }

    @Test
    void devePersistirValorNoLimiteMaximoDePrecisao() {
        BigDecimal maximo = new BigDecimal("1".repeat(17) + ".99"); // 19 dígitos no total
        Transacao t = transacaoValida();
        t.setValor(maximo);

        Transacao lida = salvarELer(t);

        assertThat(lida.getValor()).as("valor máximo preservado").isEqualByComparingTo(maximo);
    }

    @Test
    void naoDevePersistirValorAcimaDoLimiteDePrecisao() {
        Transacao t = transacaoValida();
        t.setValor(new BigDecimal("1".repeat(18) + ".99")); // 20 dígitos
        assertThatThrownBy(() -> repository.saveAndFlush(t))
                .as("valor com 20 dígitos deve ser rejeitado")
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}