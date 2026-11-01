package com.npl.na_ponta_do_lapis.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.TipoCategoria;
import com.npl.na_ponta_do_lapis.entity.Transacao;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.EstadoTransacao;
import com.npl.na_ponta_do_lapis.entity.enums.TipoTransacao;
import com.npl.na_ponta_do_lapis.repository.TransacaoRepository;
import com.npl.na_ponta_do_lapis.web.dto.TransacaoRequestDTO;

class TransacaoServiceTest {

    private static final String EMAIL = "usuario@teste.com";

    private TransacaoRepository transacaoRepository;
    private TipoCategoraService tipoCategoriaService;
    private ContaFinanceiraService contaFinanceiraService;
    private TransacaoService service;

    private ContaFinanceira conta;

    @BeforeEach
    void configurar() {
        transacaoRepository = mock(TransacaoRepository.class);
        tipoCategoriaService = mock(TipoCategoraService.class);
        contaFinanceiraService = mock(ContaFinanceiraService.class);
        service = new TransacaoService(
                transacaoRepository,
                tipoCategoriaService,
                contaFinanceiraService,
                mock(UsuarioService.class),
                mock(MarcadorService.class)
        );

        Usuario usuario = new Usuario();
        usuario.setEmail(EMAIL);

        conta = new ContaFinanceira();
        conta.setSaldo(new BigDecimal("1000.00"));
        conta.setUsuario(usuario);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(EMAIL, null, List.of()));

        when(contaFinanceiraService.buscarContaPorIdObject(anyLong())).thenReturn(conta);

        TipoCategoria categoria = new TipoCategoria();
        categoria.setNome("Outros");
        when(tipoCategoriaService.buscarPorId(anyLong())).thenReturn(categoria);

        when(transacaoRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void despesaRealizadaReduzSaldo() {
        Transacao transacao = transacao(TipoTransacao.DESPESA, EstadoTransacao.REALIZADA, "100.00");

        ReflectionTestUtils.invokeMethod(service, "aplicarImpactoSaldo", transacao);

        assertThat(conta.getSaldo()).isEqualByComparingTo("900.00");
    }

    @Test
    void despesaPendenteNaoAlteraSaldo() {
        Transacao transacao = transacao(TipoTransacao.DESPESA, EstadoTransacao.PENDENTE, "100.00");

        ReflectionTestUtils.invokeMethod(service, "aplicarImpactoSaldo", transacao);

        assertThat(conta.getSaldo()).isEqualByComparingTo("1000.00");
    }

    @Test
    void receitaRealizadaAumentaSaldo() {
        Transacao transacao = transacao(TipoTransacao.RECEITA, EstadoTransacao.REALIZADA, "250.00");

        ReflectionTestUtils.invokeMethod(service, "aplicarImpactoSaldo", transacao);

        assertThat(conta.getSaldo()).isEqualByComparingTo("1250.00");
    }

    @Test
    void estornoDesfazImpactoDeDespesaRealizada() {
        Transacao transacao = transacao(TipoTransacao.DESPESA, EstadoTransacao.REALIZADA, "100.00");
        ReflectionTestUtils.invokeMethod(service, "aplicarImpactoSaldo", transacao);

        ReflectionTestUtils.invokeMethod(service, "estornarImpactoSaldo", transacao);

        assertThat(conta.getSaldo()).isEqualByComparingTo("1000.00");
    }

    @Test
    void estornoDeTransacaoPendenteNaoAlteraSaldo() {
        Transacao transacao = transacao(TipoTransacao.DESPESA, EstadoTransacao.PENDENTE, "100.00");

        ReflectionTestUtils.invokeMethod(service, "estornarImpactoSaldo", transacao);

        assertThat(conta.getSaldo()).isEqualByComparingTo("1000.00");
    }

    @Test
    void compraParceladaComDatasFuturasCriaParcelasPendentesSemAlterarSaldo() throws Exception {
        TransacaoRequestDTO dto = new TransacaoRequestDTO(
                "Notebook",
                new BigDecimal("3000.00"),
                TipoTransacao.DESPESA,
                null,
                1L,
                1L,
                null,
                LocalDate.now().plusMonths(1).atStartOfDay(),
                true,
                3
        );

        List<Transacao> parcelas = service.criarTransacao(dto);

        assertThat(parcelas).hasSize(3);
        assertThat(parcelas).allMatch(p -> p.getEstado() == EstadoTransacao.PENDENTE);
        assertThat(parcelas).allMatch(p -> p.getGrupoParcelamento() != null);
        assertThat(parcelas.stream().map(Transacao::getNumeroParcela))
                .containsExactly(1, 2, 3);
        assertThat(parcelas.stream().map(Transacao::getValor).reduce(BigDecimal.ZERO, BigDecimal::add))
                .isEqualByComparingTo("3000.00");
        assertThat(conta.getSaldo()).isEqualByComparingTo("1000.00");
    }

    @Test
    void compraParceladaComDatasPassadasAlteraSaldoSomenteDasRealizadas() throws Exception {
        TransacaoRequestDTO dto = new TransacaoRequestDTO(
                "Geladeira",
                new BigDecimal("600.00"),
                TipoTransacao.DESPESA,
                null,
                1L,
                1L,
                null,
                LocalDate.of(2020, 1, 1).atStartOfDay(),
                true,
                2
        );

        List<Transacao> parcelas = service.criarTransacao(dto);

        assertThat(parcelas).allMatch(p -> p.getEstado() == EstadoTransacao.REALIZADA);
        assertThat(conta.getSaldo()).isEqualByComparingTo("400.00");
    }

    @Test
    void editarTransacaoAvulsaComoParceladaGeraParcelasFuturasPendentes() throws Exception {
        Transacao existente = transacao(TipoTransacao.DESPESA, EstadoTransacao.REALIZADA, "100.00");
        existente.setDescricao("Tenis");
        existente.setDataHora(LocalDateTime.of(2026, 1, 10, 0, 0));
        when(transacaoRepository.findById(1L)).thenReturn(java.util.Optional.of(existente));
        when(transacaoRepository.save(any(Transacao.class))).thenAnswer(i -> i.getArgument(0));
        when(transacaoRepository.findByGrupoParcelamentoOrderByNumeroParcelaAsc(anyString()))
                .thenReturn(List.of(existente));

        TransacaoRequestDTO dto = new TransacaoRequestDTO("Tenis", new BigDecimal("100.00"),
                TipoTransacao.DESPESA, EstadoTransacao.REALIZADA, 1L, 1L, null,
                LocalDateTime.of(2026, 1, 10, 0, 0), true, 4, 2);

        service.atualizarTransacao(1L, dto);

        assertThat(existente.getGrupoParcelamento()).isNotNull();
        assertThat(existente.getNumeroParcela()).isEqualTo(2);
        assertThat(existente.getTotalParcelas()).isEqualTo(4);
        org.mockito.ArgumentCaptor<List<Transacao>> captor = org.mockito.ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(transacaoRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(2);
        assertThat(captor.getValue()).allMatch(p -> p.getEstado() == EstadoTransacao.PENDENTE);
    }

    @Test
    void conversaoComParcelasAnterioresCriaRealizadasSemAlterarSaldo() throws Exception {
        Transacao existente = transacao(TipoTransacao.DESPESA, EstadoTransacao.PENDENTE, "100.00");
        existente.setDescricao("Tenis");
        existente.setDataHora(LocalDateTime.of(2026, 3, 10, 0, 0));
        when(transacaoRepository.findById(1L)).thenReturn(java.util.Optional.of(existente));
        when(transacaoRepository.save(any(Transacao.class))).thenAnswer(i -> i.getArgument(0));
        when(transacaoRepository.findByGrupoParcelamentoOrderByNumeroParcelaAsc(anyString()))
                .thenReturn(List.of(existente));

        TransacaoRequestDTO dto = new TransacaoRequestDTO("Tenis", new BigDecimal("100.00"),
                TipoTransacao.DESPESA, EstadoTransacao.PENDENTE, 1L, 1L, null,
                LocalDateTime.of(2026, 3, 10, 0, 0), true, 3, 3, new BigDecimal("299.99"), true);

        service.atualizarTransacao(1L, dto);

        assertThat(existente.getValorTotalCompra()).isEqualByComparingTo("299.99");
        org.mockito.ArgumentCaptor<List<Transacao>> captor = org.mockito.ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(transacaoRepository).saveAll(captor.capture());
        List<Transacao> anteriores = captor.getValue();
        assertThat(anteriores).hasSize(2);
        assertThat(anteriores).allMatch(p -> p.getEstado() == EstadoTransacao.REALIZADA);
        assertThat(anteriores.get(0).getDataHora()).isEqualTo(LocalDateTime.of(2026, 1, 10, 0, 0));
        assertThat(conta.getSaldo()).isEqualByComparingTo("1000.00");
    }

    private Transacao transacao(TipoTransacao tipo, EstadoTransacao estado, String valor) {
        Transacao transacao = new Transacao();
        transacao.setTipo(tipo);
        transacao.setEstado(estado);
        transacao.setValor(new BigDecimal(valor));
        transacao.setContaFinanceira(conta);
        return transacao;
    }
}
