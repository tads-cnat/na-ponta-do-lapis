package com.npl.na_ponta_do_lapis.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.Conquista;
import com.npl.na_ponta_do_lapis.entity.Marcador;
import com.npl.na_ponta_do_lapis.entity.Meta;
import com.npl.na_ponta_do_lapis.entity.TipoCategoria;
import com.npl.na_ponta_do_lapis.entity.Transacao;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.CategoriaConquista;
import com.npl.na_ponta_do_lapis.entity.enums.EstadoTransacao;
import com.npl.na_ponta_do_lapis.entity.enums.Moeda;
import com.npl.na_ponta_do_lapis.entity.enums.Papel;
import com.npl.na_ponta_do_lapis.entity.enums.TipoConta;
import com.npl.na_ponta_do_lapis.entity.enums.TipoMeta;
import com.npl.na_ponta_do_lapis.entity.enums.TipoTransacao;
import com.npl.na_ponta_do_lapis.repository.ContaFinanceiraRepository;
import com.npl.na_ponta_do_lapis.repository.ConquistaRepository;
import com.npl.na_ponta_do_lapis.repository.MarcadorRepository;
import com.npl.na_ponta_do_lapis.repository.MetaRepository;
import com.npl.na_ponta_do_lapis.repository.TipoCategoriaRepository;
import com.npl.na_ponta_do_lapis.repository.TransacaoRepository;
import com.npl.na_ponta_do_lapis.repository.UsuarioRepository;

@Component
@Profile("!test")
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
@Order(1)
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final TipoCategoriaRepository categoriaRepository;
    private final ContaFinanceiraRepository contaRepository;
    private final MarcadorRepository marcadorRepository;
    private final TransacaoRepository transacaoRepository;
    private final MetaRepository metaRepository;
    private final ConquistaRepository conquistaRepository;

    public DatabaseSeeder(UsuarioRepository usuarioRepository,
                          TipoCategoriaRepository categoriaRepository,
                          ContaFinanceiraRepository contaRepository,
                          MarcadorRepository marcadorRepository,
                          TransacaoRepository transacaoRepository,
                          MetaRepository metaRepository,
                          ConquistaRepository conquistaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.contaRepository = contaRepository;
        this.marcadorRepository = marcadorRepository;
        this.transacaoRepository = transacaoRepository;
        this.metaRepository = metaRepository;
        this.conquistaRepository = conquistaRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        popularConquistasPadrao();

        if (usuarioRepository.count() > 0) {
            corrigirContasLegadasSemTipo();
            log.info("[DatabaseSeeder] Banco de dados já possui registros cadastrados. Pulando carga inicial.");
            return;
        }

        log.info("[DatabaseSeeder] Banco vazio detectado. Iniciando população de dados com valores e transações reais de um usuário comum...");

        // 1. Categorias (gerenciadas pelo CategoriaInitializer)
        TipoCategoria catAlimentacao = categoriaRepository.findByNome("Alimentação").orElseThrow();
        TipoCategoria catTransporte = categoriaRepository.findByNome("Transporte").orElseThrow();
        TipoCategoria catSaude = categoriaRepository.findByNome("Saúde").orElseThrow();
        TipoCategoria catEducacao = categoriaRepository.findByNome("Educação").orElseThrow();
        TipoCategoria catLazer = categoriaRepository.findByNome("Lazer").orElseThrow();
        TipoCategoria catOutros = categoriaRepository.findByNome("Outros").orElseThrow();

        // Senha padrão para todos os usuários de teste: 123456
        String senhaPadrao = "$2a$12$1L8X.JQ/ZSm1Id90KjnZPOL3CFE1VjyZSXGEk5YPMAsUJSIohV8Y6";

        // 2. Usuários
        Usuario uLucas = criarUsuario("Lucas Henrique", "lucas@gmail.com", "Lucaasshq", senhaPadrao,
                "https://res.cloudinary.com/doqohj5cv/image/upload/v1778434359/eu_xvyhwk.png", Papel.USUARIO);
        Usuario uBruno = criarUsuario("Bruno", "bruno@gmail.com", "Brunozz", senhaPadrao,
                "https://res.cloudinary.com/doqohj5cv/image/upload/v1778434114/Screenshot_2026-05-10_135527.png_btm58t.jpg", Papel.USUARIO);
        Usuario uEduardo = criarUsuario("Eduardo", "dudu@gmail.com", "Dudurant", senhaPadrao,
                "https://res.cloudinary.com/doqohj5cv/image/upload/v1778434114/Screenshot_2026-05-10_135555_r4ut0s.jpg", Papel.USUARIO);
        Usuario uPedro = criarUsuario("Pedro", "pedro@gmail.com", "Predo", senhaPadrao,
                "https://res.cloudinary.com/doqohj5cv/image/upload/v1778434114/Screenshot_2026-05-10_135624_ppmxzb.jpg", Papel.USUARIO);
        Usuario uAdmin = criarUsuario("ADMIN", "admin@gmail.com", "ademirDaSilva", senhaPadrao,
                "https://res.cloudinary.com/doqohj5cv/image/upload/v1778434114/Screenshot_2026-05-10_135730_gby643.jpg", Papel.USUARIO);

        // 3. Contas e Cartões com Saldos e Limites Coerentes para o Cotidiano
        LocalDate hoje = LocalDate.now();
        int ano = hoje.getYear();
        int mes = hoje.getMonthValue();
        LocalDate mesAnt = hoje.minusMonths(1);
        int anoAnt = mesAnt.getYear();
        int mesAntNum = mesAnt.getMonthValue();

        // Lucas (Conta Inter + Cartão Inter + PicPay)
        ContaFinanceira cInter = criarConta("Banco Inter", new BigDecimal("1642.35"), "#FF7A00", Moeda.BRL, uLucas,
                TipoConta.CONTA_CORRENTE, null, null, null, null, null);
        ContaFinanceira cInterCard = criarConta("Cartão Inter Mastercard", new BigDecimal("685.40"), "#FF7A00", Moeda.BRL, uLucas,
                TipoConta.CARTAO_CREDITO, new BigDecimal("3000.00"), new BigDecimal("2314.60"), new BigDecimal("685.40"), BigDecimal.ZERO, hoje.withDayOfMonth(Math.min(15, hoje.lengthOfMonth())));
        ContaFinanceira cPicPay = criarConta("PicPay", new BigDecimal("85.20"), "#11C76F", Moeda.BRL, uLucas,
                TipoConta.CONTA_CORRENTE, null, null, null, null, null);

        // Bruno (Conta Nubank + Cartão Nubank + Banco do Brasil Reserva)
        ContaFinanceira cNubank = criarConta("Nubank", new BigDecimal("2180.45"), "#820AD1", Moeda.BRL, uBruno,
                TipoConta.CONTA_CORRENTE, null, null, null, null, null);
        ContaFinanceira cNubankCard = criarConta("Cartão Nubank", new BigDecimal("1120.70"), "#820AD1", Moeda.BRL, uBruno,
                TipoConta.CARTAO_CREDITO, new BigDecimal("5000.00"), new BigDecimal("3879.30"), new BigDecimal("1120.70"), BigDecimal.ZERO, hoje.withDayOfMonth(Math.min(12, hoje.lengthOfMonth())));
        ContaFinanceira cBB = criarConta("Banco do Brasil", new BigDecimal("3200.00"), "#003882", Moeda.BRL, uBruno,
                TipoConta.CONTA_CORRENTE, null, null, null, null, null);

        // Eduardo (Conta Itaú + Cartão Itaú)
        ContaFinanceira cItau = criarConta("Itaú Unibanco", new BigDecimal("645.80"), "#EC7000", Moeda.BRL, uEduardo,
                TipoConta.CONTA_CORRENTE, null, null, null, null, null);
        ContaFinanceira cItauCard = criarConta("Cartão Itaú Click", new BigDecimal("385.40"), "#EC7000", Moeda.BRL, uEduardo,
                TipoConta.CARTAO_CREDITO, new BigDecimal("1200.00"), new BigDecimal("814.60"), new BigDecimal("385.40"), BigDecimal.ZERO, hoje.withDayOfMonth(Math.min(10, hoje.lengthOfMonth())));

        // Pedro (Conta Santander + Cartão Santander SX)
        ContaFinanceira cSantander = criarConta("Santander", new BigDecimal("512.30"), "#EA1D2C", Moeda.BRL, uPedro,
                TipoConta.CONTA_CORRENTE, null, null, null, null, null);
        ContaFinanceira cSantanderCard = criarConta("Cartão Santander SX", new BigDecimal("295.80"), "#EA1D2C", Moeda.BRL, uPedro,
                TipoConta.CARTAO_CREDITO, new BigDecimal("1500.00"), new BigDecimal("1204.20"), new BigDecimal("295.80"), BigDecimal.ZERO, hoje.withDayOfMonth(Math.min(18, hoje.lengthOfMonth())));

        // 4. Marcadores
        Marcador mPixLucas = criarMarcador("PIX", "#2196F3", uLucas);
        Marcador mCreditoLucas = criarMarcador("CREDITO", "#9C27B0", uLucas);
        Marcador mDebitoLucas = criarMarcador("DEBITO", "#F44336", uLucas);

        Marcador mPixBruno = criarMarcador("PIX", "#00BCD4", uBruno);
        Marcador mCreditoBruno = criarMarcador("CREDITO", "#3F51B5", uBruno);
        Marcador mDebitoBruno = criarMarcador("DEBITO", "#000000", uBruno);

        Marcador mPixEduardo = criarMarcador("PIX", "#673AB7", uEduardo);
        Marcador mCreditoEduardo = criarMarcador("CREDITO", "#E91E63", uEduardo);
        Marcador mDebitoEduardo = criarMarcador("DEBITO", "#009688", uEduardo);

        Marcador mPixPedro = criarMarcador("PIX", "#3F51B5", uPedro);
        Marcador mCreditoPedro = criarMarcador("CREDITO", "#FF5722", uPedro);
        Marcador mDebitoPedro = criarMarcador("DEBITO", "#9E9E9E", uPedro);

        criarMarcador("PIX", "#212121", uAdmin);
        criarMarcador("CREDITO", "#D32F2F", uAdmin);
        criarMarcador("DEBITO", "#1976D2", uAdmin);

        // 5. Transações Reais e Cotidianas (Mês Atual + Histórico do Mês Anterior)
        List<Transacao> transacoes = new ArrayList<>();

        // ==========================================
        // TRANSAÇÕES: LUCAS
        // ==========================================
        // Mês atual
        transacoes.add(criarTransacao("Salário Mensal CLT", new BigDecimal("3450.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.RECEITA, LocalDateTime.of(ano, mes, 5, 8, 30), cInter, mPixLucas));
        transacoes.add(criarTransacao("Vale Refeição / Alimentação", new BigDecimal("550.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.RECEITA, LocalDateTime.of(ano, mes, 1, 9, 0), cInter, mPixLucas));
        transacoes.add(criarTransacao("PIX Rachadinha Churrasco", new BigDecimal("38.50"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.RECEITA, LocalDateTime.of(ano, mes, 7, 19, 15), cInter, mPixLucas));
        transacoes.add(criarTransacao("Rendimento da Conta (CDI)", new BigDecimal("6.85"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.RECEITA, LocalDateTime.of(ano, mes, 8, 7, 0), cInter, mPixLucas));

        transacoes.add(criarTransacao("Aluguel Apartamento", new BigDecimal("1150.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 5, 10, 0), cInter, mPixLucas));
        transacoes.add(criarTransacao("Condomínio Edifício Solar", new BigDecimal("265.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 6, 11, 30), cInter, mDebitoLucas));
        transacoes.add(criarTransacao("Conta de Energia (Neoenergia)", new BigDecimal("124.80"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 6, 14, 0), cInter, mDebitoLucas));
        transacoes.add(criarTransacao("Internet Fibra 400MB", new BigDecimal("99.90"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 7, 9, 45), cInter, mDebitoLucas));
        transacoes.add(criarTransacao("Supermercado Compra Quinzenal", new BigDecimal("318.45"), catAlimentacao, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 2, 18, 20), cInter, mDebitoLucas));
        transacoes.add(criarTransacao("Abastecimento Gasolina Comum", new BigDecimal("140.00"), catTransporte, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 4, 10, 15), cInter, mDebitoLucas));
        transacoes.add(criarTransacao("Farmácia Drogasil (Antialérgico)", new BigDecimal("42.90"), catSaude, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 6, 16, 40), cInterCard, mCreditoLucas));
        transacoes.add(criarTransacao("Netflix e Spotify", new BigDecimal("59.80"), catLazer, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 3, 8, 0), cInterCard, mCreditoLucas));
        transacoes.add(criarTransacao("iFood Hambúrguer Artesanal", new BigDecimal("46.50"), catAlimentacao, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 8, 20, 30), cInterCard, mCreditoLucas));
        transacoes.add(criarTransacao("Uber para o Trabalho", new BigDecimal("19.40"), catTransporte, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 4, 8, 15), cInterCard, mCreditoLucas));
        transacoes.add(criarTransacao("Padaria Pão & Companhia", new BigDecimal("16.80"), catAlimentacao, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 7, 7, 45), cInter, mDebitoLucas));
        transacoes.add(criarTransacao("Livro Engenharia de Software", new BigDecimal("89.00"), catEducacao, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 9, 15, 0), cInterCard, mCreditoLucas));
        transacoes.add(criarTransacao("Mensalidade Academia Smart Fit", new BigDecimal("109.90"), catSaude, EstadoTransacao.PENDENTE, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, Math.min(15, hoje.lengthOfMonth()), 10, 0), cInterCard, mCreditoLucas));
        transacoes.add(criarTransacao("Conta de Água", new BigDecimal("58.40"), catOutros, EstadoTransacao.PENDENTE, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, Math.min(18, hoje.lengthOfMonth()), 12, 0), cInter, mDebitoLucas));

        // Mês anterior (para histórico e cálculo do balanço comparativo)
        transacoes.add(criarTransacao("Salário Mensal CLT", new BigDecimal("3450.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.RECEITA, LocalDateTime.of(anoAnt, mesAntNum, 5, 8, 30), cInter, mPixLucas));
        transacoes.add(criarTransacao("Aluguel Apartamento", new BigDecimal("1150.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(anoAnt, mesAntNum, 5, 10, 0), cInter, mPixLucas));
        transacoes.add(criarTransacao("Supermercado Mensal", new BigDecimal("495.20"), catAlimentacao, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(anoAnt, mesAntNum, 6, 17, 30), cInter, mDebitoLucas));
        transacoes.add(criarTransacao("Combustível Posto Ipiranga", new BigDecimal("170.00"), catTransporte, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(anoAnt, mesAntNum, 10, 11, 0), cInter, mDebitoLucas));
        transacoes.add(criarTransacao("Fatura Fechada Cartão Inter", new BigDecimal("620.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(anoAnt, mesAntNum, 15, 14, 0), cInter, mDebitoLucas));

        // Compras Parceladas no Cartão: Lucas
        transacoes.addAll(criarCompraParcelada(
                "Smartphone Samsung Galaxy",
                new BigDecimal("189.90"),
                10, 4,
                catOutros,
                LocalDateTime.of(ano, mes, 10, 14, 0).minusMonths(3),
                cInterCard,
                mCreditoLucas
        ));
        transacoes.addAll(criarCompraParcelada(
                "Tênis de Corrida Nike",
                new BigDecimal("149.99"),
                3, 2,
                catOutros,
                LocalDateTime.of(ano, mes, 8, 16, 30).minusMonths(1),
                cInterCard,
                mCreditoLucas
        ));

        // ==========================================
        // TRANSAÇÕES: BRUNO
        // ==========================================
        // Mês atual
        transacoes.add(criarTransacao("Salário Mensal - Folha de Pagamento", new BigDecimal("4800.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.RECEITA, LocalDateTime.of(ano, mes, 5, 9, 0), cNubank, mPixBruno));
        transacoes.add(criarTransacao("Reembolso Despesa Trabalho", new BigDecimal("160.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.RECEITA, LocalDateTime.of(ano, mes, 3, 15, 30), cNubank, mPixBruno));
        transacoes.add(criarTransacao("Rendimento Caixinha Nubank", new BigDecimal("14.80"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.RECEITA, LocalDateTime.of(ano, mes, 8, 8, 0), cNubank, mPixBruno));

        transacoes.add(criarTransacao("Parcela Financiamento Residencial", new BigDecimal("1380.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 6, 10, 0), cNubank, mPixBruno));
        transacoes.add(criarTransacao("Supermercado Atacadão (Compra do Mês)", new BigDecimal("546.80"), catAlimentacao, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 3, 18, 0), cNubank, mDebitoBruno));
        transacoes.add(criarTransacao("Posto Shell - Gasolina Aditivada", new BigDecimal("160.00"), catTransporte, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 4, 11, 0), cNubank, mDebitoBruno));
        transacoes.add(criarTransacao("Conta de Luz e Energia", new BigDecimal("178.30"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 7, 14, 30), cNubank, mDebitoBruno));
        transacoes.add(criarTransacao("Plano de Saúde Familiar", new BigDecimal("360.00"), catSaude, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 8, 9, 15), cNubankCard, mCreditoBruno));
        transacoes.add(criarTransacao("Droga Raia - Fórmulas e Remédios", new BigDecimal("68.40"), catSaude, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 5, 17, 45), cNubankCard, mCreditoBruno));
        transacoes.add(criarTransacao("Almoço Restaurante Self-Service", new BigDecimal("36.90"), catAlimentacao, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 6, 12, 30), cNubank, mDebitoBruno));
        transacoes.add(criarTransacao("Troca de Óleo e Filtro (Auto Center)", new BigDecimal("180.00"), catTransporte, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 2, 10, 0), cNubankCard, mCreditoBruno));
        transacoes.add(criarTransacao("Assinaturas Digitais (YouTube e Prime)", new BigDecimal("41.80"), catLazer, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 7, 8, 0), cNubankCard, mCreditoBruno));
        transacoes.add(criarTransacao("iFood Pizza de Domingo", new BigDecimal("78.50"), catAlimentacao, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 8, 21, 0), cNubankCard, mCreditoBruno));
        transacoes.add(criarTransacao("Café da Manhã Padaria Imperial", new BigDecimal("15.30"), catAlimentacao, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 9, 7, 30), cNubank, mDebitoBruno));
        transacoes.add(criarTransacao("Material Escolar Papelaria", new BigDecimal("55.00"), catEducacao, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 4, 16, 20), cNubankCard, mCreditoBruno));
        transacoes.add(criarTransacao("Internet Banda Larga Fibra", new BigDecimal("119.90"), catOutros, EstadoTransacao.PENDENTE, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, Math.min(14, hoje.lengthOfMonth()), 10, 0), cNubank, mDebitoBruno));
        transacoes.add(criarTransacao("Seguro do Carro (Parcela 4/10)", new BigDecimal("175.00"), catTransporte, EstadoTransacao.PENDENTE, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, Math.min(16, hoje.lengthOfMonth()), 11, 0), cNubankCard, mCreditoBruno));

        // Mês anterior
        transacoes.add(criarTransacao("Salário Mensal CLT", new BigDecimal("4800.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.RECEITA, LocalDateTime.of(anoAnt, mesAntNum, 5, 9, 0), cNubank, mPixBruno));
        transacoes.add(criarTransacao("Parcela Financiamento Residencial", new BigDecimal("1380.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(anoAnt, mesAntNum, 6, 10, 0), cNubank, mPixBruno));
        transacoes.add(criarTransacao("Supermercado Atacadão", new BigDecimal("612.40"), catAlimentacao, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(anoAnt, mesAntNum, 4, 18, 30), cNubank, mDebitoBruno));
        transacoes.add(criarTransacao("Combustível Shell", new BigDecimal("190.00"), catTransporte, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(anoAnt, mesAntNum, 8, 11, 0), cNubank, mDebitoBruno));
        transacoes.add(criarTransacao("Fatura Cartão Nubank", new BigDecimal("980.50"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(anoAnt, mesAntNum, 12, 14, 0), cNubank, mDebitoBruno));

        // Compras Parceladas no Cartão: Bruno
        transacoes.addAll(criarCompraParcelada(
                "Geladeira Frost Free Brastemp",
                new BigDecimal("289.90"),
                10, 6,
                catOutros,
                LocalDateTime.of(ano, mes, 7, 11, 0).minusMonths(5),
                cNubankCard,
                mCreditoBruno
        ));
        transacoes.addAll(criarCompraParcelada(
                "Seguro Automotivo Anual",
                new BigDecimal("175.00"),
                6, 3,
                catTransporte,
                LocalDateTime.of(ano, mes, 12, 10, 0).minusMonths(2),
                cNubankCard,
                mCreditoBruno
        ));
        transacoes.addAll(criarCompraParcelada(
                "Cadeira Ergonômica Home Office",
                new BigDecimal("199.50"),
                4, 2,
                catEducacao,
                LocalDateTime.of(ano, mes, 15, 15, 45).minusMonths(1),
                cNubankCard,
                mCreditoBruno
        ));

        // ==========================================
        // TRANSAÇÕES: EDUARDO
        // ==========================================
        // Mês atual
        transacoes.add(criarTransacao("Bolsa Estágio Desenvolvimento", new BigDecimal("1500.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.RECEITA, LocalDateTime.of(ano, mes, 5, 10, 0), cItau, mPixEduardo));
        transacoes.add(criarTransacao("Ajuda de Custo Transporte", new BigDecimal("250.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.RECEITA, LocalDateTime.of(ano, mes, 1, 9, 30), cItau, mPixEduardo));
        transacoes.add(criarTransacao("Aluguel Quarto / República", new BigDecimal("600.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 5, 11, 0), cItau, mPixEduardo));
        transacoes.add(criarTransacao("Supermercado do Bairro (Básico)", new BigDecimal("215.60"), catAlimentacao, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 6, 17, 0), cItau, mDebitoEduardo));
        transacoes.add(criarTransacao("Recarga Bilhete de Ônibus", new BigDecimal("110.00"), catTransporte, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 2, 8, 0), cItau, mDebitoEduardo));
        transacoes.add(criarTransacao("Lanche Cantina da Faculdade", new BigDecimal("22.50"), catAlimentacao, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 4, 21, 15), cItau, mDebitoEduardo));
        transacoes.add(criarTransacao("Xérox e Apostilas Universitárias", new BigDecimal("28.40"), catEducacao, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 7, 14, 0), cItauCard, mCreditoEduardo));
        transacoes.add(criarTransacao("Spotify Plano Universitário", new BigDecimal("11.90"), catLazer, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 3, 9, 0), cItauCard, mCreditoEduardo));
        transacoes.add(criarTransacao("Cinema Quarta-feira (Meia Entrada)", new BigDecimal("24.00"), catLazer, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 8, 19, 30), cItauCard, mCreditoEduardo));
        transacoes.add(criarTransacao("Farmácia Pague Menos (Vitamina C)", new BigDecimal("21.50"), catSaude, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 6, 15, 30), cItauCard, mCreditoEduardo));
        transacoes.add(criarTransacao("Açaí com os Amigos", new BigDecimal("18.00"), catAlimentacao, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 9, 17, 45), cItau, mDebitoEduardo));
        transacoes.add(criarTransacao("Rateio Internet República", new BigDecimal("35.00"), catOutros, EstadoTransacao.PENDENTE, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, Math.min(12, hoje.lengthOfMonth()), 10, 0), cItau, mPixEduardo));

        // Mês anterior
        transacoes.add(criarTransacao("Bolsa Estágio", new BigDecimal("1500.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.RECEITA, LocalDateTime.of(anoAnt, mesAntNum, 5, 10, 0), cItau, mPixEduardo));
        transacoes.add(criarTransacao("Aluguel Quarto", new BigDecimal("600.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(anoAnt, mesAntNum, 5, 11, 0), cItau, mPixEduardo));
        transacoes.add(criarTransacao("Mercado Básico", new BigDecimal("230.00"), catAlimentacao, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(anoAnt, mesAntNum, 7, 18, 0), cItau, mDebitoEduardo));

        // Compras Parceladas no Cartão: Eduardo
        transacoes.addAll(criarCompraParcelada(
                "Mochila Antifurto e Fone Bluetooth",
                new BigDecimal("65.00"),
                4, 2,
                catEducacao,
                LocalDateTime.of(ano, mes, 10, 16, 0).minusMonths(1),
                cItauCard,
                mCreditoEduardo
        ));

        // ==========================================
        // TRANSAÇÕES: PEDRO
        // ==========================================
        // Mês atual
        transacoes.add(criarTransacao("Salário Mensal CLT", new BigDecimal("2100.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.RECEITA, LocalDateTime.of(ano, mes, 5, 8, 0), cSantander, mPixPedro));
        transacoes.add(criarTransacao("Ajuda nas Despesas da Casa", new BigDecimal("450.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 5, 11, 30), cSantander, mPixPedro));
        transacoes.add(criarTransacao("Supermercado Mensal", new BigDecimal("325.40"), catAlimentacao, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 6, 18, 15), cSantander, mDebitoPedro));
        transacoes.add(criarTransacao("Recarga Transporte Público", new BigDecimal("130.00"), catTransporte, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 2, 7, 30), cSantander, mDebitoPedro));
        transacoes.add(criarTransacao("Plano Celular Controle", new BigDecimal("39.99"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 4, 10, 0), cSantander, mDebitoPedro));
        transacoes.add(criarTransacao("Barbearia / Corte de Cabelo", new BigDecimal("35.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 7, 16, 0), cSantander, mPixPedro));
        transacoes.add(criarTransacao("Mensalidade Academia do Bairro", new BigDecimal("79.90"), catSaude, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 3, 9, 0), cSantanderCard, mCreditoPedro));
        transacoes.add(criarTransacao("Lanche de Sábado à Noite", new BigDecimal("34.50"), catAlimentacao, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 8, 21, 30), cSantanderCard, mCreditoPedro));
        transacoes.add(criarTransacao("Uber para Casa na Chuva", new BigDecimal("18.20"), catTransporte, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 9, 22, 10), cSantanderCard, mCreditoPedro));
        transacoes.add(criarTransacao("Remédio Farmácia", new BigDecimal("26.80"), catSaude, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, 6, 14, 20), cSantander, mDebitoPedro));
        transacoes.add(criarTransacao("Conta de Água", new BigDecimal("48.00"), catOutros, EstadoTransacao.PENDENTE, TipoTransacao.DESPESA, LocalDateTime.of(ano, mes, Math.min(15, hoje.lengthOfMonth()), 10, 0), cSantander, mDebitoPedro));

        // Mês anterior
        transacoes.add(criarTransacao("Salário Mensal CLT", new BigDecimal("2100.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.RECEITA, LocalDateTime.of(anoAnt, mesAntNum, 5, 8, 0), cSantander, mPixPedro));
        transacoes.add(criarTransacao("Supermercado", new BigDecimal("340.00"), catAlimentacao, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(anoAnt, mesAntNum, 6, 18, 0), cSantander, mDebitoPedro));
        transacoes.add(criarTransacao("Ajuda em Casa", new BigDecimal("450.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(anoAnt, mesAntNum, 5, 11, 0), cSantander, mPixPedro));

        // Compras Parceladas no Cartão: Pedro
        transacoes.addAll(criarCompraParcelada(
                "Jaqueta e Roupas de Inverno",
                new BigDecimal("79.90"),
                3, 2,
                catOutros,
                LocalDateTime.of(ano, mes, 8, 19, 0).minusMonths(1),
                cSantanderCard,
                mCreditoPedro
        ));

        transacaoRepository.saveAll(transacoes);

        // 6. Metas Realistas e Acessíveis com Ilustrações Temáticas
        List<Meta> metas = new ArrayList<>();
        // Lucas
        metas.add(criarMeta("Reserva de Emergência", "Guardar dinheiro para imprevistos (3 a 6 meses de contas)", new BigDecimal("4000.00"), new BigDecimal("1500.00"), "reserva", LocalDate.of(ano, 1, 1), LocalDate.of(ano, 12, 31), TipoMeta.POUPANCA, uLucas));
        metas.add(criarMeta("Troca de Notebook", "Economizar para novo computador de trabalho", new BigDecimal("3200.00"), new BigDecimal("1800.00"), "eletronicos", LocalDate.of(ano, 2, 1), LocalDate.of(ano, 11, 30), TipoMeta.POUPANCA, uLucas));
        metas.add(criarMeta("Limite Supermercado", "Controle mensal com compras de mercado", new BigDecimal("600.00"), new BigDecimal("318.45"), "compras_mercado", LocalDate.of(ano, mes, 1), LocalDate.of(ano, mes, hoje.lengthOfMonth()), TipoMeta.GASTO, uLucas));
        metas.add(criarMeta("Viagem de Férias", "Viagem em família no final do ano", new BigDecimal("3500.00"), new BigDecimal("2100.00"), "viagem", LocalDate.of(ano, 2, 15), LocalDate.of(ano, 12, 20), TipoMeta.POUPANCA, uLucas));
        metas.add(criarMeta("Quitar Empréstimo", "Amortização antecipada da renegociação", new BigDecimal("2500.00"), new BigDecimal("1800.00"), "divida_quitacao", LocalDate.of(ano, 1, 10), LocalDate.of(ano, 10, 10), TipoMeta.DIVIDA, uLucas));

        // Bruno
        metas.add(criarMeta("Fundo de Reserva", "Guardar em caixinha com liquidez diária", new BigDecimal("6000.00"), new BigDecimal("3200.00"), "cofrinho", LocalDate.of(ano, 1, 1), LocalDate.of(ano, 12, 31), TipoMeta.POUPANCA, uBruno));
        metas.add(criarMeta("Revisão e Seguro do Carro", "Poupar para revisão preventiva anual", new BigDecimal("2000.00"), new BigDecimal("1250.00"), "carro", LocalDate.of(ano, 3, 1), LocalDate.of(ano, 10, 31), TipoMeta.POUPANCA, uBruno));
        metas.add(criarMeta("Limite Alimentação e Delivery", "Controle mensal de almoço fora e iFood", new BigDecimal("850.00"), new BigDecimal("583.70"), "compras_mercado", LocalDate.of(ano, mes, 1), LocalDate.of(ano, mes, hoje.lengthOfMonth()), TipoMeta.GASTO, uBruno));
        metas.add(criarMeta("Reforma da Casa", "Troca de pisos e pintura da sala", new BigDecimal("5000.00"), new BigDecimal("2200.00"), "casa", LocalDate.of(ano, 4, 1), LocalDate.of(ano, 11, 30), TipoMeta.POUPANCA, uBruno));

        // Eduardo
        metas.add(criarMeta("Computador para Estudo", "Equipamento para faculdade e cursos", new BigDecimal("2000.00"), new BigDecimal("750.00"), "eletronicos", LocalDate.of(ano, 2, 15), LocalDate.of(ano, 12, 15), TipoMeta.POUPANCA, uEduardo));
        metas.add(criarMeta("Limite Lazer e Saídas", "Controle de despesas em finais de semana", new BigDecimal("180.00"), new BigDecimal("75.90"), "lazer", LocalDate.of(ano, mes, 1), LocalDate.of(ano, mes, hoje.lengthOfMonth()), TipoMeta.GASTO, uEduardo));
        metas.add(criarMeta("Investimento Renda Fixa", "Aportes mensais no Tesouro Direto", new BigDecimal("3000.00"), new BigDecimal("1400.00"), "investimento", LocalDate.of(ano, 1, 1), LocalDate.of(ano, 12, 31), TipoMeta.POUPANCA, uEduardo));

        // Pedro
        metas.add(criarMeta("Curso Técnico / Certificação", "Investir em capacitação profissional", new BigDecimal("1000.00"), new BigDecimal("450.00"), "educacao", LocalDate.of(ano, 3, 1), LocalDate.of(ano, 11, 30), TipoMeta.POUPANCA, uPedro));
        metas.add(criarMeta("Limite Mensal de Lazer", "Manter gastos recreativos sob controle", new BigDecimal("150.00"), new BigDecimal("87.50"), "lazer", LocalDate.of(ano, mes, 1), LocalDate.of(ano, mes, hoje.lengthOfMonth()), TipoMeta.GASTO, uPedro));
        metas.add(criarMeta("Plano de Saúde e Cuidados", "Reserva para exames e consultas", new BigDecimal("800.00"), new BigDecimal("500.00"), "saude", LocalDate.of(ano, 2, 1), LocalDate.of(ano, 12, 31), TipoMeta.POUPANCA, uPedro));

        metaRepository.saveAll(metas);

        log.info("[DatabaseSeeder] Carga inicial de dados realistas finalizada com sucesso!");
    }

    private Usuario criarUsuario(String nome, String email, String username, String senha, String foto, Papel papel) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email);
        u.setUsername(username);
        u.setSenha(senha);
        u.setFotoPerfil(foto);
        u.setPapel(papel);
        return usuarioRepository.save(u);
    }

    private ContaFinanceira criarConta(String nome, BigDecimal saldo, String cor, Moeda moeda, Usuario usuario) {
        return criarConta(nome, saldo, cor, moeda, usuario, TipoConta.CONTA_CORRENTE, null, null, null, null, null);
    }

    private ContaFinanceira criarConta(String nome, BigDecimal saldo, String cor, Moeda moeda, Usuario usuario,
                                       TipoConta tipoConta, BigDecimal limiteTotal, BigDecimal limiteDisponivel,
                                       BigDecimal valorFaturaAberta, BigDecimal valorFaturaFechada, LocalDate dataVencimentoFatura) {
        ContaFinanceira conta = new ContaFinanceira();
        conta.setNome(nome);
        conta.setSaldo(saldo);
        conta.setCor(cor);
        conta.setMoeda(moeda);
        conta.setUsuario(usuario);
        conta.setTipoConta(tipoConta != null ? tipoConta : TipoConta.CONTA_CORRENTE);
        conta.setLimiteTotal(limiteTotal);
        conta.setLimiteDisponivel(limiteDisponivel);
        conta.setValorFaturaAberta(valorFaturaAberta);
        conta.setValorFaturaFechada(valorFaturaFechada);
        conta.setDataVencimentoFatura(dataVencimentoFatura);
        return contaRepository.save(conta);
    }

    private void corrigirContasLegadasSemTipo() {
        List<ContaFinanceira> contas = contaRepository.findAll();
        boolean alterou = false;
        for (ContaFinanceira c : contas) {
            if (c.getTipoConta() == null) {
                c.setTipoConta(TipoConta.CONTA_CORRENTE);
                alterou = true;
            }
        }
        if (alterou) {
            contaRepository.saveAll(contas);
            log.info("[DatabaseSeeder] Contas legadas sem 'tipoConta' foram corrigidas para CONTA_CORRENTE.");
        }
    }

    private Marcador criarMarcador(String nome, String cor, Usuario usuario) {
        Marcador m = new Marcador();
        m.setNome(nome);
        m.setCor(cor);
        m.setUsuario(usuario);
        return marcadorRepository.save(m);
    }

    private Transacao criarTransacao(String descricao, BigDecimal valor, TipoCategoria categoria,
                                     EstadoTransacao estado, TipoTransacao tipo, LocalDateTime dataHora,
                                     ContaFinanceira conta, Marcador marcador) {
        Transacao t = new Transacao();
        t.setDescricao(descricao);
        t.setValor(valor);
        t.setCategoria(categoria);
        t.setEstado(estado);
        t.setTipo(tipo);
        t.setDataHora(dataHora);
        t.setContaFinanceira(conta);
        t.setMarcador(marcador);
        return t;
    }

    private List<Transacao> criarCompraParcelada(
            String descricao,
            BigDecimal valorParcela,
            int totalParcelas,
            int parcelaAtual,
            TipoCategoria categoria,
            LocalDateTime dataPrimeiraParcela,
            ContaFinanceira conta,
            Marcador marcador) {

        List<Transacao> parcelas = new ArrayList<>();
        String grupoId = UUID.randomUUID().toString();
        BigDecimal valorTotal = valorParcela.multiply(BigDecimal.valueOf(totalParcelas));

        for (int i = 1; i <= totalParcelas; i++) {
            Transacao p = new Transacao();
            p.setDescricao(descricao);
            p.setValor(valorParcela);
            p.setCategoria(categoria);
            p.setTipo(TipoTransacao.DESPESA);
            p.setContaFinanceira(conta);
            p.setMarcador(marcador);
            p.setGrupoParcelamento(grupoId);
            p.setNumeroParcela(i);
            p.setTotalParcelas(totalParcelas);
            p.setValorTotalCompra(valorTotal);

            LocalDateTime dataParcela = dataPrimeiraParcela.plusMonths(i - 1);
            p.setDataHora(dataParcela);

            if (i <= parcelaAtual) {
                p.setEstado(EstadoTransacao.REALIZADA);
            } else {
                p.setEstado(EstadoTransacao.PENDENTE);
            }

            parcelas.add(p);
        }

        return parcelas;
    }

    private Meta criarMeta(String nome, String descricao, BigDecimal valor, BigDecimal valorAtual,
                           String fotoUrl, LocalDate dataInicio, LocalDate dataLimite,
                           TipoMeta tipoMeta, Usuario usuario) {
        Meta m = new Meta();
        m.setNome(nome);
        m.setDescricao(descricao);
        m.setValor(valor);
        m.setValorAtual(valorAtual);
        m.setFotoUrl(fotoUrl);
        m.setDataInicio(dataInicio);
        m.setDataLimite(dataLimite);
        m.setTipoMeta(tipoMeta);
        m.setUsuario(usuario);
        m.setStatus(com.npl.na_ponta_do_lapis.entity.enums.StatusMeta.EM_ANDAMENTO);
        if (usuario.getFamilia() != null && ("Reserva de Emergência".equals(nome) || "Fundo de Reserva".equals(nome))) {
            m.setFamilia(usuario.getFamilia());
        }
        return m;
    }

    private void popularConquistasPadrao() {
        if (conquistaRepository.count() == 0) {
            log.info("[DatabaseSeeder] Inicializando catálogo de conquistas financeiras...");
            List<Conquista> conquistas = List.of(
                    new Conquista("PRIMEIRA_META", "Primeiro Passo", "Cadastrou a sua primeira meta financeira no sistema.", "pi-flag", 50, CategoriaConquista.METAS),
                    new Conquista("PRIMEIRO_APORTE", "Cofrinho Aberto", "Fez o seu primeiro aporte financeiro em uma meta.", "pi-wallet", 50, CategoriaConquista.ECONOMIA),
                    new Conquista("POUPADOR_BRONZE", "Poupador Bronze", "Realizou 5 aportes e manteve o ritmo das suas metas.", "pi-star", 100, CategoriaConquista.ECONOMIA),
                    new Conquista("META_BATIDA_1", "Alvo Conquistado", "Concluiu com sucesso a sua primeira meta financeira!", "pi-check-circle", 150, CategoriaConquista.METAS),
                    new Conquista("MESTRE_DAS_METAS", "Mestre dos Objetivos", "Concluiu 3 ou mais metas financeiras com êxito.", "pi-crown", 300, CategoriaConquista.METAS),
                    new Conquista("ORCAMENTO_BLINDADO", "Orçamento Blindado", "Criou um teto de gastos para controlar despesas.", "pi-shield", 75, CategoriaConquista.DISCIPLINA),
                    new Conquista("LIVRE_DE_DIVIDAS", "Adeus Dívida", "Criou uma meta para quitar dívidas e pendências financeiras.", "pi-bolt", 75, CategoriaConquista.DISCIPLINA),
                    new Conquista("FAMILIA_UNIDA", "Finanças em Família", "Criou ou compartilhou uma meta com seu grupo familiar.", "pi-users", 100, CategoriaConquista.FAMILIA)
            );
            conquistaRepository.saveAll(conquistas);
            log.info("[DatabaseSeeder] {} conquistas cadastradas no catálogo.", conquistas.size());
        }
    }

}
