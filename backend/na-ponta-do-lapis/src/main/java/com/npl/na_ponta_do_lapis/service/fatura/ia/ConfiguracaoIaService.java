package com.npl.na_ponta_do_lapis.service.fatura.ia;

import com.fasterxml.jackson.databind.JsonNode;
import com.npl.na_ponta_do_lapis.entity.AdminAuditLog;
import com.npl.na_ponta_do_lapis.entity.ChaveIaUsuario;
import com.npl.na_ponta_do_lapis.entity.ConfiguracaoIaProvedor;
import com.npl.na_ponta_do_lapis.entity.ExecucaoIaLog;
import com.npl.na_ponta_do_lapis.entity.Notificacao;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.Papel;
import com.npl.na_ponta_do_lapis.entity.enums.TipoNotificacao;
import com.npl.na_ponta_do_lapis.repository.AdminAuditLogRepository;
import com.npl.na_ponta_do_lapis.repository.ChaveIaUsuarioRepository;
import com.npl.na_ponta_do_lapis.repository.ConfiguracaoIaProvedorRepository;
import com.npl.na_ponta_do_lapis.repository.ExecucaoIaLogRepository;
import com.npl.na_ponta_do_lapis.repository.NotificacaoRepository;
import com.npl.na_ponta_do_lapis.repository.UsuarioRepository;
import com.npl.na_ponta_do_lapis.service.crypto.CryptoService;
import com.npl.na_ponta_do_lapis.service.fatura.FaturaAnaliseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerencia modelos ativos por provedor de IA (banco com fallback para variáveis de ambiente),
 * consulta modelos válidos diretamente na API de cada provedor, registra telemetria de sucesso
 * (leitura de faturas e Consultor Grafith) e dispara alertas quando um modelo é descontinuado.
 */
@Service
public class ConfiguracaoIaService {

    private static final Logger log = LoggerFactory.getLogger(ConfiguracaoIaService.class);

    public record ModeloDisponivelDTO(String id, String nomeExibicao) {}

    public record MetricaFluxoIaDTO(
            long total,
            long sucessos,
            long falhas,
            double taxaSucesso,
            long tempoMedioMs
    ) {}

    public record ProvedorIaConfigDTO(
            String provedor,
            String nomeExibicao,
            String modeloAtivo,
            String modeloPadraoEnv,
            boolean customizadoNoBanco,
            LocalDateTime atualizadoEm,
            String atualizadoPor,
            boolean alertaModeloDescontinuado,
            String ultimoErroModelo,
            LocalDateTime ultimoErroEm,
            long totalExecucoes30d,
            long sucessos30d,
            double taxaSucesso30d,
            boolean adminPossuiChaveSalva,
            List<ModeloDisponivelDTO> modelosDisponiveis,
            LocalDateTime modelosConsultadosEm
    ) {}

    public record AdminIaPainelDTO(
            int periodoDias,
            MetricaFluxoIaDTO faturas,
            MetricaFluxoIaDTO grafith,
            int alertasAtivos,
            List<ProvedorIaConfigDTO> provedores
    ) {}

    private final ConfiguracaoIaProvedorRepository configRepository;
    private final ExecucaoIaLogRepository execucaoRepository;
    private final ChaveIaUsuarioRepository chaveRepository;
    private final CryptoService cryptoService;
    private final IaHttpClient iaHttpClient;
    private final AdminAuditLogRepository auditLogRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoRepository notificacaoRepository;

    private record CacheListaModelos(List<ModeloDisponivelDTO> modelos, LocalDateTime consultadoEm) {}

    private static final long CACHE_LISTA_TTL_MINUTOS = 30;
    private final Map<ProvedorIa, String> cacheModelos = new ConcurrentHashMap<>();
    private final Map<ProvedorIa, CacheListaModelos> cacheListaModelos = new ConcurrentHashMap<>();

    @Value("${fatura.ia.gemini.model:gemini-2.5-flash}")
    private String envGeminiModel;

    @Value("${fatura.ia.claude.model:claude-sonnet-4-5}")
    private String envClaudeModel;

    @Value("${fatura.ia.openai.model:gpt-4.1-mini}")
    private String envOpenAiModel;

    @Value("${fatura.ia.deepseek.model:deepseek-chat}")
    private String envDeepSeekModel;

    @Value("${fatura.ia.groq.model:llama-3.3-70b-versatile}")
    private String envGroqModel;

    public ConfiguracaoIaService(
            ConfiguracaoIaProvedorRepository configRepository,
            ExecucaoIaLogRepository execucaoRepository,
            ChaveIaUsuarioRepository chaveRepository,
            CryptoService cryptoService,
            IaHttpClient iaHttpClient,
            AdminAuditLogRepository auditLogRepository,
            UsuarioRepository usuarioRepository,
            NotificacaoRepository notificacaoRepository
    ) {
        this.configRepository = configRepository;
        this.execucaoRepository = execucaoRepository;
        this.chaveRepository = chaveRepository;
        this.cryptoService = cryptoService;
        this.iaHttpClient = iaHttpClient;
        this.auditLogRepository = auditLogRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacaoRepository = notificacaoRepository;
    }

    public String obterModeloAtivo(ProvedorIa provedor) {
        if (provedor == null) {
            return envGeminiModel;
        }
        String emCache = cacheModelos.get(provedor);
        if (emCache != null && !emCache.isBlank()) {
            return emCache;
        }
        String resolvido = configRepository.findById(provedor)
                .map(ConfiguracaoIaProvedor::getModeloAtivo)
                .filter(m -> m != null && !m.isBlank())
                .orElseGet(() -> obterModeloPadraoEnv(provedor));
        cacheModelos.put(provedor, resolvido);
        return resolvido;
    }

    public String obterModeloPadraoEnv(ProvedorIa provedor) {
        return switch (provedor) {
            case GEMINI -> envGeminiModel;
            case CLAUDE -> envClaudeModel;
            case OPENAI -> envOpenAiModel;
            case DEEPSEEK -> envDeepSeekModel;
            case GROQ -> envGroqModel;
        };
    }

    @Transactional
    public ProvedorIaConfigDTO atualizarModelo(ProvedorIa provedor, String novoModelo, Usuario admin, String ip) {
        String modeloLimpo = novoModelo == null ? "" : novoModelo.trim();
        if (modeloLimpo.isEmpty() || modeloLimpo.length() > 120) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um identificador de modelo válido.");
        }
        if (modeloLimpo.startsWith("models/")) {
            modeloLimpo = modeloLimpo.substring("models/".length());
        }
        final String modeloFinal = modeloLimpo;

        String anterior = obterModeloAtivo(provedor);
        ConfiguracaoIaProvedor entidade = configRepository.findById(provedor)
                .orElseGet(() -> new ConfiguracaoIaProvedor(provedor, modeloFinal));

        entidade.setModeloAtivo(modeloFinal);
        entidade.setAtualizadoEm(LocalDateTime.now());
        entidade.setAtualizadoPor(admin != null ? admin.getEmail() : "sistema");
        entidade.setAlertaModeloDescontinuado(false);
        entidade.setUltimoErroModelo(null);
        entidade.setUltimoErroEm(null);
        configRepository.save(entidade);

        cacheModelos.put(provedor, modeloLimpo);

        if (admin != null) {
            auditLogRepository.save(new AdminAuditLog(
                    admin.getId(),
                    admin.getEmail(),
                    "IA_MODELO_ATUALIZADO",
                    "CONFIGURACAO_IA",
                    provedor.name(),
                    provedor.getNomeExibicao(),
                    "Modelo alterado de " + anterior + " para " + modeloLimpo,
                    null,
                    ip
            ));
        }

        return montarResumoProvedor(provedor, entidade, List.of(), admin);
    }

    @Transactional(readOnly = true)
    public AdminIaPainelDTO obterPainelAdmin(int dias, Usuario admin) {
        int periodo = Math.max(1, Math.min(dias, 90));
        LocalDateTime desde = LocalDateTime.now().minusDays(periodo);
        List<ExecucaoIaLog> logs = execucaoRepository.findByCriadoEmAfter(desde);

        MetricaFluxoIaDTO metricasFatura = calcularMetricasFluxo(logs, ExecucaoIaLog.TipoExecucao.FATURA);
        MetricaFluxoIaDTO metricasGrafith = calcularMetricasFluxo(logs, ExecucaoIaLog.TipoExecucao.GRAFITH);

        Map<ProvedorIa, ConfiguracaoIaProvedor> configs = new EnumMap<>(ProvedorIa.class);
        for (ConfiguracaoIaProvedor cfg : configRepository.findAll()) {
            configs.put(cfg.getProvedor(), cfg);
        }

        List<ProvedorIaConfigDTO> listaProvedores = new ArrayList<>();
        int alertas = 0;
        for (ProvedorIa p : ProvedorIa.values()) {
            ConfiguracaoIaProvedor cfg = configs.get(p);
            List<ExecucaoIaLog> logsDoProvedor = logs.stream().filter(l -> l.getProvedor() == p).toList();
            ProvedorIaConfigDTO dto = montarResumoProvedor(p, cfg, logsDoProvedor, admin);
            if (dto.alertaModeloDescontinuado()) {
                alertas++;
            }
            listaProvedores.add(dto);
        }

        return new AdminIaPainelDTO(periodo, metricasFatura, metricasGrafith, alertas, listaProvedores);
    }

    /**
     * Consulta diretamente a API oficial do provedor para listar modelos válidos.
     * Usa a apiKey informada ou, se vazia, a chave já salva pelo admin em Perfil > IA.
     */
    public List<ModeloDisponivelDTO> listarModelosDisponiveis(ProvedorIa provedor, Usuario admin, String apiKeyOpcional) {
        String apiKey = resolverChaveParaConsulta(provedor, admin, apiKeyOpcional);

        JsonNode json = switch (provedor) {
            case GEMINI -> iaHttpClient.get(
                    "https://generativelanguage.googleapis.com/v1beta/models?pageSize=100",
                    Map.of("x-goog-api-key", apiKey)
            );
            case CLAUDE -> iaHttpClient.get(
                    "https://api.anthropic.com/v1/models",
                    Map.of("x-api-key", apiKey, "anthropic-version", "2023-06-01")
            );
            case OPENAI -> iaHttpClient.get(
                    "https://api.openai.com/v1/models",
                    Map.of("Authorization", "Bearer " + apiKey)
            );
            case DEEPSEEK -> iaHttpClient.get(
                    "https://api.deepseek.com/v1/models",
                    Map.of("Authorization", "Bearer " + apiKey)
            );
            case GROQ -> iaHttpClient.get(
                    "https://api.groq.com/openai/v1/models",
                    Map.of("Authorization", "Bearer " + apiKey)
            );
        };

        List<ModeloDisponivelDTO> modelos = extrairModelos(provedor, json);
        cacheListaModelos.put(provedor, new CacheListaModelos(modelos, LocalDateTime.now()));
        return modelos;
    }

    private CacheListaModelos obterCacheListaValido(ProvedorIa provedor) {
        CacheListaModelos entry = cacheListaModelos.get(provedor);
        if (entry == null) {
            return null;
        }
        if (entry.consultadoEm().isBefore(LocalDateTime.now().minusMinutes(CACHE_LISTA_TTL_MINUTOS))) {
            cacheListaModelos.remove(provedor);
            return null;
        }
        return entry;
    }

    /**
     * Registra telemetria de cada chamada a IA (Fatura ou Grafith).
     * Nunca propaga erro para não interferir no fluxo do usuário.
     */
    @Transactional
    public void registrarExecucao(
            ExecucaoIaLog.TipoExecucao tipo,
            ProvedorIa provedor,
            boolean sucesso,
            Integer httpStatus,
            String codigoErro,
            String mensagemErro,
            long duracaoMs
    ) {
        if (provedor == null || tipo == null) {
            return;
        }
        try {
            String modelo = obterModeloAtivo(provedor);
            String msgLimpa = mensagemErro != null && mensagemErro.length() > 400
                    ? mensagemErro.substring(0, 400)
                    : mensagemErro;

            execucaoRepository.save(new ExecucaoIaLog(
                    tipo,
                    provedor,
                    modelo,
                    sucesso,
                    httpStatus,
                    codigoErro,
                    msgLimpa,
                    duracaoMs
            ));

            if (!sucesso && ehErroModeloDescontinuado(httpStatus, codigoErro, msgLimpa)) {
                registrarAlertaModeloDescontinuado(provedor, modelo, msgLimpa);
            } else if (sucesso) {
                configRepository.findById(provedor).ifPresent(cfg -> {
                    if (cfg.isAlertaModeloDescontinuado()) {
                        cfg.setAlertaModeloDescontinuado(false);
                        cfg.setUltimoErroModelo(null);
                        cfg.setUltimoErroEm(null);
                        configRepository.save(cfg);
                    }
                });
            }
        } catch (Exception ex) {
            log.warn("Falha ao registrar telemetria de IA: {}", ex.getMessage());
        }
    }

    public static boolean ehErroModeloDescontinuado(Integer httpStatus, String codigoErro, String mensagem) {
        if ("MODELO_DESCONTINUADO".equals(codigoErro)) {
            return true;
        }
        if (httpStatus != null && httpStatus == 404) {
            return true;
        }
        if (mensagem == null) {
            return false;
        }
        String lower = mensagem.toLowerCase(Locale.ROOT);
        return lower.contains("no longer available")
                || lower.contains("deprecated")
                || lower.contains("model_not_found")
                || lower.contains("not found for api version")
                || lower.contains("does not exist");
    }

    private void registrarAlertaModeloDescontinuado(ProvedorIa provedor, String modelo, String detalheErro) {
        ConfiguracaoIaProvedor cfg = configRepository.findById(provedor)
                .orElseGet(() -> new ConfiguracaoIaProvedor(provedor, modelo));

        LocalDateTime agora = LocalDateTime.now();
        cfg.setAlertaModeloDescontinuado(true);
        cfg.setUltimoErroModelo(detalheErro != null ? detalheErro : "Modelo retornou HTTP 404 ou foi descontinuado.");
        cfg.setUltimoErroEm(agora);

        boolean deveNotificar = cfg.getUltimoAlertaNotificadoEm() == null
                || cfg.getUltimoAlertaNotificadoEm().isBefore(agora.minusHours(24));

        if (deveNotificar) {
            cfg.setUltimoAlertaNotificadoEm(agora);
            notificarAdminsModeloDescontinuado(provedor, modelo);
        }
        configRepository.save(cfg);
    }

    private void notificarAdminsModeloDescontinuado(ProvedorIa provedor, String modelo) {
        try {
            List<Usuario> admins = usuarioRepository.findByPapelIn(List.of(Papel.ADMIN_SITE));
            String titulo = "Modelo de IA indisponível (" + provedor.getNomeExibicao() + ")";
            String mensagem = "O modelo \"" + modelo + "\" retornou erro de descontinuação/404. "
                    + "Acesse Painel Admin > Catálogos & IA > Modelos de IA para selecionar um modelo válido.";

            for (Usuario admin : admins) {
                if (admin.isAtivo()) {
                    notificacaoRepository.save(new Notificacao(
                            admin,
                            titulo,
                            mensagem,
                            TipoNotificacao.SISTEMA,
                            "/admin/catalogos?aba=ia"
                    ));
                }
            }
        } catch (Exception ex) {
            log.warn("Não foi possível enviar notificação de modelo descontinuado aos admins: {}", ex.getMessage());
        }
    }

    private String resolverChaveParaConsulta(ProvedorIa provedor, Usuario admin, String apiKeyOpcional) {
        if (apiKeyOpcional != null && !apiKeyOpcional.isBlank()) {
            return apiKeyOpcional.trim();
        }
        if (admin != null) {
            Optional<ChaveIaUsuario> salva = chaveRepository.findByUsuarioIdOrderByCriadoEmDesc(admin.getId())
                    .stream()
                    .filter(c -> c.getProvedor() == provedor)
                    .sorted(Comparator.comparing(ChaveIaUsuario::isPadrao).reversed())
                    .findFirst();
            if (salva.isPresent()) {
                return cryptoService.decrypt(salva.get().getChaveCriptografada());
            }
        }
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Cadastre uma chave de " + provedor.getNomeExibicao() + " em Perfil > IA ou informe uma chave no campo ao lado para consultar os modelos."
        );
    }

    private List<ModeloDisponivelDTO> extrairModelos(ProvedorIa provedor, JsonNode json) {
        List<ModeloDisponivelDTO> modelos = new ArrayList<>();
        if (provedor == ProvedorIa.GEMINI) {
            JsonNode arr = json.path("models");
            if (arr.isArray()) {
                for (JsonNode m : arr) {
                    boolean suportaGeracao = false;
                    JsonNode metodos = m.path("supportedGenerationMethods");
                    if (metodos.isArray()) {
                        for (JsonNode metodo : metodos) {
                            if ("generateContent".equals(metodo.asText())) {
                                suportaGeracao = true;
                                break;
                            }
                        }
                    }
                    if (!suportaGeracao) continue;

                    String id = m.path("name").asText("");
                    if (id.startsWith("models/")) {
                        id = id.substring("models/".length());
                    }
                    String display = m.path("displayName").asText(id);
                    if (id.isBlank() || !modeloAdequadoParaProposito(ProvedorIa.GEMINI, id, display)) continue;
                    modelos.add(new ModeloDisponivelDTO(id, display));
                }
            }
            modelos.sort(Comparator.comparing(ModeloDisponivelDTO::id).reversed());
            return modelos;
        }

        JsonNode data = json.path("data");
        if (data.isArray()) {
            for (JsonNode m : data) {
                String id = m.path("id").asText("");
                String display = m.path("display_name").asText(id);
                if (id.isBlank() || !modeloAdequadoParaProposito(provedor, id, display)) continue;
                modelos.add(new ModeloDisponivelDTO(id, display));
            }
        }
        modelos.sort(Comparator.comparing(ModeloDisponivelDTO::id));
        return modelos;
    }

    /**
     * Filtra apenas modelos adequados para chat financeiro + extração estruturada de faturas,
     * descartando modelos de áudio/TTS, imagem/vídeo (incl. Nano Banana / Imagen / Veo),
     * transcrição (Whisper), moderação (Guard) e endpoints legados incompatíveis.
     */
    private boolean modeloAdequadoParaProposito(ProvedorIa provedor, String id, String displayName) {
        String lower = id.toLowerCase(Locale.ROOT);
        String displayLower = displayName != null ? displayName.toLowerCase(Locale.ROOT) : "";
        String combinado = lower + " " + displayLower;

        if (combinado.contains("embedding") || combinado.contains("tts") || combinado.contains("audio")
                || combinado.contains("image") || combinado.contains("imagen") || combinado.contains("veo")
                || combinado.contains("banana") || combinado.contains("video")
                || combinado.contains("whisper") || combinado.contains("realtime") || combinado.contains("transcribe")
                || combinado.contains("diarize") || combinado.contains("moderation") || combinado.contains("guard")
                || combinado.contains("orpheus") || combinado.contains("playai")) {
            return false;
        }
        return switch (provedor) {
            case GEMINI -> lower.startsWith("gemini-")
                    && !lower.contains("live")
                    && !combinado.contains("robotics")
                    && !combinado.contains("computer-use")
                    && !combinado.contains("computer use")
                    && !combinado.contains("deep-research")
                    && !combinado.contains("deep research")
                    && !lower.contains("customtools");
            case CLAUDE -> lower.startsWith("claude-");
            case OPENAI -> (lower.startsWith("gpt-") || lower.startsWith("o1") || lower.startsWith("o3")
                    || lower.startsWith("o4") || lower.startsWith("chatgpt-"))
                    && !lower.contains("instruct")
                    && !lower.contains("search")
                    && !lower.contains("codex");
            case DEEPSEEK -> lower.startsWith("deepseek-");
            case GROQ -> !lower.contains("allam");
        };
    }

    private MetricaFluxoIaDTO calcularMetricasFluxo(List<ExecucaoIaLog> logs, ExecucaoIaLog.TipoExecucao tipo) {
        List<ExecucaoIaLog> filtrados = logs.stream().filter(l -> l.getTipo() == tipo).toList();
        long total = filtrados.size();
        long sucessos = filtrados.stream().filter(ExecucaoIaLog::isSucesso).count();
        long falhas = total - sucessos;
        double taxa = total == 0 ? 100.0 : Math.round((sucessos * 1000.0) / total) / 10.0;
        long tempoMedio = total == 0 ? 0L : Math.round(
                filtrados.stream()
                        .map(ExecucaoIaLog::getDuracaoMs)
                        .filter(d -> d != null && d >= 0)
                        .mapToLong(Long::longValue)
                        .average()
                        .orElse(0.0)
        );
        return new MetricaFluxoIaDTO(total, sucessos, falhas, taxa, tempoMedio);
    }

    private ProvedorIaConfigDTO montarResumoProvedor(
            ProvedorIa provedor,
            ConfiguracaoIaProvedor cfg,
            List<ExecucaoIaLog> logsProvedor,
            Usuario admin
    ) {
        String envDefault = obterModeloPadraoEnv(provedor);
        String ativo = (cfg != null && cfg.getModeloAtivo() != null && !cfg.getModeloAtivo().isBlank())
                ? cfg.getModeloAtivo()
                : envDefault;
        long total = logsProvedor.size();
        long sucessos = logsProvedor.stream().filter(ExecucaoIaLog::isSucesso).count();
        double taxa = total == 0 ? 100.0 : Math.round((sucessos * 1000.0) / total) / 10.0;

        boolean adminTemChave = admin != null && chaveRepository.findByUsuarioIdOrderByCriadoEmDesc(admin.getId())
                .stream()
                .anyMatch(c -> c.getProvedor() == provedor);

        CacheListaModelos cacheLista = obterCacheListaValido(provedor);
        return new ProvedorIaConfigDTO(
                provedor.name(),
                provedor.getNomeExibicao(),
                ativo,
                envDefault,
                cfg != null && cfg.getAtualizadoEm() != null,
                cfg != null ? cfg.getAtualizadoEm() : null,
                cfg != null ? cfg.getAtualizadoPor() : null,
                cfg != null && cfg.isAlertaModeloDescontinuado(),
                cfg != null ? cfg.getUltimoErroModelo() : null,
                cfg != null ? cfg.getUltimoErroEm() : null,
                total,
                sucessos,
                taxa,
                adminTemChave,
                cacheLista != null ? cacheLista.modelos() : List.of(),
                cacheLista != null ? cacheLista.consultadoEm() : null
        );
    }
}

