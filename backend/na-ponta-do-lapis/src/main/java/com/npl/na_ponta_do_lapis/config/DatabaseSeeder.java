package com.npl.na_ponta_do_lapis.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.Marcador;
import com.npl.na_ponta_do_lapis.entity.Meta;
import com.npl.na_ponta_do_lapis.entity.TipoCategoria;
import com.npl.na_ponta_do_lapis.entity.Transacao;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.EstadoTransacao;
import com.npl.na_ponta_do_lapis.entity.enums.Moeda;
import com.npl.na_ponta_do_lapis.entity.enums.Papel;
import com.npl.na_ponta_do_lapis.entity.enums.TipoConta;
import com.npl.na_ponta_do_lapis.entity.enums.TipoMeta;
import com.npl.na_ponta_do_lapis.entity.enums.TipoTransacao;
import com.npl.na_ponta_do_lapis.repository.ContaFinanceiraRepository;
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

    public DatabaseSeeder(UsuarioRepository usuarioRepository,
                          TipoCategoriaRepository categoriaRepository,
                          ContaFinanceiraRepository contaRepository,
                          MarcadorRepository marcadorRepository,
                          TransacaoRepository transacaoRepository,
                          MetaRepository metaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.contaRepository = contaRepository;
        this.marcadorRepository = marcadorRepository;
        this.transacaoRepository = transacaoRepository;
        this.metaRepository = metaRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            corrigirContasLegadasSemTipo();
            log.info("[DatabaseSeeder] Banco de dados já possui registros cadastrados. Pulando carga inicial.");
            return;
        }

        log.info("[DatabaseSeeder] Banco vazio detectado. Iniciando população de dados de desenvolvimento...");

        // 1. Categorias (já inicializadas pelo CategoriaInitializer)
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

        // 3. Contas Financeiras e Cartões de Crédito
        // Cores institucionais mapeadas para compatibilidade direta com a biblioteca de ícones bancários:
        // Inter: #FF7A00, Nubank: #820AD1, Itaú: #EC7000, Santander: #EA1D2C, BB: #003882, PicPay: #11C76F

        // Lucas (Conta Inter + Cartão Inter Black + PicPay)
        ContaFinanceira cInter = criarConta("Banco Inter", new BigDecimal("4250.80"), "#FF7A00", Moeda.BRL, uLucas,
                TipoConta.CONTA_CORRENTE, null, null, null, null, null);
        ContaFinanceira cInterCard = criarConta("Cartão Inter Mastercard", new BigDecimal("1250.90"), "#FF7A00", Moeda.BRL, uLucas,
                TipoConta.CARTAO_CREDITO, new BigDecimal("6000.00"), new BigDecimal("4749.10"), new BigDecimal("1250.90"), BigDecimal.ZERO, LocalDate.now().plusDays(10));
        ContaFinanceira cPicPay = criarConta("PicPay", new BigDecimal("185.00"), "#11C76F", Moeda.BRL, uLucas,
                TipoConta.CONTA_CORRENTE, null, null, null, null, null);

        // Bruno (Conta Nubank + Cartão Ultravioleta + Banco do Brasil)
        ContaFinanceira cNubank = criarConta("Nubank", new BigDecimal("5820.40"), "#820AD1", Moeda.BRL, uBruno,
                TipoConta.CONTA_CORRENTE, null, null, null, null, null);
        ContaFinanceira cNubankCard = criarConta("Cartão Nubank Ultravioleta", new BigDecimal("2450.00"), "#820AD1", Moeda.BRL, uBruno,
                TipoConta.CARTAO_CREDITO, new BigDecimal("15000.00"), new BigDecimal("12550.00"), new BigDecimal("2450.00"), BigDecimal.ZERO, LocalDate.now().plusDays(15));
        ContaFinanceira cBB = criarConta("Banco do Brasil", new BigDecimal("12500.00"), "#003882", Moeda.BRL, uBruno,
                TipoConta.CONTA_CORRENTE, null, null, null, null, null);

        // Eduardo (Conta Itaú + Cartão Itaú Personnalité)
        ContaFinanceira cItau = criarConta("Itaú Unibanco", new BigDecimal("3150.00"), "#EC7000", Moeda.BRL, uEduardo,
                TipoConta.CONTA_CORRENTE, null, null, null, null, null);
        ContaFinanceira cItauCard = criarConta("Cartão Itaú Personnalité", new BigDecimal("890.50"), "#EC7000", Moeda.BRL, uEduardo,
                TipoConta.CARTAO_CREDITO, new BigDecimal("10000.00"), new BigDecimal("9109.50"), new BigDecimal("890.50"), BigDecimal.ZERO, LocalDate.now().plusDays(8));

        // Pedro (Conta Santander + Cartão Santander SX)
        ContaFinanceira cSantander = criarConta("Santander", new BigDecimal("980.00"), "#EA1D2C", Moeda.BRL, uPedro,
                TipoConta.CONTA_CORRENTE, null, null, null, null, null);
        ContaFinanceira cSantanderCard = criarConta("Cartão Santander SX", new BigDecimal("420.00"), "#EA1D2C", Moeda.BRL, uPedro,
                TipoConta.CARTAO_CREDITO, new BigDecimal("3000.00"), new BigDecimal("2580.00"), new BigDecimal("420.00"), BigDecimal.ZERO, LocalDate.now().plusDays(12));

        // 4. Marcadores
        // Lucas
        Marcador mPixLucas = criarMarcador("PIX", "#2196F3", uLucas);
        Marcador mCreditoLucas = criarMarcador("CREDITO", "#9C27B0", uLucas);
        Marcador mDebitoLucas = criarMarcador("DEBITO", "#F44336", uLucas);

        // Bruno
        Marcador mPixBruno = criarMarcador("PIX", "#00BCD4", uBruno);
        Marcador mCreditoBruno = criarMarcador("CREDITO", "#3F51B5", uBruno);
        Marcador mDebitoBruno = criarMarcador("DEBITO", "#000000", uBruno);

        // Eduardo
        Marcador mPixEduardo = criarMarcador("PIX", "#673AB7", uEduardo);
        Marcador mCreditoEduardo = criarMarcador("CREDITO", "#E91E63", uEduardo);
        Marcador mDebitoEduardo = criarMarcador("DEBITO", "#009688", uEduardo);

        // Pedro
        Marcador mPixPedro = criarMarcador("PIX", "#3F51B5", uPedro);
        Marcador mCreditoPedro = criarMarcador("CREDITO", "#FF5722", uPedro);
        Marcador mDebitoPedro = criarMarcador("DEBITO", "#9E9E9E", uPedro);

        // ADMIN
        criarMarcador("PIX", "#212121", uAdmin);
        criarMarcador("CREDITO", "#D32F2F", uAdmin);
        criarMarcador("DEBITO", "#1976D2", uAdmin);

        // 5. Transações
        List<Transacao> transacoes = new ArrayList<>();

        // Lucas
        transacoes.add(criarTransacao("Salário Mensal", new BigDecimal("5000.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.RECEITA, LocalDateTime.of(2026, 5, 1, 8, 0), cInter, mPixLucas));
        transacoes.add(criarTransacao("Supermercado", new BigDecimal("450.50"), catAlimentacao, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(2026, 5, 1, 18, 30), cInter, mDebitoLucas));
        transacoes.add(criarTransacao("Gasolina", new BigDecimal("200.00"), catTransporte, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(2026, 5, 2, 10, 0), cInter, mDebitoLucas));
        transacoes.add(criarTransacao("Netflix", new BigDecimal("55.90"), catLazer, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(2026, 5, 3, 9, 0), cInterCard, mCreditoLucas));
        transacoes.add(criarTransacao("Jantar Japonês", new BigDecimal("120.00"), catAlimentacao, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(2026, 5, 3, 21, 0), cInterCard, mCreditoLucas));
        transacoes.add(criarTransacao("Rendimento CDI", new BigDecimal("15.20"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.RECEITA, LocalDateTime.of(2026, 5, 4, 10, 0), cInter, mPixLucas));
        transacoes.add(criarTransacao("Farmácia", new BigDecimal("85.00"), catSaude, EstadoTransacao.PENDENTE, TipoTransacao.DESPESA, LocalDateTime.of(2026, 5, 5, 14, 0), cInter, mDebitoLucas));
        transacoes.add(criarTransacao("Uber Faculdade", new BigDecimal("25.00"), catTransporte, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(2026, 5, 2, 19, 0), cInterCard, mCreditoLucas));
        transacoes.add(criarTransacao("Livros Java", new BigDecimal("150.00"), catEducacao, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(2026, 5, 4, 15, 0), cInterCard, mCreditoLucas));
        transacoes.add(criarTransacao("Venda de Teclado Antigo", new BigDecimal("300.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.RECEITA, LocalDateTime.of(2026, 5, 5, 11, 0), cInter, mPixLucas));
        transacoes.add(criarTransacao("Bônus Performance", new BigDecimal("1000.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.RECEITA, LocalDateTime.of(2026, 5, 1, 8, 0), cInter, mPixLucas));
        transacoes.add(criarTransacao("Uber Cinema", new BigDecimal("20.00"), catTransporte, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(2026, 5, 1, 19, 0), cInterCard, mCreditoLucas));
        transacoes.add(criarTransacao("Pizza Família", new BigDecimal("85.00"), catAlimentacao, EstadoTransacao.PENDENTE, TipoTransacao.DESPESA, LocalDateTime.of(2026, 5, 5, 20, 30), cInter, mDebitoLucas));

        // Bruno
        transacoes.add(criarTransacao("Dividendo Ações", new BigDecimal("15000.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.RECEITA, LocalDateTime.of(2026, 5, 1, 9, 0), cBB, mPixBruno));
        transacoes.add(criarTransacao("Compra de Luxo", new BigDecimal("1200.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(2026, 5, 1, 14, 0), cNubankCard, mCreditoBruno));
        transacoes.add(criarTransacao("Manutenção Carro", new BigDecimal("800.00"), catTransporte, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(2026, 5, 2, 11, 0), cNubankCard, mCreditoBruno));
        transacoes.add(criarTransacao("Almoço Executivo", new BigDecimal("85.00"), catAlimentacao, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(2026, 5, 2, 13, 0), cNubank, mDebitoBruno));
        transacoes.add(criarTransacao("Café Padaria", new BigDecimal("12.50"), catAlimentacao, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(2026, 5, 3, 8, 0), cNubank, mDebitoBruno));
        transacoes.add(criarTransacao("Mensalidade Software", new BigDecimal("200.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(2026, 5, 3, 10, 0), cNubankCard, mCreditoBruno));
        transacoes.add(criarTransacao("Viagem Fim de Semana", new BigDecimal("1500.00"), catLazer, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(2026, 5, 4, 17, 0), cNubankCard, mCreditoBruno));
        transacoes.add(criarTransacao("Freelance Backend", new BigDecimal("4000.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.RECEITA, LocalDateTime.of(2026, 5, 5, 9, 0), cNubank, mPixBruno));
        transacoes.add(criarTransacao("Seguro Saúde", new BigDecimal("800.00"), catSaude, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(2026, 5, 5, 10, 0), cNubankCard, mCreditoBruno));
        transacoes.add(criarTransacao("Depósito em Dinheiro", new BigDecimal("1000.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.RECEITA, LocalDateTime.of(2026, 5, 5, 15, 0), cBB, mPixBruno));
        transacoes.add(criarTransacao("Troca de Óleo", new BigDecimal("180.00"), catTransporte, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(2026, 5, 2, 9, 0), cNubank, mDebitoBruno));
        transacoes.add(criarTransacao("Conserto Celular", new BigDecimal("400.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(2026, 5, 2, 14, 0), cNubank, mDebitoBruno));

        // Eduardo
        transacoes.add(criarTransacao("Aporte Inicial", new BigDecimal("3000.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.RECEITA, LocalDateTime.of(2026, 5, 1, 8, 0), cItau, mPixEduardo));
        transacoes.add(criarTransacao("Aluguel Escritório", new BigDecimal("1200.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(2026, 5, 1, 10, 0), cItau, mDebitoEduardo));
        transacoes.add(criarTransacao("Internet Fibra", new BigDecimal("120.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(2026, 5, 2, 9, 0), cItauCard, mCreditoEduardo));
        transacoes.add(criarTransacao("Rodízio de Pizza", new BigDecimal("90.00"), catAlimentacao, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(2026, 5, 2, 20, 0), cItauCard, mCreditoEduardo));
        transacoes.add(criarTransacao("Cinema", new BigDecimal("45.00"), catLazer, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(2026, 5, 3, 16, 0), cItauCard, mCreditoEduardo));

        // Pedro
        transacoes.add(criarTransacao("Mesada", new BigDecimal("500.00"), catOutros, EstadoTransacao.REALIZADA, TipoTransacao.RECEITA, LocalDateTime.of(2026, 5, 1, 12, 0), cSantander, mPixPedro));
        transacoes.add(criarTransacao("Lanche Faculdade", new BigDecimal("18.00"), catAlimentacao, EstadoTransacao.REALIZADA, TipoTransacao.DESPESA, LocalDateTime.of(2026, 5, 1, 21, 0), cSantander, mDebitoPedro));
        transacoes.add(criarTransacao("X-Burguer", new BigDecimal("35.00"), catAlimentacao, EstadoTransacao.PENDENTE, TipoTransacao.DESPESA, LocalDateTime.of(2026, 5, 5, 21, 0), cSantanderCard, mCreditoPedro));

        transacaoRepository.saveAll(transacoes);

        // 6. Metas
        List<Meta> metas = new ArrayList<>();
        metas.add(criarMeta("Reserva de Emergência", "Guardar dinheiro para imprevistos", new BigDecimal("5000.00"), new BigDecimal("1500.00"), "https://link-da-foto.com/reserva.png", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), TipoMeta.POUPANCA, uLucas));
        metas.add(criarMeta("Viagem de Férias", "Juntar dinheiro para viajar no final do ano", new BigDecimal("3000.00"), new BigDecimal("1200.00"), "https://link-da-foto.com/viagem.png", LocalDate.of(2026, 2, 15), LocalDate.of(2026, 11, 30), TipoMeta.PRAZO_FIXO, uBruno));
        metas.add(criarMeta("Limite Alimentação", "Controlar gastos mensais com supermercado e ifood", new BigDecimal("800.00"), new BigDecimal("650.00"), "https://link-da-foto.com/comida.png", LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), TipoMeta.GASTO, uEduardo));
        metas.add(criarMeta("Limite Lazer", "Evitar gastar demais com saídas", new BigDecimal("500.00"), new BigDecimal("120.00"), "https://link-da-foto.com/lazer.png", LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), TipoMeta.GASTO, uPedro));
        metas.add(criarMeta("Tesouro Direto", "Investir em renda fixa para o futuro", new BigDecimal("10000.00"), new BigDecimal("4500.00"), "https://link-da-foto.com/investimento.png", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), TipoMeta.POUPANCA, uLucas));
        metas.add(criarMeta("Curso de Especialização", "Guardar para curso de pós-graduação", new BigDecimal("8000.00"), new BigDecimal("2000.00"), "https://link-da-foto.com/curso.png", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 9, 30), TipoMeta.PRAZO_FIXO, uBruno));
        metas.add(criarMeta("Comprar Notebook", "Equipamento novo para trabalho e estudos", new BigDecimal("4000.00"), new BigDecimal("3800.00"), "https://link-da-foto.com/notebook.png", LocalDate.of(2026, 3, 20), LocalDate.of(2026, 10, 15), TipoMeta.POUPANCA, uEduardo));
        metas.add(criarMeta("Assinaturas e Academia", "Orçamento para Netflix, Spotify e SmartFit", new BigDecimal("150.00"), new BigDecimal("150.00"), "https://link-da-foto.com/academia.png", LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), TipoMeta.GASTO, uPedro));
        metas.add(criarMeta("Intercâmbio", "Juntar para curso de idiomas no exterior", new BigDecimal("25000.00"), new BigDecimal("5000.00"), "https://link-da-foto.com/fundo.png", LocalDate.of(2026, 1, 1), LocalDate.of(2027, 12, 31), TipoMeta.PRAZO_FIXO, uBruno));
        metas.add(criarMeta("Quitar Empréstimo", "Finalizar parcelas do empréstimo pessoal", new BigDecimal("7000.00"), new BigDecimal("3500.00"), "https://link-da-foto.com/emprestimo.png", LocalDate.of(2026, 1, 5), LocalDate.of(2027, 6, 30), TipoMeta.DIVIDA, uLucas));

        metaRepository.saveAll(metas);

        log.info("[DatabaseSeeder] Carga inicial de dados finalizada com sucesso!");
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
        return m;
    }

}

