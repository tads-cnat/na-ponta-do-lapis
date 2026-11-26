package com.npl.na_ponta_do_lapis.service;

import com.npl.na_ponta_do_lapis.entity.*;
import com.npl.na_ponta_do_lapis.entity.enums.*;
import com.npl.na_ponta_do_lapis.repository.*;
import com.npl.na_ponta_do_lapis.security.jwt.JwtAuthFilter;
import com.npl.na_ponta_do_lapis.web.dto.AssinaturaRecorrenteRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.AssinaturaRecorrenteResponseDTO;
import com.npl.na_ponta_do_lapis.web.dto.AssinaturasResumoDTO;
import com.npl.na_ponta_do_lapis.web.dto.ParcelamentoResumoDTO;
import com.npl.na_ponta_do_lapis.web.dto.TransacoesResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AssinaturaRecorrenteService {

    private static final Logger log = LoggerFactory.getLogger(AssinaturaRecorrenteService.class);
    private static final int MAX_CICLOS_POR_EXECUCAO = 12;

    private final AssinaturaRecorrenteRepository assinaturaRepository;
    private final ContaFinanceiraService contaFinanceiraService;
    private final TipoCategoriaRepository categoriaRepository;
    private final MarcadorRepository marcadorRepository;
    private final TransacaoRepository transacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoService notificacaoService;
    private final ConquistaService conquistaService;

    public AssinaturaRecorrenteService(
            AssinaturaRecorrenteRepository assinaturaRepository,
            ContaFinanceiraService contaFinanceiraService,
            TipoCategoriaRepository categoriaRepository,
            MarcadorRepository marcadorRepository,
            TransacaoRepository transacaoRepository,
            UsuarioRepository usuarioRepository,
            NotificacaoService notificacaoService,
            ConquistaService conquistaService) {
        this.assinaturaRepository = assinaturaRepository;
        this.contaFinanceiraService = contaFinanceiraService;
        this.categoriaRepository = categoriaRepository;
        this.marcadorRepository = marcadorRepository;
        this.transacaoRepository = transacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacaoService = notificacaoService;
        this.conquistaService = conquistaService;
    }

    @Transactional(readOnly = true)
    public AssinaturasResumoDTO listarMinhas() {
        String email = JwtAuthFilter.getEmailUsuarioLogado();
        List<AssinaturaRecorrente> lista = assinaturaRepository.buscarPorUsuarioEmail(email);

        List<AssinaturaRecorrenteResponseDTO> dtos = lista.stream()
                .map(AssinaturaRecorrenteResponseDTO::new)
                .toList();

        BigDecimal totalMensalDespesas = dtos.stream()
                .filter(a -> a.status() == StatusRecorrencia.ATIVA && a.tipo() == TipoTransacao.DESPESA)
                .map(AssinaturaRecorrenteResponseDTO::valorMensalEquivalente)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalMensalReceitas = dtos.stream()
                .filter(a -> a.status() == StatusRecorrencia.ATIVA && a.tipo() == TipoTransacao.RECEITA)
                .map(AssinaturaRecorrenteResponseDTO::valorMensalEquivalente)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAnualDespesas = totalMensalDespesas.multiply(new BigDecimal("12"));

        long ativas = dtos.stream().filter(a -> a.status() == StatusRecorrencia.ATIVA).count();
        long pausadas = dtos.stream().filter(a -> a.status() == StatusRecorrencia.PAUSADA).count();

        return new AssinaturasResumoDTO(
                totalMensalDespesas,
                totalMensalReceitas,
                totalAnualDespesas,
                ativas,
                pausadas,
                dtos
        );
    }

    @Transactional
    public AssinaturaRecorrenteResponseDTO criar(AssinaturaRecorrenteRequestDTO dto) throws AccessDeniedException {
        String email = JwtAuthFilter.getEmailUsuarioLogado();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário autenticado não encontrado."));

        AssinaturaRecorrente assinatura = new AssinaturaRecorrente();
        assinatura.setUsuario(usuario);
        preencherCampos(assinatura, dto, email);
        assinatura.setStatus(StatusRecorrencia.ATIVA);

        AssinaturaRecorrente salva = assinaturaRepository.save(assinatura);

        if (Boolean.TRUE.equals(dto.lancarPrimeiraAgora())) {
            gerarTransacaoDoCiclo(salva, salva.getProximaCobranca());
            salva.setUltimaCobranca(salva.getProximaCobranca());
            salva.setProximaCobranca(salva.getFrequencia().avancar(salva.getProximaCobranca(), salva.getDiaCobranca()));
            salva = assinaturaRepository.save(salva);
        }

        conquistaService.desbloquear(usuario, "ASSINATURA_CONTROLADA");
        return new AssinaturaRecorrenteResponseDTO(salva);
    }

    @Transactional
    public AssinaturaRecorrenteResponseDTO atualizar(Long id, AssinaturaRecorrenteRequestDTO dto) throws AccessDeniedException {
        String email = JwtAuthFilter.getEmailUsuarioLogado();
        AssinaturaRecorrente assinatura = buscarEDono(id, email);
        preencherCampos(assinatura, dto, email);
        return new AssinaturaRecorrenteResponseDTO(assinaturaRepository.save(assinatura));
    }

    @Transactional
    public AssinaturaRecorrenteResponseDTO alterarStatus(Long id, StatusRecorrencia novoStatus) throws AccessDeniedException {
        String email = JwtAuthFilter.getEmailUsuarioLogado();
        AssinaturaRecorrente assinatura = buscarEDono(id, email);
        assinatura.setStatus(novoStatus);
        return new AssinaturaRecorrenteResponseDTO(assinaturaRepository.save(assinatura));
    }

    @Transactional
    public TransacoesResponseDTO lancarCicloManualmente(Long id) throws AccessDeniedException {
        String email = JwtAuthFilter.getEmailUsuarioLogado();
        AssinaturaRecorrente assinatura = buscarEDono(id, email);

        LocalDate dataLancamento = LocalDate.now();
        Transacao tx = gerarTransacaoDoCiclo(assinatura, dataLancamento);
        assinatura.setUltimaCobranca(dataLancamento);

        LocalDate base = assinatura.getProximaCobranca().isAfter(dataLancamento)
                ? assinatura.getProximaCobranca()
                : dataLancamento;
        assinatura.setProximaCobranca(assinatura.getFrequencia().avancar(base, assinatura.getDiaCobranca()));
        assinaturaRepository.save(assinatura);

        return new TransacoesResponseDTO(tx);
    }

    @Transactional
    public void excluir(Long id) throws AccessDeniedException {
        String email = JwtAuthFilter.getEmailUsuarioLogado();
        AssinaturaRecorrente assinatura = buscarEDono(id, email);
        assinaturaRepository.delete(assinatura);
    }

    /**
     * Executa diariamente às 06:15 (America/Sao_Paulo) para lançar transações de assinaturas ativas vencidas.
     */
    @Scheduled(cron = "0 15 6 * * *", zone = "America/Sao_Paulo")
    @Transactional
    public void processarVencidasHoje() {
        LocalDate hoje = LocalDate.now();
        List<AssinaturaRecorrente> vencidas = assinaturaRepository.buscarVencidas(StatusRecorrencia.ATIVA, hoje);
        if (vencidas.isEmpty()) {
            return;
        }

        int lancadas = 0;
        for (AssinaturaRecorrente assinatura : vencidas) {
            int ciclos = 0;
            while (!assinatura.getProximaCobranca().isAfter(hoje) && ciclos < MAX_CICLOS_POR_EXECUCAO) {
                LocalDate dataCiclo = assinatura.getProximaCobranca();
                gerarTransacaoDoCiclo(assinatura, dataCiclo);
                notificarLancamentoAutomatico(assinatura);
                assinatura.setUltimaCobranca(dataCiclo);
                assinatura.setProximaCobranca(
                        assinatura.getFrequencia().avancar(dataCiclo, assinatura.getDiaCobranca()));
                lancadas++;
                ciclos++;
            }
            assinaturaRepository.save(assinatura);
        }
        log.info("[Assinaturas] Processamento diário concluído: {} transação(ões) gerada(s).", lancadas);
    }

    @Transactional(readOnly = true)
    public List<ParcelamentoResumoDTO> listarParcelamentosEmAberto() {
        String email = JwtAuthFilter.getEmailUsuarioLogado();
        List<Transacao> transacoes = transacaoRepository.buscarTransacoesUsuarioLogado(email);
        return agruparParcelamentosEmAberto(transacoes);
    }

    public static List<ParcelamentoResumoDTO> agruparParcelamentosEmAberto(List<Transacao> transacoes) {
        LocalDate hoje = LocalDate.now();
        Map<String, List<Transacao>> grupos = transacoes.stream()
                .filter(t -> t.getGrupoParcelamento() != null && !t.getGrupoParcelamento().isBlank())
                .collect(Collectors.groupingBy(Transacao::getGrupoParcelamento));

        return grupos.entrySet().stream()
                .map(entry -> {
                    List<Transacao> parcelas = entry.getValue().stream()
                            .sorted(Comparator.comparing(
                                    t -> t.getNumeroParcela() != null ? t.getNumeroParcela() : 0))
                            .toList();
                    Transacao primeira = parcelas.get(0);
                    int total = primeira.getTotalParcelas() != null
                            ? Math.max(primeira.getTotalParcelas(), parcelas.size())
                            : parcelas.size();

                    List<Transacao> pendentes = parcelas.stream()
                            .filter(p -> p.getEstado() == EstadoTransacao.PENDENTE
                                    || (p.getDataHora() != null && p.getDataHora().toLocalDate().isAfter(hoje)))
                            .toList();

                    if (pendentes.isEmpty()) {
                        return null;
                    }

                    int pagas = (int) parcelas.stream()
                            .filter(p -> p.getEstado() == EstadoTransacao.REALIZADA
                                    && (p.getDataHora() == null || !p.getDataHora().toLocalDate().isAfter(hoje)))
                            .count();

                    BigDecimal valorRestante = pendentes.stream()
                            .map(Transacao::getValor)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    LocalDate proximaData = pendentes.stream()
                            .map(p -> p.getDataHora() != null ? p.getDataHora().toLocalDate() : null)
                            .filter(java.util.Objects::nonNull)
                            .min(LocalDate::compareTo)
                            .orElse(null);

                    LocalDate ultimaData = pendentes.stream()
                            .map(p -> p.getDataHora() != null ? p.getDataHora().toLocalDate() : null)
                            .filter(java.util.Objects::nonNull)
                            .max(LocalDate::compareTo)
                            .orElse(null);

                    String descricaoLimpa = primeira.getDescricao() != null
                            ? primeira.getDescricao().replaceAll("\\s*\\(\\d+/\\d+\\)$", "").trim()
                            : "Compra parcelada";

                    return new ParcelamentoResumoDTO(
                            entry.getKey(),
                            descricaoLimpa,
                            primeira.getValor(),
                            valorRestante,
                            pagas,
                            pendentes.size(),
                            total,
                            proximaData,
                            ultimaData,
                            primeira.getContaFinanceira() != null ? primeira.getContaFinanceira().getNome() : null,
                            primeira.getCategoria() != null ? primeira.getCategoria().getNome() : null
                    );
                })
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparing(
                        ParcelamentoResumoDTO::proximaParcelaData,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private void notificarLancamentoAutomatico(AssinaturaRecorrente assinatura) {
        try {
            NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
            String valorFmt = nf.format(assinatura.getValor());
            String estadoTxt = assinatura.getEstadoGeracao() == EstadoTransacao.REALIZADA ? "Realizada" : "Pendente";
            notificacaoService.criarNotificacao(
                    assinatura.getUsuario(),
                    "Assinatura lançada: " + assinatura.getDescricao(),
                    String.format("A recorrência %s (%s) foi lançada na conta %s como %s.",
                            assinatura.getDescricao(), valorFmt,
                            assinatura.getContaFinanceira().getNome(), estadoTxt),
                    TipoNotificacao.SISTEMA,
                    "/app/transacoes"
            );
        } catch (Exception ex) {
            log.warn("[Assinaturas] Falha ao criar notificação para assinatura {}: {}", assinatura.getId(), ex.getMessage());
        }
    }

    private void preencherCampos(AssinaturaRecorrente assinatura, AssinaturaRecorrenteRequestDTO dto, String email)
            throws AccessDeniedException {
        ContaFinanceira conta = contaFinanceiraService.buscarContaPorIdObject(dto.contaId());
        if (!conta.getUsuario().getEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException("Você não tem permissão para usar esta conta financeira.");
        }

        TipoCategoria categoria = categoriaRepository.findById(dto.categoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada."));

        Marcador marcador = null;
        if (dto.marcadorId() != null) {
            marcador = marcadorRepository.findById(dto.marcadorId())
                    .orElseThrow(() -> new IllegalArgumentException("Marcador não encontrado."));
            if (marcador.getUsuario() != null && !marcador.getUsuario().getEmail().equalsIgnoreCase(email)) {
                throw new AccessDeniedException("Você não tem permissão para usar este marcador.");
            }
        }

        assinatura.setDescricao(dto.descricao().trim());
        assinatura.setValor(dto.valor());
        assinatura.setTipo(dto.tipo());
        assinatura.setFrequencia(dto.frequencia());
        assinatura.setEstadoGeracao(dto.estadoGeracao() != null ? dto.estadoGeracao() : EstadoTransacao.PENDENTE);
        assinatura.setProximaCobranca(dto.proximaCobranca());
        assinatura.setDiaCobranca(dto.proximaCobranca().getDayOfMonth());
        assinatura.setContaFinanceira(conta);
        assinatura.setCategoria(categoria);
        assinatura.setMarcador(marcador);
    }

    private AssinaturaRecorrente buscarEDono(Long id, String email) throws AccessDeniedException {
        AssinaturaRecorrente assinatura = assinaturaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Assinatura não encontrada."));
        if (!assinatura.getUsuario().getEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException("Você não tem permissão para acessar esta assinatura.");
        }
        return assinatura;
    }

    private Transacao gerarTransacaoDoCiclo(AssinaturaRecorrente assinatura, LocalDate data) {
        ContaFinanceira conta = contaFinanceiraService.buscarContaPorIdObject(assinatura.getContaFinanceira().getId());

        Transacao tx = new Transacao();
        tx.setDescricao(assinatura.getDescricao());
        tx.setValor(assinatura.getValor());
        tx.setTipo(assinatura.getTipo());
        tx.setEstado(assinatura.getEstadoGeracao());
        tx.setDataHora(data.atTime(LocalTime.of(8, 0)));
        tx.setContaFinanceira(conta);
        tx.setCategoria(assinatura.getCategoria());
        tx.setMarcador(assinatura.getMarcador());
        tx.setOrigem(OrigemTransacao.MANUAL);

        if (tx.getEstado() == EstadoTransacao.REALIZADA) {
            if (tx.getTipo() == TipoTransacao.DESPESA) {
                conta.setSaldo(conta.getSaldo().subtract(tx.getValor()));
            } else {
                conta.setSaldo(conta.getSaldo().add(tx.getValor()));
            }
        }

        return transacaoRepository.save(tx);
    }
}
