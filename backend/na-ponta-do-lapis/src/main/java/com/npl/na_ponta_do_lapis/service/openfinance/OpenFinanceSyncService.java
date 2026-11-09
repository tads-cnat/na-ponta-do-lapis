package com.npl.na_ponta_do_lapis.service.openfinance;

import com.fasterxml.jackson.databind.JsonNode;
import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.TipoCategoria;
import com.npl.na_ponta_do_lapis.entity.Transacao;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.UsuarioPluggyIntegracao;
import com.npl.na_ponta_do_lapis.entity.enums.EstadoTransacao;
import com.npl.na_ponta_do_lapis.entity.enums.Moeda;
import com.npl.na_ponta_do_lapis.entity.enums.TipoConta;
import com.npl.na_ponta_do_lapis.entity.enums.TipoTransacao;
import com.npl.na_ponta_do_lapis.repository.ContaFinanceiraRepository;
import com.npl.na_ponta_do_lapis.repository.TransacaoRepository;
import com.npl.na_ponta_do_lapis.repository.UsuarioPluggyIntegracaoRepository;
import com.npl.na_ponta_do_lapis.service.crypto.CryptoService;
import com.npl.na_ponta_do_lapis.web.dto.OpenFinanceConectarRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.OpenFinanceItemDTO;
import com.npl.na_ponta_do_lapis.web.dto.OpenFinanceStatusResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OpenFinanceSyncService {

    private static final Logger log = LoggerFactory.getLogger(OpenFinanceSyncService.class);
    private static final int LIMITE_MANUAL_DIARIO = 5;

    private static final Pattern PADRAO_PARCELA_FINAL = Pattern.compile("(?i)(?:^|[\\s\\(\\[\\-])(?:parcela\\s+|parc\\.?\\s*)?(\\d{1,2})\\s*[/]\\s*(\\d{1,2})\\s*[\\)\\]]?\\s*$");
    private static final Pattern PADRAO_PARCELA_TEXTO = Pattern.compile("(?i)(?:parcela|parc\\.?)\\s*(\\d{1,2})\\s*(?:de|[/])\\s*(\\d{1,2})");
    private static final Pattern PADRAO_PARCELA_PARENTESES = Pattern.compile("(?i)[\\(\\[]\\s*(\\d{1,2})\\s*[/]\\s*(\\d{1,2})\\s*[\\)\\]]");

    private record DadosParcelamento(
            Integer numeroParcela,
            Integer totalParcelas,
            BigDecimal valorTotalCompra,
            String grupoParcelamento
    ) {}

    private final UsuarioPluggyIntegracaoRepository integracaoRepository;
    private final ContaFinanceiraRepository contaRepository;
    private final TransacaoRepository transacaoRepository;
    private final PluggyClient pluggyClient;
    private final PluggyCategoriaMapper categoriaMapper;
    private final CryptoService cryptoService;

    public OpenFinanceSyncService(UsuarioPluggyIntegracaoRepository integracaoRepository,
                                  ContaFinanceiraRepository contaRepository,
                                  TransacaoRepository transacaoRepository,
                                  PluggyClient pluggyClient,
                                  PluggyCategoriaMapper categoriaMapper,
                                  CryptoService cryptoService) {
        this.integracaoRepository = integracaoRepository;
        this.contaRepository = contaRepository;
        this.transacaoRepository = transacaoRepository;
        this.pluggyClient = pluggyClient;
        this.categoriaMapper = categoriaMapper;
        this.cryptoService = cryptoService;
    }

    @Transactional(readOnly = true)
    public OpenFinanceStatusResponseDTO obterStatus(Usuario usuario) {
        Optional<UsuarioPluggyIntegracao> opt = integracaoRepository.findByUsuarioId(usuario.getId());
        if (opt.isEmpty()) {
            return new OpenFinanceStatusResponseDTO(false, null, LIMITE_MANUAL_DIARIO, LIMITE_MANUAL_DIARIO);
        }

        UsuarioPluggyIntegracao integracao = opt.get();
        int restantes = calcularRestantesHoje(integracao);
        String maskedClient = obterClientIdMascarado(integracao);
        int totalContas = contaRepository.countByUsuarioIdAndSincronizadaTrue(usuario.getId());
        return new OpenFinanceStatusResponseDTO(true, integracao.getUltimaSincronizacao(), restantes, LIMITE_MANUAL_DIARIO, maskedClient, totalContas);
    }

    @Transactional(readOnly = true)
    public String gerarConnectToken(Usuario usuario) {
        UsuarioPluggyIntegracao integracao = integracaoRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        "Nenhuma conta da Pluggy configurada. Conecte com Client ID e Secret primeiro."));

        String clientId = cryptoService.decrypt(integracao.getClientIdCriptografado());
        String clientSecret = cryptoService.decrypt(integracao.getClientSecretCriptografado());
        String apiKey = pluggyClient.autenticar(clientId, clientSecret);
        return pluggyClient.criarConnectToken(apiKey);
    }

    @Transactional
    public OpenFinanceStatusResponseDTO conectar(Usuario usuario, OpenFinanceConectarRequestDTO dto) {
        String clientId = dto.clientId().trim();
        String clientSecret = dto.clientSecret().trim();

        // 1. Testa autenticação na Pluggy antes de persistir
        pluggyClient.autenticar(clientId, clientSecret);

        // 2. Criptografa e salva ou atualiza integração
        UsuarioPluggyIntegracao integracao = integracaoRepository.findByUsuarioId(usuario.getId())
                .orElse(new UsuarioPluggyIntegracao());

        integracao.setUsuario(usuario);
        integracao.setClientIdCriptografado(cryptoService.encrypt(clientId));
        integracao.setClientSecretCriptografado(cryptoService.encrypt(clientSecret));
        integracao.setDataContadorManual(LocalDate.now());
        integracao.setSincronizacoesManuaisHoje(0);

        if (dto.itemIds() != null && !dto.itemIds().isBlank()) {
            String[] ids = dto.itemIds().split("[,;\\s]+");
            for (String id : ids) {
                if (!id.isBlank()) {
                    integracao.adicionarItemId(id.trim());
                }
            }
        }

        integracaoRepository.save(integracao);

        // 3. Executa a primeira sincronização inicial (janela de 30 dias)
        try {
            return executarSincronizacao(integracao, false);
        } catch (Exception e) {
            log.warn("[OpenFinanceSyncService] Conexão Pluggy salva com sucesso, mas a primeira sincronização falhou ou ainda não há contas vinculadas: {}", e.getMessage());
            int restantes = calcularRestantesHoje(integracao);
            String maskedClient = obterClientIdMascarado(integracao);
            int totalContas = contaRepository.countByUsuarioIdAndSincronizadaTrue(usuario.getId());
            return new OpenFinanceStatusResponseDTO(true, integracao.getUltimaSincronizacao(), restantes, LIMITE_MANUAL_DIARIO, maskedClient, totalContas);
        }
    }

    @Transactional
    public void desconectar(Usuario usuario) {
        integracaoRepository.findByUsuarioId(usuario.getId()).ifPresent(integracaoRepository::delete);
    }

    @Transactional
    public OpenFinanceStatusResponseDTO adicionarItem(Usuario usuario, String itemId) {
        if (itemId == null || itemId.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "O Item ID é obrigatório");
        }

        UsuarioPluggyIntegracao integracao = integracaoRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST, "Conecte sua conta Pluggy antes de adicionar um item."));

        String cleanId = itemId.trim();
        String clientId = cryptoService.decrypt(integracao.getClientIdCriptografado());
        String clientSecret = cryptoService.decrypt(integracao.getClientSecretCriptografado());
        String apiKey = pluggyClient.autenticar(clientId, clientSecret);

        // Valida se o item existe na Pluggy
        try {
            pluggyClient.buscarItem(apiKey, cleanId);
        } catch (Exception e) {
            log.error("[OpenFinanceSyncService] Item ID inválido ou não encontrado na Pluggy: {}", cleanId, e);
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Item ID '" + cleanId + "' não encontrado ou inválido na sua conta Pluggy. Verifique no painel do Meu Pluggy.");
        }

        integracao.adicionarItemId(cleanId);
        integracaoRepository.save(integracao);

        return executarSincronizacao(integracao, false);
    }

    @Transactional
    public void removerItem(Usuario usuario, String itemId) {
        UsuarioPluggyIntegracao integracao = integracaoRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST, "Nenhuma integração Pluggy encontrada."));

        integracao.removerItemId(itemId);
        integracaoRepository.save(integracao);
    }

    @Transactional(readOnly = true)
    public List<OpenFinanceItemDTO> listarItensConectados(Usuario usuario) {
        Optional<UsuarioPluggyIntegracao> opt = integracaoRepository.findByUsuarioId(usuario.getId());
        if (opt.isEmpty()) {
            return Collections.emptyList();
        }

        UsuarioPluggyIntegracao integracao = opt.get();
        Set<String> itemIds = integracao.getItemIds();
        if (itemIds.isEmpty()) {
            return Collections.emptyList();
        }

        String clientId = cryptoService.decrypt(integracao.getClientIdCriptografado());
        String clientSecret = cryptoService.decrypt(integracao.getClientSecretCriptografado());
        String apiKey = pluggyClient.autenticar(clientId, clientSecret);

        List<OpenFinanceItemDTO> resultado = new ArrayList<>();
        for (String itemId : itemIds) {
            try {
                JsonNode itemNode = pluggyClient.buscarItem(apiKey, itemId);
                String nomeInst = itemNode.path("connector").path("name").asText("Instituição Bancária");
                String logoUrl = itemNode.path("connector").path("imageUrl").asText("");
                String status = itemNode.path("status").asText("UPDATED");
                String updatedAt = itemNode.path("lastUpdatedAt").asText(itemNode.path("updatedAt").asText(""));

                if (nomeInst.equalsIgnoreCase("MeuPluggy") || nomeInst.equalsIgnoreCase("Pluggy")) {
                    try {
                        JsonNode accs = pluggyClient.listarContasPorItem(apiKey, itemId);
                        String bancoDetectado = detectarBancoPorContas(accs);
                        if (bancoDetectado != null) {
                            nomeInst = bancoDetectado;
                        }
                    } catch (Exception ignored) {}
                }

                resultado.add(new OpenFinanceItemDTO(itemId, nomeInst, logoUrl, status, updatedAt));
            } catch (Exception e) {
                log.warn("[OpenFinanceSyncService] Não foi possível consultar detalhes do item {}: {}", itemId, e.getMessage());
                resultado.add(new OpenFinanceItemDTO(itemId, "Instituição Bancária (" + itemId.substring(0, Math.min(8, itemId.length())) + "...)", "", "UPDATED", ""));
            }
        }
        return resultado;
    }

    @Transactional
    public OpenFinanceStatusResponseDTO sincronizarManual(Usuario usuario) {
        UsuarioPluggyIntegracao integracao = integracaoRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new IllegalArgumentException("Nenhuma conta do Open Finance conectada. Conecte primeiro."));

        resetarContadorSeNecessario(integracao);

        if (integracao.getSincronizacoesManuaisHoje() >= LIMITE_MANUAL_DIARIO) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.TOO_MANY_REQUESTS,
                    "Você atingiu o limite de " + LIMITE_MANUAL_DIARIO + " sincronizações manuais por hoje. Suas contas serão sincronizadas automaticamente de madrugada.");
        }

        integracao.setSincronizacoesManuaisHoje(integracao.getSincronizacoesManuaisHoje() + 1);
        return executarSincronizacao(integracao, true);
    }

    @Transactional
    public void sincronizarTodosUsuariosAutomatico() {
        List<UsuarioPluggyIntegracao> integracoes = integracaoRepository.findAllByClientIdCriptografadoIsNotNull();
        log.info("[OpenFinanceSyncService] Iniciando sincronização noturna automática para {} usuário(s)", integracoes.size());

        for (UsuarioPluggyIntegracao integracao : integracoes) {
            try {
                executarSincronizacao(integracao, false);
            } catch (Exception e) {
                log.error("[OpenFinanceSyncService] Erro ao sincronizar automaticamente para usuário id={}", integracao.getUsuario().getId(), e);
            }
        }
    }

    private OpenFinanceStatusResponseDTO executarSincronizacao(UsuarioPluggyIntegracao integracao, boolean manual) {
        Usuario usuario = integracao.getUsuario();
        String clientId = cryptoService.decrypt(integracao.getClientIdCriptografado());
        String clientSecret = cryptoService.decrypt(integracao.getClientSecretCriptografado());

        String apiKey = pluggyClient.autenticar(clientId, clientSecret);

        Set<String> itemIds = new LinkedHashSet<>(integracao.getItemIds());

        // Se ainda não temos itemIds salvos, tenta obter de /items caso a conta Pluggy suporte
        if (itemIds.isEmpty()) {
            try {
                JsonNode itemsNode = pluggyClient.listarItems(apiKey);
                if (itemsNode != null && itemsNode.isArray()) {
                    for (JsonNode itemNode : itemsNode) {
                        String id = itemNode.path("id").asText();
                        if (id != null && !id.isBlank()) {
                            itemIds.add(id);
                            integracao.adicionarItemId(id);
                        }
                    }
                    if (!itemIds.isEmpty()) {
                        integracaoRepository.save(integracao);
                    }
                }
            } catch (Exception e) {
                log.warn("[OpenFinanceSyncService] GET /items não habilitado ou retornou erro: {}", e.getMessage());
            }
        }

        List<ContaFinanceira> contasSincronizadas = new ArrayList<>();

        for (String itemId : itemIds) {
            String nomeInstituicao = "Instituição Bancária";
            String corInstituicao = null;
            try {
                JsonNode itemNode = pluggyClient.buscarItem(apiKey, itemId);
                JsonNode connector = itemNode.path("connector");
                nomeInstituicao = connector.path("name").asText("Instituição Bancária");
                String primaryColor = connector.path("primaryColor").asText("");
                if (primaryColor != null && !primaryColor.isBlank()) {
                    corInstituicao = primaryColor.startsWith("#") ? primaryColor : "#" + primaryColor;
                }
            } catch (Exception e) {
                log.warn("[OpenFinanceSyncService] Não foi possível consultar detalhes do item {}: {}", itemId, e.getMessage());
            }

            try {
                JsonNode accountsNode = pluggyClient.listarContasPorItem(apiKey, itemId);

                // Se o conector veio como MeuPluggy ou Pluggy ou cor vermelha da Pluggy, descobre o banco real pelas contas do item
                if (nomeInstituicao.equalsIgnoreCase("MeuPluggy") || nomeInstituicao.equalsIgnoreCase("Pluggy") || "#ef294b".equalsIgnoreCase(corInstituicao)) {
                    String bancoDetectado = detectarBancoPorContas(accountsNode);
                    if (bancoDetectado != null) {
                        nomeInstituicao = bancoDetectado;
                        corInstituicao = determinarCorInstituicao(bancoDetectado, itemId);
                    }
                }

                if (accountsNode != null && accountsNode.isArray()) {
                    for (JsonNode accNode : accountsNode) {
                        String pluggyAccountId = accNode.path("id").asText();
                        String rawName = accNode.path("name").asText("Conta Bancária");
                        BigDecimal balance = BigDecimal.valueOf(
                                accNode.hasNonNull("balance") ? accNode.path("balance").asDouble(0.0) :
                                (accNode.hasNonNull("availableBalance") ? accNode.path("availableBalance").asDouble(0.0) : 0.0)
                        );

                        // Se a instituição do item ainda era genérica ou MeuPluggy, detecta pelo texto da conta
                        String instConta = nomeInstituicao;
                        String corConta = corInstituicao;
                        if (instConta.equalsIgnoreCase("MeuPluggy") || instConta.equalsIgnoreCase("Pluggy") || instConta.equalsIgnoreCase("Instituição Bancária") || "#ef294b".equalsIgnoreCase(corConta)) {
                            String b = detectarBancoPorTexto(rawName);
                            if (b != null) {
                                instConta = b;
                                corConta = determinarCorInstituicao(b, pluggyAccountId);
                            }
                        }

                        String finalCor = (corConta != null && !corConta.isBlank() && !"#ef294b".equalsIgnoreCase(corConta))
                                ? corConta
                                : determinarCorInstituicao(instConta, pluggyAccountId);
                        String nomeAmigavelBase = formatarNomeContaAmigavel(instConta, rawName);

                        ContaFinanceira conta = contaRepository.findByPluggyAccountIdAndUsuarioId(pluggyAccountId, usuario.getId())
                                .orElseGet(() -> {
                                    ContaFinanceira nova = new ContaFinanceira();
                                    nova.setUsuario(usuario);
                                    nova.setNome(gerarNomeContaUnico(nomeAmigavelBase, usuario.getEmail(), null));
                                    nova.setCor(finalCor);
                                    nova.setMoeda(Moeda.BRL);
                                    nova.setSincronizada(true);
                                    nova.setPluggyAccountId(pluggyAccountId);
                                    return nova;
                                });

                        // Atualiza a cor para a cor oficial correta da instituição
                        conta.setCor(finalCor);

                        // Define o tipo de conta (Cartão de Crédito vs Conta Corrente)
                        String rawType = accNode.path("type").asText("").trim().toUpperCase();
                        String rawSubtype = accNode.path("subtype").asText("").trim().toUpperCase();
                        boolean ehCartao = "CREDIT".equals(rawType) || "CREDIT_CARD".equals(rawSubtype);
                        conta.setTipoConta(ehCartao ? TipoConta.CARTAO_CREDITO : TipoConta.CONTA_CORRENTE);

                        // Extrai limites de cartão se presentes em creditData
                        JsonNode creditData = accNode.path("creditData");
                        if (creditData.isObject()) {
                            if (creditData.hasNonNull("creditLimit")) {
                                conta.setLimiteTotal(BigDecimal.valueOf(creditData.path("creditLimit").asDouble()));
                            }
                            if (creditData.hasNonNull("availableCreditLimit")) {
                                conta.setLimiteDisponivel(BigDecimal.valueOf(creditData.path("availableCreditLimit").asDouble()));
                            }
                        }

                        // Se for cartão de crédito, consulta as faturas (/bills) para discriminar fatura fechada e fatura aberta
                        if (ehCartao) {
                            try {
                                JsonNode billsNode = pluggyClient.listarFaturasPorConta(apiKey, pluggyAccountId);
                                BigDecimal fechada = BigDecimal.ZERO;
                                BigDecimal aberta = BigDecimal.ZERO;
                                LocalDate vencimentoFechada = null;
                                LocalDate hoje = LocalDate.now();

                                if (billsNode != null && billsNode.isArray() && !billsNode.isEmpty()) {
                                    for (JsonNode bill : billsNode) {
                                        double bTotal = Math.abs(bill.path("totalAmount").asDouble(0.0));
                                        if (bTotal <= 0.001) {
                                            continue;
                                        }

                                        // Calcula total já pago nesta fatura
                                        double totalPago = 0.0;
                                        JsonNode paymentsNode = bill.path("payments");
                                        if (paymentsNode.isArray()) {
                                            for (JsonNode p : paymentsNode) {
                                                totalPago += Math.abs(p.path("amount").asDouble(0.0));
                                            }
                                        }

                                        // Se a fatura já foi totalmente paga, desconsidera
                                        if (totalPago >= (bTotal - 0.05)) {
                                            continue;
                                        }

                                        double saldoRestanteFatura = Math.max(0.0, bTotal - totalPago);

                                        String bClosing = bill.path("billClosingDate").asText("");
                                        String bDue = bill.path("dueDate").asText("");
                                        LocalDate closingDate = null;
                                        LocalDate dueDate = null;

                                        try {
                                            if (bClosing.length() >= 10) {
                                                closingDate = LocalDate.parse(bClosing.substring(0, 10));
                                            }
                                        } catch (Exception ignored) {}

                                        try {
                                            if (bDue.length() >= 10) {
                                                dueDate = LocalDate.parse(bDue.substring(0, 10));
                                            }
                                        } catch (Exception ignored) {}

                                        boolean isFechada = false;
                                        if (closingDate != null) {
                                            isFechada = !closingDate.isAfter(hoje);
                                        } else if (dueDate != null) {
                                            isFechada = !dueDate.isAfter(hoje.plusDays(35));
                                        } else {
                                            isFechada = true;
                                        }

                                        if (isFechada) {
                                            fechada = fechada.add(BigDecimal.valueOf(saldoRestanteFatura));
                                            if (vencimentoFechada == null && dueDate != null) {
                                                vencimentoFechada = dueDate;
                                            }
                                        } else {
                                            aberta = aberta.add(BigDecimal.valueOf(saldoRestanteFatura));
                                        }
                                    }
                                }

                                // Se não obteve vencimento pelas faturas, tenta balanceDueDate do creditData
                                if (vencimentoFechada == null && creditData.isObject() && creditData.hasNonNull("balanceDueDate")) {
                                    String bDue = creditData.path("balanceDueDate").asText("");
                                    if (bDue.length() >= 10) {
                                        try {
                                            vencimentoFechada = LocalDate.parse(bDue.substring(0, 10));
                                        } catch (Exception ignored) {}
                                    }
                                }

                                BigDecimal saldoPositivo = balance.abs();

                                // Se o saldo total do cartão for maior que a fatura fechada pendente,
                                // o excedente corresponde aos lançamentos da fatura em aberto (próximo ciclo)
                                if (saldoPositivo.compareTo(fechada) > 0) {
                                    BigDecimal diferenca = saldoPositivo.subtract(fechada);
                                    if (diferenca.compareTo(aberta) > 0) {
                                        aberta = diferenca;
                                    }
                                } else if (fechada.compareTo(BigDecimal.ZERO) == 0 && saldoPositivo.compareTo(BigDecimal.ZERO) > 0) {
                                    // Se todas as faturas fechadas já foram pagas, todo o saldo em aberto vai para a próxima fatura
                                    aberta = saldoPositivo;
                                } else if (fechada.compareTo(saldoPositivo) > 0 && saldoPositivo.compareTo(BigDecimal.ZERO) > 0) {
                                    // Se a fatura fechada for maior que o saldo (ex: pagamento antecipado no banco)
                                    fechada = saldoPositivo;
                                    aberta = BigDecimal.ZERO;
                                }

                                conta.setValorFaturaFechada(fechada.setScale(2, RoundingMode.HALF_UP));
                                conta.setValorFaturaAberta(aberta.setScale(2, RoundingMode.HALF_UP));
                                conta.setDataVencimentoFatura(vencimentoFechada);
                                log.info("[OpenFinanceSyncService] Cartão {}: faturaFechada={}, vencimento={}, faturaAberta={}, saldoTotal={}",
                                        conta.getNome(), conta.getValorFaturaFechada(), conta.getDataVencimentoFatura(), conta.getValorFaturaAberta(), conta.getSaldo());
                            } catch (Exception ex) {
                                log.warn("[OpenFinanceSyncService] Aviso ao buscar faturas detalhadas para o cartão {}: {}", pluggyAccountId, ex.getMessage());
                            }
                        }


                        // Atualiza o nome se continha MeuPluggy, sufixo genérico ou nome bruto
                        if (conta.getNome() == null || conta.getNome().contains("MeuPluggy") || conta.getNome().contains("(Open Finance)") || conta.getNome().equalsIgnoreCase(rawName)) {
                            conta.setNome(gerarNomeContaUnico(nomeAmigavelBase, usuario.getEmail(), conta.getId()));
                        }

                        conta.setSaldo(balance);
                        contaRepository.save(conta);
                        contasSincronizadas.add(conta);
                    }
                }
            } catch (Exception e) {
                log.error("[OpenFinanceSyncService] Erro ao buscar contas para o itemId={}: {}", itemId, e.getMessage());
            }
        }

        // Janela de datas: 30 dias se for 1ª vez; 7 dias nas rotinas seguintes
        LocalDate de = integracao.getUltimaSincronizacao() == null
                ? LocalDate.now().minusDays(30)
                : LocalDate.now().minusDays(7);
        LocalDate ate = LocalDate.now();

        for (ContaFinanceira conta : contasSincronizadas) {
            try {
                JsonNode txArray = pluggyClient.listarTransacoes(apiKey, conta.getPluggyAccountId(), de, ate);
                if (txArray != null && txArray.isArray()) {
                    for (JsonNode txNode : txArray) {
                        processarTransacaoPluggy(txNode, conta);
                    }
                }
            } catch (Exception e) {
                log.error("[OpenFinanceSyncService] Erro ao sincronizar transações para a conta '{}' (id={}): {}",
                        conta.getNome(), conta.getPluggyAccountId(), e.getMessage());
            }
        }

        integracao.setUltimaSincronizacao(LocalDateTime.now());
        integracaoRepository.save(integracao);

        int restantes = calcularRestantesHoje(integracao);
        String maskedClient = obterClientIdMascarado(integracao);
        int totalContas = contaRepository.countByUsuarioIdAndSincronizadaTrue(usuario.getId());
        return new OpenFinanceStatusResponseDTO(true, integracao.getUltimaSincronizacao(), restantes, LIMITE_MANUAL_DIARIO, maskedClient, totalContas);
    }

    private void processarTransacaoPluggy(JsonNode txNode, ContaFinanceira conta) {
        String pluggyTxId = txNode.path("id").asText();
        if (pluggyTxId == null || pluggyTxId.isBlank()) {
            return;
        }

        double amount = txNode.path("amount").asDouble(0.0);
        String description = txNode.path("description").asText("");
        if (description.isBlank()) {
            description = txNode.path("descriptionRaw").asText("Transação Bancária");
        }

        JsonNode catNode = txNode.path("category");
        String category = catNode.isObject() ? catNode.path("name").asText("") : catNode.asText("");

        String dateStr = txNode.path("date").asText("");
        String statusStr = txNode.path("status").asText("POSTED");

        TipoTransacao tipo = determinarTipoTransacao(txNode, conta, amount);
        BigDecimal valor = BigDecimal.valueOf(Math.abs(amount));
        DadosParcelamento parcelamento = extrairDadosParcelamento(txNode, conta, description, valor);

        // Se já existe, atualiza estado, tipo e dados de parcelamento (se ainda não vinculados)
        Optional<Transacao> txExistente = transacaoRepository.findByPluggyTransactionId(pluggyTxId);
        if (txExistente.isPresent()) {
            Transacao t = txExistente.get();
            boolean mudou = false;
            if (t.getTipo() != tipo) {
                t.setTipo(tipo);
                mudou = true;
            }
            if (statusStr.equalsIgnoreCase("POSTED") && t.getEstado() == EstadoTransacao.PENDENTE) {
                t.setEstado(EstadoTransacao.REALIZADA);
                mudou = true;
            }
            if (parcelamento != null && (t.getNumeroParcela() == null || t.getGrupoParcelamento() == null)) {
                t.setNumeroParcela(parcelamento.numeroParcela());
                t.setTotalParcelas(parcelamento.totalParcelas());
                t.setValorTotalCompra(parcelamento.valorTotalCompra());
                t.setGrupoParcelamento(parcelamento.grupoParcelamento());
                mudou = true;
            }
            if (mudou) {
                transacaoRepository.save(t);
            }
            return;
        }

        // Nova transação
        EstadoTransacao estado = statusStr.equalsIgnoreCase("PENDING") ? EstadoTransacao.PENDENTE : EstadoTransacao.REALIZADA;

        LocalDateTime dataHora = parseData(dateStr);
        TipoCategoria categoria = categoriaMapper.mapearCategoria(category, description);

        Transacao novaTx = new Transacao();
        novaTx.setDescricao(description);
        novaTx.setValor(valor);
        novaTx.setTipo(tipo);
        novaTx.setEstado(estado);
        novaTx.setDataHora(dataHora);
        novaTx.setCategoria(categoria);
        novaTx.setContaFinanceira(conta);
        novaTx.setPluggyTransactionId(pluggyTxId);

        if (parcelamento != null) {
            novaTx.setNumeroParcela(parcelamento.numeroParcela());
            novaTx.setTotalParcelas(parcelamento.totalParcelas());
            novaTx.setValorTotalCompra(parcelamento.valorTotalCompra());
            novaTx.setGrupoParcelamento(parcelamento.grupoParcelamento());
        }

        transacaoRepository.save(novaTx);
    }

    private DadosParcelamento extrairDadosParcelamento(JsonNode txNode, ContaFinanceira conta, String description, BigDecimal valorParcela) {
        Integer numero = null;
        Integer total = null;
        BigDecimal valorTotal = null;

        // 1. Tenta metadados oficiais do cartão de crédito da Pluggy
        JsonNode ccMeta = txNode.path("creditCardMetadata");
        if (ccMeta.isObject()) {
            int n = ccMeta.path("installmentNumber").asInt(0);
            int t = ccMeta.path("totalInstallments").asInt(0);
            double totalAmt = ccMeta.path("totalAmount").asDouble(0.0);
            if (n >= 1 && t >= 2 && n <= t && t <= 60) {
                numero = n;
                total = t;
                if (totalAmt > 0) {
                    valorTotal = BigDecimal.valueOf(totalAmt);
                }
            }
        }

        // 2. Se não veio em creditCardMetadata, analisa a descrição (e descriptionRaw)
        if (numero == null || total == null) {
            String textoBusca = description != null ? description : "";
            Matcher m = PADRAO_PARCELA_FINAL.matcher(textoBusca);
            if (!m.find()) {
                m = PADRAO_PARCELA_PARENTESES.matcher(textoBusca);
            }
            if (!m.find()) {
                m = PADRAO_PARCELA_TEXTO.matcher(textoBusca);
            }
            if (!m.find() && txNode.hasNonNull("descriptionRaw")) {
                textoBusca = txNode.path("descriptionRaw").asText("");
                m = PADRAO_PARCELA_FINAL.matcher(textoBusca);
                if (!m.find()) {
                    m = PADRAO_PARCELA_PARENTESES.matcher(textoBusca);
                }
                if (!m.find()) {
                    m = PADRAO_PARCELA_TEXTO.matcher(textoBusca);
                }
            }

            if (m.find()) {
                try {
                    int n = Integer.parseInt(m.group(1));
                    int t = Integer.parseInt(m.group(2));
                    if (n >= 1 && t >= 2 && n <= t && t <= 60) {
                        numero = n;
                        total = t;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        if (numero == null || total == null) {
            return null;
        }

        if (valorTotal == null && valorParcela != null) {
            valorTotal = valorParcela.multiply(BigDecimal.valueOf(total));
        }

        // Limpa a descrição para encontrar o nome base da compra
        String descBase = description != null
                ? description.replaceAll("(?i)\\s*(?:parcela|parc\\.?)?\\s*[\\(\\[]?\\s*\\d{1,2}\\s*[/|de]\\s*\\d{1,2}\\s*[\\)\\]]?\\s*$", "")
                        .trim()
                        .toLowerCase(Locale.ROOT)
                : "";
        if (descBase.isBlank()) {
            descBase = description != null ? description.trim().toLowerCase(Locale.ROOT) : "";
        }

        // UUID determinístico baseado na conta, nome base e total de parcelas
        String seed = (conta != null ? conta.getId() : "0") + ":" + descBase + ":" + total;
        String grupoParcelamento = UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8)).toString();

        return new DadosParcelamento(numero, total, valorTotal, grupoParcelamento);
    }

    private TipoTransacao determinarTipoTransacao(JsonNode txNode, ContaFinanceira conta, double amount) {
        String explicitType = txNode.path("type").asText("").trim().toUpperCase();
        if ("DEBIT".equals(explicitType)) {
            return TipoTransacao.DESPESA;
        }
        if ("CREDIT".equals(explicitType)) {
            return TipoTransacao.RECEITA;
        }

        boolean ehCartao = conta.getTipoConta() == TipoConta.CARTAO_CREDITO
                || txNode.hasNonNull("billId")
                || txNode.hasNonNull("creditCardMetadata");

        if (ehCartao) {
            // Em cartão de crédito: valor positivo é compra (despesa), valor negativo é pagamento/estorno (receita)
            return amount > 0 ? TipoTransacao.DESPESA : TipoTransacao.RECEITA;
        } else {
            // Em conta corrente: valor negativo é saída (despesa), valor positivo é entrada (receita)
            return amount < 0 ? TipoTransacao.DESPESA : TipoTransacao.RECEITA;
        }
    }

    private LocalDateTime parseData(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return OffsetDateTime.parse(dateStr).toLocalDateTime();
        } catch (Exception e) {
            try {
                return LocalDate.parse(dateStr).atStartOfDay();
            } catch (Exception ex) {
                return LocalDateTime.now();
            }
        }
    }

    private String determinarCorInstituicao(String nomeInstituicao, String fallbackSeed) {
        if (nomeInstituicao != null) {
            String lower = nomeInstituicao.toLowerCase();
            if (lower.contains("nubank") || lower.contains("nu pagamentos")) {
                return "#820AD1";
            }
            if (lower.contains("inter")) {
                return "#FF7A00";
            }
            if (lower.contains("itaú") || lower.contains("itau")) {
                return "#EC7000";
            }
            if (lower.contains("bradesco")) {
                return "#CC092F";
            }
            if (lower.contains("santander")) {
                return "#EC0000";
            }
            if (lower.contains("brasil") || lower.contains("bb")) {
                return "#0038A8";
            }
            if (lower.contains("caixa")) {
                return "#005CA9";
            }
            if (lower.contains("c6")) {
                return "#1F1F1F";
            }
            if (lower.contains("btg")) {
                return "#0F172A";
            }
            if (lower.contains("picpay")) {
                return "#11C76F";
            }
            if (lower.contains("mercado pago") || lower.contains("mercadopago")) {
                return "#009EE3";
            }
            if (lower.contains("sicredi")) {
                return "#006633";
            }
            if (lower.contains("sicoob")) {
                return "#003641";
            }
            if (lower.contains("safra")) {
                return "#0B1F41";
            }
        }

        String[] paleta = new String[]{
                "#4F46E5", "#059669", "#D97706", "#DC2626", "#7C3AED",
                "#DB2777", "#2563EB", "#0891B2", "#475569", "#EA580C"
        };
        int hash = Math.abs((fallbackSeed != null ? fallbackSeed : (nomeInstituicao != null ? nomeInstituicao : "default")).hashCode());
        return paleta[hash % paleta.length];
    }

    private String formatarNomeContaAmigavel(String nomeInstituicao, String rawName) {
        String instLimpa = nomeInstituicao;
        if (instLimpa == null || instLimpa.isBlank() || instLimpa.equalsIgnoreCase("MeuPluggy") || instLimpa.equalsIgnoreCase("Pluggy") || instLimpa.equalsIgnoreCase("Instituição Bancária")) {
            String b = detectarBancoPorTexto(rawName);
            instLimpa = b != null ? b : "Conta Bancária";
        }

        if (instLimpa.toLowerCase().contains("nu pagamentos") || instLimpa.toLowerCase().contains("nubank")) {
            instLimpa = "Nubank";
        } else if (instLimpa.toLowerCase().contains("inter")) {
            instLimpa = "Banco Inter";
        }

        if (rawName == null || rawName.isBlank() || rawName.equalsIgnoreCase(instLimpa) || rawName.equalsIgnoreCase("MeuPluggy")) {
            return instLimpa + " - Conta";
        }

        String semPrefixo = rawName.replaceAll("(?i)MeuPluggy\\s*-\\s*", "")
                .replaceAll("(?i)BANCO INTER", "")
                .replaceAll("(?i)Nu Pagamentos S\\.A\\. - Instituição de Pagamento", "")
                .trim();

        String rLower = rawName.toLowerCase();
        if (rLower.contains("gold") || rLower.contains("platinum") || rLower.contains("black") || rLower.contains("infinite") || rLower.contains("cartao") || rLower.contains("cartão") || rLower.contains("credit")) {
            String complemento = capitalizar(semPrefixo.isBlank() ? "Gold" : semPrefixo);
            return instLimpa + " - Cartão " + complemento;
        }

        if (rLower.contains("conta") || rLower.contains("checking") || rLower.contains("pagamento") || semPrefixo.isBlank()) {
            return instLimpa + " - Conta";
        }

        return instLimpa + " - " + capitalizar(semPrefixo);
    }

    private String detectarBancoPorTexto(String texto) {
        if (texto == null || texto.isBlank()) return null;
        String t = texto.toLowerCase();
        if (t.contains("inter")) return "Banco Inter";
        if (t.contains("nu ") || t.contains("nubank") || t.contains("nu pagamentos")) return "Nubank";
        if (t.contains("itaú") || t.contains("itau")) return "Itaú";
        if (t.contains("bradesco")) return "Bradesco";
        if (t.contains("santander")) return "Santander";
        if (t.contains("brasil") || t.contains("bb")) return "Banco do Brasil";
        if (t.contains("caixa")) return "Caixa";
        if (t.contains("c6")) return "C6 Bank";
        if (t.contains("picpay")) return "PicPay";
        if (t.contains("mercado pago") || t.contains("mercadopago")) return "Mercado Pago";
        if (t.contains("sicredi")) return "Sicredi";
        if (t.contains("sicoob")) return "Sicoob";
        return null;
    }

    private String detectarBancoPorContas(JsonNode accountsNode) {
        if (accountsNode == null || !accountsNode.isArray()) {
            return null;
        }
        for (JsonNode acc : accountsNode) {
            String b = detectarBancoPorTexto(acc.path("name").asText("") + " " + acc.path("marketingName").asText(""));
            if (b != null) return b;
        }
        return null;
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isBlank()) return "";
        String trimmed = texto.trim();
        if (trimmed.length() <= 1) return trimmed.toUpperCase();
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1).toLowerCase();
    }

    private String gerarNomeContaUnico(String base, String email, Long contaIdIgnorar) {
        String tentativa = base;
        int contador = 1;
        List<ContaFinanceira> contasDoUsuario = contaRepository.buscarContaFinanceiraUsuarioLogado(email);

        while (true) {
            final String nomeAtual = tentativa;
            boolean existe = contasDoUsuario.stream()
                    .filter(c -> contaIdIgnorar == null || !c.getId().equals(contaIdIgnorar))
                    .anyMatch(c -> c.getNome().equalsIgnoreCase(nomeAtual));
            if (!existe) {
                return tentativa;
            }
            contador++;
            tentativa = base + " #" + contador;
        }
    }

    private void resetarContadorSeNecessario(UsuarioPluggyIntegracao integracao) {
        if (integracao.getDataContadorManual() == null || !integracao.getDataContadorManual().isEqual(LocalDate.now())) {
            integracao.setDataContadorManual(LocalDate.now());
            integracao.setSincronizacoesManuaisHoje(0);
        }
    }

    private int calcularRestantesHoje(UsuarioPluggyIntegracao integracao) {
        resetarContadorSeNecessario(integracao);
        return Math.max(0, LIMITE_MANUAL_DIARIO - integracao.getSincronizacoesManuaisHoje());
    }

    private String obterClientIdMascarado(UsuarioPluggyIntegracao integracao) {
        try {
            if (integracao.getClientIdCriptografado() == null) {
                return null;
            }
            String rawClient = cryptoService.decrypt(integracao.getClientIdCriptografado());
            return cryptoService.mask(rawClient);
        } catch (Exception e) {
            return null;
        }
    }
}
