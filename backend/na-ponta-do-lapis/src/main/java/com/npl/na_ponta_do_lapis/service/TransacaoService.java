package com.npl.na_ponta_do_lapis.service;

import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.Marcador;
import com.npl.na_ponta_do_lapis.entity.TipoCategoria;
import com.npl.na_ponta_do_lapis.entity.Transacao;
import com.npl.na_ponta_do_lapis.entity.enums.EstadoTransacao;
import com.npl.na_ponta_do_lapis.entity.enums.TipoTransacao;
import com.npl.na_ponta_do_lapis.repository.TransacaoRepository;
import com.npl.na_ponta_do_lapis.service.fatura.parcelamento.ParcelamentoCalculator;
import com.npl.na_ponta_do_lapis.web.dto.TransacaoFaturaDTO;
import com.npl.na_ponta_do_lapis.web.dto.TransacaoRequestDTO;
import com.npl.na_ponta_do_lapis.web.exception.NotFoundException;
import com.npl.na_ponta_do_lapis.web.exception.TransacaoNaoExisteException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.npl.na_ponta_do_lapis.security.jwt.JwtAuthFilter.getEmailUsuarioLogado;

@Service
public class TransacaoService {
    private final TransacaoRepository transacaoRepository;
    private final UsuarioService usuarioService;
    private final MarcadorService marcadorService;

    private final TipoCategoraService tipoCategoriaService;

    private final ContaFinanceiraService contaFinanceiraService;

    public TransacaoService(TransacaoRepository transacaoRepository, TipoCategoraService tipoCategoriaService, ContaFinanceiraService contaFinanceiraService, UsuarioService usuarioService, MarcadorService marcadorService) {
        this.transacaoRepository = transacaoRepository;
        this.contaFinanceiraService = contaFinanceiraService;
        this.tipoCategoriaService = tipoCategoriaService;
        this.usuarioService = usuarioService;
        this.marcadorService = marcadorService;
    }

    /**
     * Cria uma transação. Quando {@code parcelado} é true, gera uma transação por parcela
     * (o valor informado é o total e a data é a da primeira parcela); caso contrário, gera uma única.
     */
    @Transactional
    public List<Transacao> criarTransacao(TransacaoRequestDTO transacao) throws AccessDeniedException {
        ContaFinanceira conta = contaFinanceiraService.buscarContaPorIdObject(transacao.idContaFinanceira());
        String email = getEmailUsuarioLogado();
        validarDono(conta, email);

        if (transacao.isCompraParcelada()) {
            return criarParcelas(transacao, conta, email);
        }

        Transacao novaTransacao = new Transacao();
        novaTransacao.setDescricao(transacao.descricao());
        novaTransacao.setValor(transacao.valor());
        novaTransacao.setTipo(transacao.tipo());

        LocalDateTime dataUsuario = transacao.dataHora();
        LocalTime horaAtual = LocalTime.now();
        novaTransacao.setDataHora(dataUsuario.with(horaAtual));

        // Agora o estado vem do DTO; sem estado explícito, a transação é realizada.
        novaTransacao.setEstado(transacao.estado() != null ? transacao.estado() : EstadoTransacao.REALIZADA);

        TipoCategoria categoria = tipoCategoriaService.buscarPorId(transacao.idCategoria());

        novaTransacao.setCategoria(categoria);
        novaTransacao.setContaFinanceira(conta);

        if (transacao.marcadorId() != null) {
            Marcador marcador = marcadorService.buscarMarcadorPorIdObject(transacao.marcadorId());
            validarDono(marcador, email);
            novaTransacao.setMarcador(marcador);
        }

        aplicarImpactoSaldo(novaTransacao);
        return List.of(transacaoRepository.save(novaTransacao));
    }

    @Transactional
    public List<Transacao> salvarFaturaEmLote(List<TransacaoFaturaDTO> dtos, Long contaId) throws AccessDeniedException {
        ContaFinanceira conta = contaFinanceiraService.buscarContaPorIdObject(contaId);
        validarDono(conta, getEmailUsuarioLogado());

        List<Transacao> transacoesParaSalvar = new ArrayList<>();
        for (TransacaoFaturaDTO dto : dtos) {
            if (dto == null || dto.descricao() == null || dto.descricao().isBlank()
                    || dto.valor() == null || dto.valor().signum() <= 0 || dto.data() == null) {
                throw new IllegalArgumentException("Transação da fatura com dados inválidos.");
            }

            Transacao transacao = new Transacao();
            transacao.setDescricao(dto.descricao().trim());
            transacao.setValor(dto.valor());
            transacao.setDataHora(dto.data().atStartOfDay());
            transacao.setTipo(TipoTransacao.DESPESA);
            transacao.setEstado(EstadoTransacao.REALIZADA);
            transacao.setContaFinanceira(conta);
            transacao.setCategoria(tipoCategoriaService.buscarPorNomeFlexivel(dto.categoria()));

            aplicarMetadadosParcelamento(transacao, dto);
            aplicarImpactoSaldo(transacao);
            transacoesParaSalvar.add(transacao);
        }

        return transacaoRepository.saveAll(transacoesParaSalvar);
    }

    private void aplicarMetadadosParcelamento(Transacao transacao, TransacaoFaturaDTO dto) {
        Integer numero = dto.numeroParcela();
        Integer total = dto.totalParcelas();
        if (numero == null || total == null || numero < 1 || total < 2 || numero > total || total > 60) {
            return;
        }

        BigDecimal valorTotalCompra = dto.valorTotalCompra() != null
                ? dto.valorTotalCompra()
                : dto.valor().multiply(BigDecimal.valueOf(total));

        transacao.setGrupoParcelamento(UUID.randomUUID().toString());
        transacao.setNumeroParcela(numero);
        transacao.setTotalParcelas(total);
        transacao.setValorTotalCompra(valorTotalCompra);
    }

    public List<Transacao> listarTransacoesUsuarioNaSessao() {
        return transacaoRepository.buscarTransacoesUsuarioLogado(getEmailUsuarioLogado());
    }

    public List<Transacao> buscarPorDescricao(String descricao) {
        return transacaoRepository.buscarPorDescricao(getEmailUsuarioLogado(), descricao);
    }

    public Transacao buscarPorId(Long id) {
        return transacaoRepository.findById(id)
                .orElseThrow(() -> new TransacaoNaoExisteException("Transação com ID:" + id + " não encontrada"));
    }

    @Transactional
    public Transacao atualizarTransacao(Long id, TransacaoRequestDTO dto) throws AccessDeniedException {
        Transacao transacaoExistente = buscarPorId(id);
        String emailUsuarioLogado = getEmailUsuarioLogado();

        ContaFinanceira contaFinanceiraAntiga = transacaoExistente.getContaFinanceira();
        validarDono(contaFinanceiraAntiga, emailUsuarioLogado);

        Marcador marcadorNovo = null;
        if (dto.marcadorId() != null) {
            marcadorNovo = marcadorService.buscarMarcadorPorIdObject(dto.marcadorId());
            validarDono(marcadorNovo, emailUsuarioLogado);
        }

        ContaFinanceira novaContaFinanceira = contaFinanceiraService.buscarContaPorIdObject(dto.idContaFinanceira());
        validarDono(novaContaFinanceira, emailUsuarioLogado);

        // Remove o impacto antigo apenas se a transação estava realizada.
        estornarImpactoSaldo(transacaoExistente);

        transacaoExistente.setDescricao(dto.descricao());
        transacaoExistente.setValor(dto.valor());
        transacaoExistente.setEstado(dto.estado() != null ? dto.estado() : transacaoExistente.getEstado());
        transacaoExistente.setTipo(dto.tipo());
        transacaoExistente.setDataHora(dto.dataHora());
        transacaoExistente.setMarcador(marcadorNovo);
        transacaoExistente.setCategoria(tipoCategoriaService.buscarPorId(dto.idCategoria()));
        transacaoExistente.setContaFinanceira(novaContaFinanceira);

        // Aplica o novo impacto somente se o novo estado for REALIZADA.
        aplicarImpactoSaldo(transacaoExistente);

        Transacao salva = transacaoRepository.save(transacaoExistente);

        // Transação avulsa marcada como parcelada: vira a parcela atual e gera as futuras.
        if (dto.isCompraParcelada() && salva.getGrupoParcelamento() == null) {
            converterEmParcelada(salva, dto);
            if (dto.isCriarAnteriores()) {
                criarParcelasAnteriores(salva);
            }
            gerarParcelasFuturas(salva.getGrupoParcelamento());
        }
        return salva;
    }

    private void converterEmParcelada(Transacao transacao, TransacaoRequestDTO dto) {
        Integer total = dto.totalParcelas();
        Integer numero = dto.numeroParcela();
        if (total == null || total < 2 || total > 60) {
            throw new IllegalArgumentException("Informe um número de parcelas entre 2 e 60.");
        }
        if (numero == null || numero < 1 || numero > total) {
            throw new IllegalArgumentException("A parcela atual deve estar entre 1 e o total de parcelas.");
        }

        BigDecimal valorTotal = dto.valorTotalCompra() != null
                ? dto.valorTotalCompra()
                : transacao.getValor().multiply(BigDecimal.valueOf(total));

        transacao.setGrupoParcelamento(UUID.randomUUID().toString());
        transacao.setNumeroParcela(numero);
        transacao.setTotalParcelas(total);
        transacao.setValorTotalCompra(valorTotal);
        transacaoRepository.save(transacao);
    }

    /**
     * Cria as parcelas 1..(atual-1) como REALIZADA, sem alterar o saldo: elas representam
     * pagamentos que já aconteceram antes desta transação existir no sistema.
     */
    private void criarParcelasAnteriores(Transacao atual) {
        int numeroAtual = atual.getNumeroParcela();
        if (numeroAtual <= 1) {
            return;
        }
        List<BigDecimal> valores = ParcelamentoCalculator.calcularValores(
                atual.getValorTotalCompra(), atual.getTotalParcelas());

        List<Transacao> anteriores = new ArrayList<>();
        for (int numero = 1; numero < numeroAtual; numero++) {
            Transacao parcela = new Transacao();
            parcela.setDescricao(atual.getDescricao());
            parcela.setValor(valores.get(numero - 1));
            parcela.setCategoria(atual.getCategoria());
            parcela.setTipo(atual.getTipo());
            parcela.setEstado(EstadoTransacao.REALIZADA);
            parcela.setDataHora(atual.getDataHora().minusMonths(numeroAtual - numero));
            parcela.setContaFinanceira(atual.getContaFinanceira());
            parcela.setMarcador(atual.getMarcador());
            parcela.setGrupoParcelamento(atual.getGrupoParcelamento());
            parcela.setNumeroParcela(numero);
            parcela.setTotalParcelas(atual.getTotalParcelas());
            parcela.setValorTotalCompra(atual.getValorTotalCompra());
            anteriores.add(parcela);
        }
        transacaoRepository.saveAll(anteriores);
    }

    @Transactional
    public void removerTransacao(Long id) throws AccessDeniedException {
        Transacao transacao = buscarPorId(id);
        ContaFinanceira conta = transacao.getContaFinanceira();

        validarDono(conta, getEmailUsuarioLogado());

        estornarImpactoSaldo(transacao);
        transacaoRepository.delete(transacao);
    }

    // -------------------------------------------------------------------------
    // Parcelamento
    // -------------------------------------------------------------------------

    private List<Transacao> criarParcelas(TransacaoRequestDTO dto, ContaFinanceira conta, String email)
            throws AccessDeniedException {
        Integer totalParcelas = dto.totalParcelas();
        if (totalParcelas == null || totalParcelas < 2 || totalParcelas > 60) {
            throw new IllegalArgumentException("Informe um número de parcelas entre 2 e 60.");
        }

        TipoCategoria categoria = tipoCategoriaService.buscarPorId(dto.idCategoria());

        Marcador marcador = null;
        if (dto.marcadorId() != null) {
            marcador = marcadorService.buscarMarcadorPorIdObject(dto.marcadorId());
            validarDono(marcador, email);
        }

        int numeroAtual = dto.numeroParcela() != null ? dto.numeroParcela() : 1;
        if (numeroAtual < 1 || numeroAtual > totalParcelas) {
            throw new IllegalArgumentException("A parcela atual deve estar entre 1 e o total de parcelas.");
        }

        String grupo = UUID.randomUUID().toString();
        List<BigDecimal> valores = ParcelamentoCalculator.calcularValores(dto.valor(), totalParcelas);
        // A data informada é a da parcela atual; as anteriores recuam um mês cada.
        LocalDateTime primeira = dto.dataHora().toLocalDate().atStartOfDay().minusMonths(numeroAtual - 1L);
        List<LocalDateTime> datas = ParcelamentoCalculator.calcularDatas(primeira, totalParcelas);
        LocalDateTime agora = LocalDateTime.now();

        List<Transacao> parcelas = new ArrayList<>();
        for (int indice = 0; indice < datas.size(); indice++) {
            boolean anterior = indice + 1 < numeroAtual;
            if (anterior && !dto.isCriarAnteriores()) {
                continue;
            }

            Transacao transacao = new Transacao();
            transacao.setDescricao(dto.descricao().trim());
            transacao.setValor(valores.get(indice));
            transacao.setCategoria(categoria);
            transacao.setTipo(dto.tipo());
            transacao.setContaFinanceira(conta);
            transacao.setMarcador(marcador);
            transacao.setDataHora(datas.get(indice));
            transacao.setEstado(datas.get(indice).isAfter(agora)
                    ? EstadoTransacao.PENDENTE
                    : EstadoTransacao.REALIZADA);
            transacao.setGrupoParcelamento(grupo);
            transacao.setNumeroParcela(indice + 1);
            transacao.setTotalParcelas(totalParcelas);
            transacao.setValorTotalCompra(dto.valor());
            parcelas.add(transacao);

            // Parcelas anteriores já foram pagas antes: entram no histórico sem alterar o saldo.
            if (!anterior) {
                aplicarImpactoSaldo(transacao);
            }
        }

        return transacaoRepository.saveAll(parcelas);
    }

    @Transactional
    public List<Transacao> listarParcelamento(String grupoId) throws AccessDeniedException {
        List<Transacao> parcelas = transacaoRepository.findByGrupoParcelamentoOrderByNumeroParcelaAsc(grupoId);
        if (parcelas.isEmpty()) {
            throw new NotFoundException("Parcelamento não encontrado.");
        }
        validarDono(parcelas.get(0).getContaFinanceira(), getEmailUsuarioLogado());
        return parcelas;
    }

    @Transactional
    public List<Transacao> gerarParcelasFuturas(String grupoId) throws AccessDeniedException {
        List<Transacao> existentes = transacaoRepository.findByGrupoParcelamentoOrderByNumeroParcelaAsc(grupoId);
        if (existentes.isEmpty()) {
            throw new NotFoundException("Parcelamento não encontrado.");
        }

        Transacao referencia = existentes.get(existentes.size() - 1);
        validarDono(referencia.getContaFinanceira(), getEmailUsuarioLogado());

        Integer totalParcelas = referencia.getTotalParcelas();
        Integer numeroAtual = referencia.getNumeroParcela();
        if (totalParcelas == null || numeroAtual == null || numeroAtual >= totalParcelas) {
            return List.of();
        }

        BigDecimal valorTotalCompra = referencia.getValorTotalCompra();
        if (valorTotalCompra == null) {
            valorTotalCompra = referencia.getValor().multiply(BigDecimal.valueOf(totalParcelas));
        }
        List<BigDecimal> valores = ParcelamentoCalculator.calcularValores(valorTotalCompra, totalParcelas);

        List<Transacao> novas = new ArrayList<>();
        for (int numero = numeroAtual + 1; numero <= totalParcelas; numero++) {
            Transacao transacao = new Transacao();
            transacao.setDescricao(referencia.getDescricao());
            transacao.setValor(valores.get(numero - 1));
            transacao.setCategoria(referencia.getCategoria());
            transacao.setEstado(EstadoTransacao.PENDENTE);
            transacao.setTipo(referencia.getTipo());
            transacao.setDataHora(referencia.getDataHora().plusMonths(numero - numeroAtual));
            transacao.setContaFinanceira(referencia.getContaFinanceira());
            transacao.setMarcador(referencia.getMarcador());
            transacao.setGrupoParcelamento(grupoId);
            transacao.setNumeroParcela(numero);
            transacao.setTotalParcelas(totalParcelas);
            transacao.setValorTotalCompra(valorTotalCompra);
            novas.add(transacao);
        }

        return transacaoRepository.saveAll(novas);
    }

    @Transactional
    public void excluirParcelamento(String grupoId, boolean apenasFuturas) throws AccessDeniedException {
        List<Transacao> parcelas = transacaoRepository.findByGrupoParcelamentoOrderByNumeroParcelaAsc(grupoId);
        if (parcelas.isEmpty()) {
            throw new NotFoundException("Parcelamento não encontrado.");
        }
        validarDono(parcelas.get(0).getContaFinanceira(), getEmailUsuarioLogado());

        List<Transacao> alvo = apenasFuturas
                ? parcelas.stream().filter(t -> t.getEstado() == EstadoTransacao.PENDENTE).toList()
                : parcelas;

        for (Transacao transacao : alvo) {
            estornarImpactoSaldo(transacao);
            transacaoRepository.delete(transacao);
        }
    }

    // -------------------------------------------------------------------------
    // Impacto no saldo: somente REALIZADA afeta o saldo.
    // -------------------------------------------------------------------------

    private void aplicarImpactoSaldo(Transacao transacao) {
        if (transacao.getEstado() != EstadoTransacao.REALIZADA
                || transacao.getContaFinanceira() == null
                || transacao.getValor() == null) {
            return;
        }

        ContaFinanceira conta = transacao.getContaFinanceira();
        if (transacao.getTipo() == TipoTransacao.DESPESA) {
            conta.setSaldo(conta.getSaldo().subtract(transacao.getValor()));
        } else {
            conta.setSaldo(conta.getSaldo().add(transacao.getValor()));
        }
    }

    private void estornarImpactoSaldo(Transacao transacao) {
        if (transacao.getEstado() != EstadoTransacao.REALIZADA
                || transacao.getContaFinanceira() == null
                || transacao.getValor() == null) {
            return;
        }

        ContaFinanceira conta = transacao.getContaFinanceira();
        if (transacao.getTipo() == TipoTransacao.DESPESA) {
            conta.setSaldo(conta.getSaldo().add(transacao.getValor()));
        } else {
            conta.setSaldo(conta.getSaldo().subtract(transacao.getValor()));
        }
    }

    private void validarDono(ContaFinanceira conta, String email) throws AccessDeniedException {
        if (conta == null || conta.getUsuario() == null || email == null
                || !email.equals(conta.getUsuario().getEmail())) {
            throw new AccessDeniedException("Você não tem permissão para operar nessa conta financeira.");
        }
    }

    private void validarDono(Marcador marcador, String email) throws AccessDeniedException {
        if (marcador == null || marcador.getUsuario() == null || email == null
                || !email.equals(marcador.getUsuario().getEmail())) {
            throw new AccessDeniedException("Você não tem permissão para usar esse marcador.");
        }
    }
}
