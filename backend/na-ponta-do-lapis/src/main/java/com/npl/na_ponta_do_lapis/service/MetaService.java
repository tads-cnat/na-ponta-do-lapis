package com.npl.na_ponta_do_lapis.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.npl.na_ponta_do_lapis.entity.AporteMeta;
import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.Meta;
import com.npl.na_ponta_do_lapis.entity.TipoCategoria;
import com.npl.na_ponta_do_lapis.entity.Transacao;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.EstadoTransacao;
import com.npl.na_ponta_do_lapis.entity.enums.StatusMeta;
import com.npl.na_ponta_do_lapis.entity.enums.TipoMeta;
import com.npl.na_ponta_do_lapis.entity.enums.TipoNotificacao;
import com.npl.na_ponta_do_lapis.entity.enums.TipoTransacao;
import com.npl.na_ponta_do_lapis.repository.AporteMetaRepository;
import com.npl.na_ponta_do_lapis.repository.ContaFinanceiraRepository;
import com.npl.na_ponta_do_lapis.repository.MetaRepository;
import com.npl.na_ponta_do_lapis.repository.TipoCategoriaRepository;
import com.npl.na_ponta_do_lapis.repository.TransacaoRepository;
import com.npl.na_ponta_do_lapis.service.factory.CalculadoraMetaFactory;
import com.npl.na_ponta_do_lapis.service.strategy.CalculadoraMetaStrategy;
import com.npl.na_ponta_do_lapis.web.dto.AporteMetaDTO;
import com.npl.na_ponta_do_lapis.web.dto.AporteMetaResponseDTO;
import com.npl.na_ponta_do_lapis.web.dto.MetaDTO;
import com.npl.na_ponta_do_lapis.web.dto.MetaResponseDTO;
import com.npl.na_ponta_do_lapis.web.exception.ContaIdNaoExisteException;
import com.npl.na_ponta_do_lapis.web.exception.MetaNaoEncontradaException;

@Service
public class MetaService {

    private final CalculadoraMetaFactory factory;
    private final MetaRepository metaRepository;
    private final ContaFinanceiraRepository contaFinanceiraRepository;
    private final TipoCategoriaRepository tipoCategoriaRepository;
    private final AporteMetaRepository aporteMetaRepository;
    private final TransacaoRepository transacaoRepository;
    private final NotificacaoService notificacaoService;
    private final ConquistaService conquistaService;

    public MetaService(MetaRepository metaRepository, CalculadoraMetaFactory factory,
                       ContaFinanceiraRepository contaFinanceiraRepository,
                       TipoCategoriaRepository tipoCategoriaRepository,
                       AporteMetaRepository aporteMetaRepository,
                       TransacaoRepository transacaoRepository,
                       NotificacaoService notificacaoService,
                       ConquistaService conquistaService) {
        this.metaRepository = metaRepository;
        this.factory = factory;
        this.contaFinanceiraRepository = contaFinanceiraRepository;
        this.tipoCategoriaRepository = tipoCategoriaRepository;
        this.aporteMetaRepository = aporteMetaRepository;
        this.transacaoRepository = transacaoRepository;
        this.notificacaoService = notificacaoService;
        this.conquistaService = conquistaService;
    }

    @Transactional(readOnly = true)
    public List<MetaResponseDTO> listarTodas(Usuario usuarioAutenticado) {
        Long familiaId = usuarioAutenticado.getFamilia() != null ? usuarioAutenticado.getFamilia().getId() : null;
        List<Meta> metas = familiaId != null
                ? metaRepository.findByUsuarioIdOuFamiliaId(usuarioAutenticado.getId(), familiaId)
                : metaRepository.findByUsuarioId(usuarioAutenticado.getId());

        return metas.stream()
                .map(this::montarResponseComProgresso)
                .toList();
    }

    @Transactional(readOnly = true)
    public MetaResponseDTO buscarPorId(Long id, Usuario usuarioAutenticado) {
        Meta meta = metaRepository.findById(id)
                .orElseThrow(MetaNaoEncontradaException::new);

        validarAcessoMeta(meta, usuarioAutenticado);
        return montarResponseComProgresso(meta);
    }

    @Transactional
    public MetaResponseDTO criar(MetaDTO dto, Usuario usuarioAutenticado) {
        ContaFinanceira conta = null;
        if (dto.contaId() != null) {
            conta = contaFinanceiraRepository.findById(dto.contaId())
                    .orElseThrow(() -> new ContaIdNaoExisteException("Conta financeira não encontrada"));

            if (!conta.getUsuario().getId().equals(usuarioAutenticado.getId())) {
                throw new ContaIdNaoExisteException("Conta não pertence ao usuário autenticado");
            }
        }

        TipoCategoria categoria = null;
        if (dto.categoriaId() != null) {
            categoria = tipoCategoriaRepository.findById(dto.categoriaId()).orElse(null);
        }

        Meta novaMeta = dto.toEntity();
        if (novaMeta.getDataInicio() == null) {
            novaMeta.setDataInicio(LocalDate.now());
        }
        novaMeta.setUsuario(usuarioAutenticado);
        novaMeta.setConta(conta);
        novaMeta.setCategoria(categoria);
        novaMeta.setStatus(StatusMeta.EM_ANDAMENTO);

        if (Boolean.TRUE.equals(dto.familiar()) && usuarioAutenticado.getFamilia() != null) {
            novaMeta.setFamilia(usuarioAutenticado.getFamilia());
        }

        Meta metaSalva = metaRepository.save(novaMeta);
        conquistaService.verificarConquistasCriacaoMeta(metaSalva, usuarioAutenticado);
        return montarResponseComProgresso(metaSalva);
    }

    @Transactional
    public MetaResponseDTO atualizar(Long id, MetaDTO dto, Usuario usuarioAutenticado) {
        Meta meta = metaRepository.findById(id)
                .orElseThrow(MetaNaoEncontradaException::new);

        validarAcessoMeta(meta, usuarioAutenticado);

        ContaFinanceira conta = null;
        if (dto.contaId() != null) {
            conta = contaFinanceiraRepository.findById(dto.contaId())
                    .orElseThrow(() -> new ContaIdNaoExisteException("Conta financeira não encontrada"));

            if (!conta.getUsuario().getId().equals(usuarioAutenticado.getId())) {
                throw new ContaIdNaoExisteException("Conta não pertence ao usuário autenticado");
            }
        }

        TipoCategoria categoria = null;
        if (dto.categoriaId() != null) {
            categoria = tipoCategoriaRepository.findById(dto.categoriaId()).orElse(null);
        }

        meta.setNome(dto.nome());
        meta.setDescricao(dto.descricao());
        meta.setFotoUrl(dto.fotoUrl());
        meta.setValor(dto.valor());
        meta.setDataLimite(dto.dataLimite());
        meta.setTipoMeta(dto.tipoMeta());
        meta.setConta(conta);
        meta.setCategoria(categoria);

        if (Boolean.TRUE.equals(dto.familiar()) && usuarioAutenticado.getFamilia() != null) {
            meta.setFamilia(usuarioAutenticado.getFamilia());
        } else if (Boolean.FALSE.equals(dto.familiar())) {
            meta.setFamilia(null);
        }

        Meta atualizado = metaRepository.save(meta);
        return montarResponseComProgresso(atualizado);
    }

    @Transactional
    public void deletar(Long id, Usuario usuarioAutenticado) {
        Meta meta = metaRepository.findById(id)
                .orElseThrow(MetaNaoEncontradaException::new);

        validarAcessoMeta(meta, usuarioAutenticado);
        metaRepository.delete(meta);
    }

    @Transactional
    public AporteMetaResponseDTO registrarAporte(Long metaId, AporteMetaDTO dto, Usuario usuarioAutenticado) {
        Meta meta = metaRepository.findById(metaId)
                .orElseThrow(MetaNaoEncontradaException::new);

        validarAcessoMeta(meta, usuarioAutenticado);

        ContaFinanceira contaOrigem = null;
        if (dto.contaOrigemId() != null) {
            contaOrigem = contaFinanceiraRepository.findById(dto.contaOrigemId())
                    .orElseThrow(() -> new ContaIdNaoExisteException("Conta de origem não encontrada"));

            if (!contaOrigem.getUsuario().getId().equals(usuarioAutenticado.getId())) {
                throw new ContaIdNaoExisteException("Conta de origem não pertence ao usuário");
            }

            // 1. Debita da conta de origem
            contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(dto.valor()));
            contaFinanceiraRepository.save(contaOrigem);

            // 2. Busca categoria adequada para o extrato (Investimentos ou Outros)
            TipoCategoria categoriaExtrato = tipoCategoriaRepository.findByNome("Outros").orElse(null);

            // 3. Registra a Transação de Saída no Extrato Financeiro
            Transacao transacaoSaida = new Transacao();
            transacaoSaida.setDescricao("Aporte na meta: " + meta.getNome());
            transacaoSaida.setValor(dto.valor());
            transacaoSaida.setTipo(TipoTransacao.DESPESA);
            transacaoSaida.setEstado(EstadoTransacao.REALIZADA);
            transacaoSaida.setDataHora(LocalDateTime.now());
            transacaoSaida.setContaFinanceira(contaOrigem);
            transacaoSaida.setCategoria(categoriaExtrato);
            transacaoRepository.save(transacaoSaida);

            // 4. Se a Meta possui uma conta destino vinculada diferente da origem, credita nela (Transferência)
            if (meta.getConta() != null && !meta.getConta().getId().equals(contaOrigem.getId())) {
                ContaFinanceira contaDestino = meta.getConta();
                contaDestino.setSaldo(contaDestino.getSaldo().add(dto.valor()));
                contaFinanceiraRepository.save(contaDestino);

                Transacao transacaoEntrada = new Transacao();
                transacaoEntrada.setDescricao("Recebimento de aporte meta: " + meta.getNome());
                transacaoEntrada.setValor(dto.valor());
                transacaoEntrada.setTipo(TipoTransacao.RECEITA);
                transacaoEntrada.setEstado(EstadoTransacao.REALIZADA);
                transacaoEntrada.setDataHora(LocalDateTime.now());
                transacaoEntrada.setContaFinanceira(contaDestino);
                transacaoEntrada.setCategoria(categoriaExtrato);
                transacaoRepository.save(transacaoEntrada);
            }
        }

        AporteMeta aporte = new AporteMeta(meta, usuarioAutenticado, contaOrigem, dto.valor(), dto.observacao());
        AporteMeta aporteSalvo = aporteMetaRepository.save(aporte);

        BigDecimal valorAnterior = meta.getValorAtual() != null ? meta.getValorAtual() : BigDecimal.ZERO;
        BigDecimal novoValor = valorAnterior.add(dto.valor());
        meta.setValorAtual(novoValor);

        double progressoAntes = calcularPercentual(valorAnterior, meta.getValor());
        double progressoDepois = calcularPercentual(novoValor, meta.getValor());

        if (novoValor.compareTo(meta.getValor()) >= 0 && meta.getStatus() != StatusMeta.CONCLUIDA) {
            meta.setStatus(StatusMeta.CONCLUIDA);
            notificacaoService.criarNotificacao(
                    usuarioAutenticado,
                    "Meta Concluída: " + meta.getNome(),
                    "Parabéns! Você alcançou o objetivo de R$ " + meta.getValor() + " na meta " + meta.getNome() + "!",
                    TipoNotificacao.META_ATINGIDA,
                    "/metas"
            );
        } else if (progressoAntes < 80.0 && progressoDepois >= 80.0) {
            notificacaoService.criarNotificacao(
                    usuarioAutenticado,
                    "Meta quase lá: " + meta.getNome(),
                    "Você já atingiu " + String.format("%.0f", progressoDepois) + "% da meta " + meta.getNome() + ". Falta pouco!",
                    TipoNotificacao.META_ALERTA_80,
                    "/metas"
            );
        } else if (progressoAntes < 50.0 && progressoDepois >= 50.0) {
            notificacaoService.criarNotificacao(
                    usuarioAutenticado,
                    "Metade do caminho: " + meta.getNome(),
                    "Você alcançou 50% da meta " + meta.getNome() + ". Continue assim!",
                    TipoNotificacao.META_ALERTA_50,
                    "/metas"
            );
        }

        metaRepository.save(meta);
        conquistaService.verificarConquistasAporte(meta, usuarioAutenticado);
        return AporteMetaResponseDTO.fromEntity(aporteSalvo);
    }

    @Transactional(readOnly = true)
    public List<AporteMetaResponseDTO> listarAportes(Long metaId, Usuario usuarioAutenticado) {
        Meta meta = metaRepository.findById(metaId)
                .orElseThrow(MetaNaoEncontradaException::new);

        validarAcessoMeta(meta, usuarioAutenticado);
        return aporteMetaRepository.findByMetaIdOrderByDataAporteDesc(metaId)
                .stream()
                .map(AporteMetaResponseDTO::fromEntity)
                .toList();
    }

    private void validarAcessoMeta(Meta meta, Usuario usuario) {
        boolean ehDono = meta.getUsuario().getId().equals(usuario.getId());
        boolean ehMembroFamilia = meta.getFamilia() != null && usuario.getFamilia() != null
                && meta.getFamilia().getId().equals(usuario.getFamilia().getId());

        if (!ehDono && !ehMembroFamilia) {
            throw new MetaNaoEncontradaException();
        }
    }

    private double calcularPercentual(BigDecimal atual, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) <= 0) return 0.0;
        return (atual.divide(total, 4, java.math.RoundingMode.HALF_UP).doubleValue()) * 100.0;
    }

    private MetaResponseDTO montarResponseComProgresso(Meta meta) {
        BigDecimal valorAtual = meta.getValorAtual();
        if (meta.getTipoMeta() == TipoMeta.GASTO) {
            LocalDate hoje = LocalDate.now();
            LocalDateTime inicio = meta.getDataInicio() != null
                    ? meta.getDataInicio().atStartOfDay()
                    : YearMonth.from(hoje).atDay(1).atStartOfDay();
            LocalDateTime fim = meta.getDataLimite() != null
                    ? meta.getDataLimite().atTime(23, 59, 59)
                    : YearMonth.from(hoje).atEndOfMonth().atTime(23, 59, 59);

            Long usuarioId = meta.getFamilia() == null ? meta.getUsuario().getId() : null;
            Long familiaId = meta.getFamilia() != null ? meta.getFamilia().getId() : null;
            Long contaId = meta.getConta() != null ? meta.getConta().getId() : null;
            Long categoriaId = meta.getCategoria() != null ? meta.getCategoria().getId() : null;

            valorAtual = transacaoRepository.calcularTotalDespesas(usuarioId, familiaId, contaId, categoriaId, inicio, fim);
            meta.setValorAtual(valorAtual);

            if (valorAtual.compareTo(meta.getValor()) >= 0) {
                meta.setStatus(StatusMeta.ESTOURADA);
            } else {
                meta.setStatus(StatusMeta.EM_ANDAMENTO);
            }
        }

        CalculadoraMetaStrategy calculadora = factory.obterCalculadora(meta.getTipoMeta());
        double progresso = calculadora.calcularProgresso(valorAtual, meta.getValor());
        return new MetaResponseDTO(meta, progresso);
    }
}