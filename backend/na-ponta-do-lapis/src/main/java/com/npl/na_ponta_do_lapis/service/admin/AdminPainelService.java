package com.npl.na_ponta_do_lapis.service.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.npl.na_ponta_do_lapis.entity.*;
import com.npl.na_ponta_do_lapis.entity.enums.Papel;
import com.npl.na_ponta_do_lapis.entity.enums.StatusConvite;
import com.npl.na_ponta_do_lapis.entity.enums.TipoNotificacao;
import com.npl.na_ponta_do_lapis.repository.*;
import com.npl.na_ponta_do_lapis.security.UsuarioAuthCacheService;
import com.npl.na_ponta_do_lapis.service.UsuarioService;
import com.npl.na_ponta_do_lapis.web.dto.admin.AdminDTOs.*;
import com.npl.na_ponta_do_lapis.web.exception.BusinessException;
import com.npl.na_ponta_do_lapis.web.exception.UsuarioIdNaoExisteException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AdminPainelService {

    private final UsuarioRepository usuarioRepository;
    private final FamiliaRepository familiaRepository;
    private final TipoCategoriaRepository tipoCategoriaRepository;
    private final ConquistaRepository conquistaRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final UsuarioAuthCacheService usuarioAuthCacheService;
    private final UsuarioService usuarioService;
    private final ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager entityManager;

    public AdminPainelService(
            UsuarioRepository usuarioRepository,
            FamiliaRepository familiaRepository,
            TipoCategoriaRepository tipoCategoriaRepository,
            ConquistaRepository conquistaRepository,
            NotificacaoRepository notificacaoRepository,
            AdminAuditLogRepository auditLogRepository,
            UsuarioAuthCacheService usuarioAuthCacheService,
            UsuarioService usuarioService,
            ObjectMapper objectMapper
    ) {
        this.usuarioRepository = usuarioRepository;
        this.familiaRepository = familiaRepository;
        this.tipoCategoriaRepository = tipoCategoriaRepository;
        this.conquistaRepository = conquistaRepository;
        this.notificacaoRepository = notificacaoRepository;
        this.auditLogRepository = auditLogRepository;
        this.usuarioAuthCacheService = usuarioAuthCacheService;
        this.usuarioService = usuarioService;
        this.objectMapper = objectMapper;
    }

    // -------------------------------------------------------------------------
    // Dashboard Executivo
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public AdminDashboardResumoDTO obterResumoDashboard(int diasSolicitados) {
        int periodoDias = (diasSolicitados == 7 || diasSolicitados == 90) ? diasSolicitados : 30;
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime ha7Dias = agora.minusDays(7);
        LocalDateTime ha30Dias = agora.minusDays(30);
        LocalDateTime inicioPeriodo = agora.minusDays(periodoDias);
        LocalDateTime inicioPeriodoAnterior = agora.minusDays(periodoDias * 2L);

        long totalUsuarios = usuarioRepository.count();
        long usuariosAtivos = usuarioRepository.contarAtivos();
        long usuariosSuspensos = usuarioRepository.contarSuspensos();
        long ativosVerificados = usuarioRepository.contarAtivosVerificados();
        long ativosNaoVerificados = usuarioRepository.contarAtivosNaoVerificados();
        long novos7 = usuarioRepository.contarCriadosDesde(ha7Dias);
        long novos30 = usuarioRepository.contarCriadosDesde(ha30Dias);
        long novosNoPeriodo = usuarioRepository.contarCriadosDesde(inicioPeriodo);
        long novosPeriodoAnterior = usuarioRepository.contarCriadosEntre(inicioPeriodoAnterior, inicioPeriodo);
        long emailsVerificados = usuarioRepository.contarEmailsVerificados();
        long usuariosAtivados = queryCount(
                "SELECT COUNT(u) FROM Usuario u WHERE EXISTS (SELECT 1 FROM Transacao t WHERE t.contaFinanceira.usuario.id = u.id) OR EXISTS (SELECT 1 FROM Meta m WHERE m.usuario.id = u.id)",
                Map.of()
        );
        long totalFamilias = familiaRepository.count();

        long convitesPendentes = queryCount(
                "SELECT COUNT(c) FROM Convite c WHERE c.status = :st",
                Map.of("st", StatusConvite.PENDENTE)
        );
        long usuariosComIa = queryCount(
                "SELECT COUNT(DISTINCT c.usuario.id) FROM ChaveIaUsuario c",
                Map.of()
        );
        long usuariosComOpenFinance = queryCount(
                "SELECT COUNT(DISTINCT p.usuario.id) FROM UsuarioPluggyIntegracao p",
                Map.of()
        );
        long totalTransacoes = queryCount("SELECT COUNT(t) FROM Transacao t", Map.of());
        long totalMetas = queryCount("SELECT COUNT(m) FROM Meta m", Map.of());

        // 1. Taxa de Ativação (24h): usuários com criadoEm que criaram 1 transação ou 1 meta em até 24h
        @SuppressWarnings("unchecked")
        List<Object[]> usuariosComData = entityManager.createQuery(
                        "SELECT u.id, COALESCE(u.criadoEm, u.termosAceitosEm) FROM Usuario u WHERE u.criadoEm IS NOT NULL OR u.termosAceitosEm IS NOT NULL")
                .getResultList();

        Map<Long, LocalDateTime> primeiroEventoTx = buscarPrimeiraDataPorUsuario(
                "SELECT t.contaFinanceira.usuario.id, MIN(COALESCE(t.criadoEm, t.dataHora)) FROM Transacao t GROUP BY t.contaFinanceira.usuario.id");
        Map<Long, LocalDateTime> ultimoEventoTx = buscarPrimeiraDataPorUsuario(
                "SELECT t.contaFinanceira.usuario.id, MAX(COALESCE(t.criadoEm, t.dataHora)) FROM Transacao t GROUP BY t.contaFinanceira.usuario.id");
        Map<Long, LocalDateTime> ultimoAporte = buscarPrimeiraDataPorUsuario(
                "SELECT a.usuario.id, MAX(a.dataAporte) FROM AporteMeta a GROUP BY a.usuario.id");

        @SuppressWarnings("unchecked")
        List<Object[]> primeirasMetas = entityManager.createQuery(
                        "SELECT m.usuario.id, MIN(m.dataInicio) FROM Meta m GROUP BY m.usuario.id")
                .getResultList();
        Map<Long, LocalDate> primeiraMetaPorUsuario = new HashMap<>();
        for (Object[] row : primeirasMetas) {
            if (row[0] != null && row[1] != null) {
                primeiraMetaPorUsuario.put((Long) row[0], (LocalDate) row[1]);
            }
        }

        @SuppressWarnings("unchecked")
        List<Object[]> ultimosAcessos = entityManager.createQuery(
                        "SELECT u.id, u.ultimoAcessoEm FROM Usuario u WHERE u.ultimoAcessoEm IS NOT NULL")
                .getResultList();
        Map<Long, LocalDateTime> ultimoAcessoPorUsuario = new HashMap<>();
        for (Object[] row : ultimosAcessos) {
            ultimoAcessoPorUsuario.put((Long) row[0], (LocalDateTime) row[1]);
        }

        long usuariosBaseAtivacao24h = usuariosComData.size();
        long usuariosAtivados24h = 0;
        long usuariosElegiveisD7 = 0;
        long usuariosRetidosD7 = 0;
        long usuariosElegiveisM2 = 0;
        long usuariosRetidosM2 = 0;

        for (Object[] row : usuariosComData) {
            Long uid = (Long) row[0];
            LocalDateTime cadastro = (LocalDateTime) row[1];
            if (cadastro == null) continue;

            LocalDateTime limite24h = cadastro.plusHours(24);
            LocalDateTime primTx = primeiroEventoTx.get(uid);
            LocalDate primMeta = primeiraMetaPorUsuario.get(uid);

            boolean ativouTx24h = primTx != null && !primTx.isAfter(limite24h);
            boolean ativouMeta24h = primMeta != null && !primMeta.isAfter(limite24h.toLocalDate());
            if (ativouTx24h || ativouMeta24h) {
                usuariosAtivados24h++;
            }

            LocalDateTime ultTx = ultimoEventoTx.get(uid);
            LocalDateTime ultAp = ultimoAporte.get(uid);
            LocalDateTime ultAc = ultimoAcessoPorUsuario.get(uid);

            if (!cadastro.isAfter(ha7Dias)) {
                usuariosElegiveisD7++;
                LocalDateTime corteD7 = cadastro.plusDays(7);
                boolean voltouD7 = (ultTx != null && !ultTx.isBefore(corteD7))
                        || (ultAp != null && !ultAp.isBefore(corteD7))
                        || (ultAc != null && !ultAc.isBefore(corteD7));
                if (voltouD7) {
                    usuariosRetidosD7++;
                }
            }

            if (!cadastro.isAfter(ha30Dias)) {
                usuariosElegiveisM2++;
                LocalDateTime corteM2 = cadastro.plusDays(30);
                boolean voltouM2 = (ultTx != null && !ultTx.isBefore(corteM2))
                        || (ultAp != null && !ultAp.isBefore(corteM2))
                        || (ultAc != null && !ultAc.isBefore(corteM2));
                if (voltouM2) {
                    usuariosRetidosM2++;
                }
            }
        }

        double taxaAtivacao24h = calcularPercentual(usuariosAtivados24h, usuariosBaseAtivacao24h);
        double retencaoD7 = calcularPercentual(usuariosRetidosD7, usuariosElegiveisD7);
        double retencaoSegundoMes = calcularPercentual(usuariosRetidosM2, usuariosElegiveisM2);

        // Risco de Churn: usuários ativos e já ativados que não tiveram atividade nos últimos 14 dias
        LocalDateTime ha14Dias = agora.minusDays(14);
        long usuariosRiscoChurn = queryCount(
                """
                SELECT COUNT(u) FROM Usuario u
                WHERE (u.ativo IS NULL OR u.ativo = true)
                  AND (EXISTS (SELECT 1 FROM Transacao t WHERE t.contaFinanceira.usuario.id = u.id)
                       OR EXISTS (SELECT 1 FROM Meta m WHERE m.usuario.id = u.id))
                  AND (u.ultimoAcessoEm IS NULL OR u.ultimoAcessoEm < :d14)
                  AND NOT EXISTS (SELECT 1 FROM Transacao t2 WHERE t2.contaFinanceira.usuario.id = u.id AND COALESCE(t2.criadoEm, t2.dataHora) >= :d14)
                  AND NOT EXISTS (SELECT 1 FROM AporteMeta a2 WHERE a2.usuario.id = u.id AND a2.dataAporte >= :d14)
                """,
                Map.of("d14", ha14Dias)
        );

        // Etapa do Funil: usuários que criaram ao menos 1 conta financeira
        long funilComConta = queryCount("SELECT COUNT(DISTINCT c.usuario.id) FROM ContaFinanceira c", Map.of());

        // 2. Índice de Automação: Fatura PDF + Open Finance vs Manual
        long transacoesFaturaPdf = queryCount(
                "SELECT COUNT(t) FROM Transacao t WHERE t.origem = :orig",
                Map.of("orig", com.npl.na_ponta_do_lapis.entity.enums.OrigemTransacao.FATURA_PDF)
        );
        long transacoesOpenFinance = queryCount(
                "SELECT COUNT(t) FROM Transacao t WHERE t.origem = :orig OR (t.origem IS NULL AND t.pluggyTransactionId IS NOT NULL)",
                Map.of("orig", com.npl.na_ponta_do_lapis.entity.enums.OrigemTransacao.OPEN_FINANCE)
        );
        long transacoesManuais = Math.max(0, totalTransacoes - transacoesFaturaPdf - transacoesOpenFinance);
        double indiceAutomacao = calcularPercentual(transacoesFaturaPdf + transacoesOpenFinance, totalTransacoes);

        // 3. Fator Viral Familiar (K): média de membros ativos por família
        long membrosAtivosEmFamilias = queryCount(
                "SELECT COUNT(u) FROM Usuario u WHERE u.familia IS NOT NULL AND (u.ativo IS NULL OR u.ativo = true)",
                Map.of()
        );
        double fatorViralFamiliar = totalFamilias > 0
                ? Math.round(((double) membrosAtivosEmFamilias / totalFamilias) * 100.0) / 100.0
                : 0.0;

        // 5. Taxa de Conclusão de Metas
        long metasConcluidas = queryCount(
                "SELECT COUNT(m) FROM Meta m WHERE m.status = :st",
                Map.of("st", com.npl.na_ponta_do_lapis.entity.enums.StatusMeta.CONCLUIDA)
        );
        double taxaConclusaoMetas = calcularPercentual(metasConcluidas, totalMetas);

        // 6. Stickiness (WAU / MAU): login/acesso (throttle 1h), transações, aportes ou metas criadas
        String jpqlAtivosNoPeriodo = """
                SELECT COUNT(u) FROM Usuario u
                WHERE (u.ativo IS NULL OR u.ativo = true)
                  AND (
                    u.ultimoAcessoEm >= :d
                    OR EXISTS (SELECT 1 FROM Transacao t WHERE t.contaFinanceira.usuario.id = u.id AND COALESCE(t.criadoEm, t.dataHora) >= :d)
                    OR EXISTS (SELECT 1 FROM AporteMeta a WHERE a.usuario.id = u.id AND a.dataAporte >= :d)
                    OR EXISTS (SELECT 1 FROM Meta m WHERE m.usuario.id = u.id AND m.dataInicio >= :dDate)
                  )
                """;
        long wau = queryCount(jpqlAtivosNoPeriodo, Map.of("d", ha7Dias, "dDate", ha7Dias.toLocalDate()));
        long mau = queryCount(jpqlAtivosNoPeriodo, Map.of("d", ha30Dias, "dDate", ha30Dias.toLocalDate()));
        double stickinessWauMau = calcularPercentual(wau, mau);

        List<SerieDiariaDTO> serieCadastros = montarSerieCadastros(periodoDias);

        return new AdminDashboardResumoDTO(
                totalUsuarios,
                usuariosAtivos,
                usuariosSuspensos,
                ativosVerificados,
                ativosNaoVerificados,
                novos7,
                novos30,
                emailsVerificados,
                usuariosAtivados,
                totalFamilias,
                membrosAtivosEmFamilias,
                convitesPendentes,
                usuariosComIa,
                usuariosComOpenFinance,
                totalTransacoes,
                totalMetas,
                taxaAtivacao24h,
                usuariosAtivados24h,
                usuariosBaseAtivacao24h,
                indiceAutomacao,
                transacoesManuais,
                transacoesFaturaPdf,
                transacoesOpenFinance,
                fatorViralFamiliar,
                retencaoSegundoMes,
                usuariosRetidosM2,
                usuariosElegiveisM2,
                retencaoD7,
                usuariosRetidosD7,
                usuariosElegiveisD7,
                usuariosRiscoChurn,
                funilComConta,
                taxaConclusaoMetas,
                metasConcluidas,
                stickinessWauMau,
                wau,
                mau,
                periodoDias,
                novosNoPeriodo,
                novosPeriodoAnterior,
                serieCadastros
        );
    }

    private Map<Long, LocalDateTime> buscarPrimeiraDataPorUsuario(String jpql) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createQuery(jpql).getResultList();
        Map<Long, LocalDateTime> map = new HashMap<>();
        for (Object[] row : rows) {
            if (row[0] != null && row[1] != null) {
                map.put((Long) row[0], (LocalDateTime) row[1]);
            }
        }
        return map;
    }

    private double calcularPercentual(long parte, long total) {
        if (total <= 0) return 0.0;
        return Math.round(((double) parte / total) * 1000.0) / 10.0;
    }

    private List<SerieDiariaDTO> montarSerieCadastros(int periodoDias) {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio = hoje.minusDays(periodoDias - 1L).atStartOfDay();

        @SuppressWarnings("unchecked")
        List<LocalDateTime> datas = entityManager.createQuery(
                        "SELECT COALESCE(u.criadoEm, u.termosAceitosEm) FROM Usuario u WHERE COALESCE(u.criadoEm, u.termosAceitosEm) >= :inicio")
                .setParameter("inicio", inicio)
                .getResultList();

        Map<LocalDate, Long> contagemPorDia = new HashMap<>();
        for (LocalDateTime dt : datas) {
            if (dt != null) {
                contagemPorDia.merge(dt.toLocalDate(), 1L, Long::sum);
            }
        }

        List<SerieDiariaDTO> serie = new ArrayList<>(periodoDias);
        for (int i = periodoDias - 1; i >= 0; i--) {
            LocalDate dia = hoje.minusDays(i);
            serie.add(new SerieDiariaDTO(dia.toString(), contagemPorDia.getOrDefault(dia, 0L)));
        }
        return serie;
    }

    // -------------------------------------------------------------------------
    // Gestão de Usuários (LGPD estrita)
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public AdminPageResponseDTO<AdminUsuarioResumoDTO> listarUsuarios(
            String q,
            Papel papel,
            Boolean ativo,
            Boolean emailVerificado,
            Boolean onboardingConcluido,
            Boolean riscoChurn,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.min(Math.max(1, size), 100),
                Sort.by(Sort.Direction.DESC, "id")
        );

        org.springframework.data.jpa.domain.Specification<Usuario> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (q != null && !q.isBlank()) {
                String pattern = "%" + q.strip().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("nome")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern),
                        cb.like(cb.lower(root.get("username")), pattern)
                ));
            }
            if (papel != null) {
                predicates.add(cb.equal(root.get("papel"), papel));
            }
            if (ativo != null) {
                if (ativo) {
                    predicates.add(cb.or(cb.isNull(root.get("ativo")), cb.isTrue(root.get("ativo"))));
                } else {
                    predicates.add(cb.isFalse(root.get("ativo")));
                }
            }
            if (emailVerificado != null) {
                if (emailVerificado) {
                    predicates.add(cb.or(cb.isNull(root.get("emailVerificado")), cb.isTrue(root.get("emailVerificado"))));
                } else {
                    predicates.add(cb.isFalse(root.get("emailVerificado")));
                }
            }
            if (onboardingConcluido != null) {
                if (onboardingConcluido) {
                    predicates.add(cb.isTrue(root.get("onboardingConcluido")));
                } else {
                    predicates.add(cb.or(cb.isNull(root.get("onboardingConcluido")), cb.isFalse(root.get("onboardingConcluido"))));
                }
            }
            if (Boolean.TRUE.equals(riscoChurn)) {
                LocalDateTime ha14Dias = LocalDateTime.now().minusDays(14);
                predicates.add(cb.or(cb.isNull(root.get("ativo")), cb.isTrue(root.get("ativo"))));
                predicates.add(cb.or(cb.isNull(root.get("ultimoAcessoEm")), cb.lessThan(root.get("ultimoAcessoEm"), ha14Dias)));

                var sqTemTx = query.subquery(Long.class);
                var t1 = sqTemTx.from(Transacao.class);
                sqTemTx.select(cb.literal(1L))
                        .where(cb.equal(t1.get("contaFinanceira").get("usuario").get("id"), root.get("id")));

                var sqTemMeta = query.subquery(Long.class);
                var m1 = sqTemMeta.from(Meta.class);
                sqTemMeta.select(cb.literal(1L))
                        .where(cb.equal(m1.get("usuario").get("id"), root.get("id")));

                predicates.add(cb.or(cb.exists(sqTemTx), cb.exists(sqTemMeta)));

                var sqTxRecente = query.subquery(Long.class);
                var t2 = sqTxRecente.from(Transacao.class);
                sqTxRecente.select(cb.literal(1L))
                        .where(
                                cb.equal(t2.get("contaFinanceira").get("usuario").get("id"), root.get("id")),
                                cb.greaterThanOrEqualTo(cb.coalesce(t2.get("criadoEm"), t2.get("dataHora")), ha14Dias)
                        );
                predicates.add(cb.not(cb.exists(sqTxRecente)));

                var sqAporteRecente = query.subquery(Long.class);
                var a2 = sqAporteRecente.from(AporteMeta.class);
                sqAporteRecente.select(cb.literal(1L))
                        .where(
                                cb.equal(a2.get("usuario").get("id"), root.get("id")),
                                cb.greaterThanOrEqualTo(a2.get("dataAporte"), ha14Dias)
                        );
                predicates.add(cb.not(cb.exists(sqAporteRecente)));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<Usuario> result = usuarioRepository.findAll(spec, pageable);

        List<AdminUsuarioResumoDTO> content = result.getContent().stream()
                .map(u -> new AdminUsuarioResumoDTO(
                        u.getId(),
                        u.getNome(),
                        u.getEmail(),
                        u.getUsername(),
                        u.getPapel(),
                        u.isAtivo(),
                        u.isEmailVerificado(),
                        u.isOnboardingConcluido(),
                        u.getMoedaPadrao(),
                        u.getFamilia() != null ? u.getFamilia().getNome() : null,
                        u.getCriadoEm() != null ? u.getCriadoEm() : u.getTermosAceitosEm(),
                        u.getUltimoAcessoEm()
                ))
                .toList();

        return new AdminPageResponseDTO<>(content, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public AdminUsuarioDetalheDTO obterDetalheUsuario(Long usuarioId) {
        Usuario u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioIdNaoExisteException("Usuário de ID " + usuarioId + " não encontrado."));

        long totalContas = queryCount("SELECT COUNT(c) FROM ContaFinanceira c WHERE c.usuario.id = :uid", Map.of("uid", usuarioId));
        long totalTransacoes = queryCount("SELECT COUNT(t) FROM Transacao t WHERE t.contaFinanceira.usuario.id = :uid", Map.of("uid", usuarioId));
        long totalMetas = queryCount("SELECT COUNT(m) FROM Meta m WHERE m.usuario.id = :uid", Map.of("uid", usuarioId));
        long totalConquistas = queryCount("SELECT COUNT(uc) FROM UsuarioConquista uc WHERE uc.usuario.id = :uid", Map.of("uid", usuarioId));
        boolean possuiIa = queryCount("SELECT COUNT(c) FROM ChaveIaUsuario c WHERE c.usuario.id = :uid", Map.of("uid", usuarioId)) > 0;
        boolean possuiOpenFinance = queryCount("SELECT COUNT(p) FROM UsuarioPluggyIntegracao p WHERE p.usuario.id = :uid", Map.of("uid", usuarioId)) > 0;

        return new AdminUsuarioDetalheDTO(
                u.getId(),
                u.getNome(),
                u.getEmail(),
                u.getUsername(),
                u.getTelefone(),
                u.getFotoPerfil(),
                u.getPapel(),
                u.isAtivo(),
                u.getSuspensoEm(),
                u.getMotivoSuspensao(),
                u.isEmailVerificado(),
                u.isOnboardingConcluido(),
                u.getMoedaPadrao(),
                u.getVersaoTermos(),
                u.getTermosAceitosEm(),
                u.getCriadoEm(),
                u.getUltimoAcessoEm(),
                u.getFamilia() != null ? u.getFamilia().getId() : null,
                u.getFamilia() != null ? u.getFamilia().getNome() : null,
                totalContas,
                totalTransacoes,
                totalMetas,
                totalConquistas,
                possuiIa,
                possuiOpenFinance
        );
    }

    @Transactional
    public AdminUsuarioDetalheDTO alterarSuspensao(Long usuarioId, AdminSuspensaoRequestDTO req, String ip) {
        Usuario admin = usuarioService.buscarUsuarioLogado();
        Usuario alvo = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioIdNaoExisteException("Usuário de ID " + usuarioId + " não encontrado."));

        if (!req.ativo() && Objects.equals(admin.getId(), alvo.getId())) {
            throw new BusinessException("Você não pode suspender a própria conta.");
        }
        if (!req.ativo() && (req.motivo() == null || req.motivo().isBlank())) {
            throw new BusinessException("Informe o motivo da suspensão.");
        }
        if (!req.ativo() && alvo.getPapel() == Papel.ADMIN_SITE && usuarioRepository.contarAtivosPorPapel(Papel.ADMIN_SITE) <= 1) {
            throw new BusinessException("Não é possível suspender o último ADMIN_SITE ativo da plataforma.");
        }

        alvo.setAtivo(req.ativo());
        if (!req.ativo()) {
            alvo.setSuspensoEm(LocalDateTime.now());
            alvo.setMotivoSuspensao(req.motivo().strip());
        } else {
            alvo.setSuspensoEm(null);
            alvo.setMotivoSuspensao(null);
        }
        usuarioRepository.save(alvo);
        usuarioAuthCacheService.evictPorUsuario(alvo);

        registrarAuditoria(
                admin,
                req.ativo() ? "REATIVAR_USUARIO" : "SUSPENDER_USUARIO",
                "USUARIO",
                String.valueOf(alvo.getId()),
                alvo.getEmail(),
                req.motivo(),
                Map.of("ativo", req.ativo()),
                ip
        );

        return obterDetalheUsuario(usuarioId);
    }

    @Transactional
    public AdminUsuarioDetalheDTO forcarVerificacaoEmail(Long usuarioId, String ip) {
        Usuario admin = usuarioService.buscarUsuarioLogado();
        Usuario alvo = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioIdNaoExisteException("Usuário de ID " + usuarioId + " não encontrado."));

        alvo.setEmailVerificado(true);
        alvo.setEmailVerificacaoTokenHash(null);
        alvo.setEmailVerificacaoExpiraEm(null);
        usuarioRepository.save(alvo);
        usuarioAuthCacheService.evictPorUsuario(alvo);

        registrarAuditoria(
                admin,
                "VERIFICAR_EMAIL_USUARIO",
                "USUARIO",
                String.valueOf(alvo.getId()),
                alvo.getEmail(),
                "Verificação manual via painel admin",
                Map.of("emailVerificado", true),
                ip
        );

        return obterDetalheUsuario(usuarioId);
    }

    @Transactional
    public AdminUsuarioDetalheDTO alterarPapel(Long usuarioId, AdminAlterarPapelRequestDTO req, String ip) {
        Usuario admin = usuarioService.buscarUsuarioLogado();
        Usuario alvo = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioIdNaoExisteException("Usuário de ID " + usuarioId + " não encontrado."));

        if (req.papel() != Papel.USUARIO && req.papel() != Papel.ADMIN_SITE) {
            throw new BusinessException("O painel permite alternar apenas entre USUARIO e ADMIN_SITE.");
        }
        if (Objects.equals(admin.getId(), alvo.getId()) && req.papel() != Papel.ADMIN_SITE) {
            throw new BusinessException("Você não pode remover o seu próprio papel de ADMIN_SITE.");
        }
        if (alvo.getPapel() == Papel.ADMIN_SITE && req.papel() != Papel.ADMIN_SITE
                && usuarioRepository.contarAtivosPorPapel(Papel.ADMIN_SITE) <= 1) {
            throw new BusinessException("Não é possível rebaixar o último ADMIN_SITE ativo da plataforma.");
        }

        Papel papelAnterior = alvo.getPapel();
        alvo.setPapel(req.papel());
        usuarioRepository.save(alvo);
        usuarioAuthCacheService.evictPorUsuario(alvo);

        registrarAuditoria(
                admin,
                "ALTERAR_PAPEL_USUARIO",
                "USUARIO",
                String.valueOf(alvo.getId()),
                alvo.getEmail(),
                req.motivo(),
                Map.of("antes", String.valueOf(papelAnterior), "depois", String.valueOf(req.papel())),
                ip
        );

        return obterDetalheUsuario(usuarioId);
    }

    @Transactional
    public AdminUsuarioDetalheDTO resetarOnboarding(Long usuarioId, String ip) {
        Usuario admin = usuarioService.buscarUsuarioLogado();
        Usuario alvo = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioIdNaoExisteException("Usuário de ID " + usuarioId + " não encontrado."));

        alvo.setOnboardingConcluido(false);
        usuarioRepository.save(alvo);
        usuarioAuthCacheService.evictPorUsuario(alvo);

        registrarAuditoria(
                admin,
                "RESETAR_ONBOARDING",
                "USUARIO",
                String.valueOf(alvo.getId()),
                alvo.getEmail(),
                "Reset de onboarding via painel admin",
                Map.of("onboardingConcluido", false),
                ip
        );

        return obterDetalheUsuario(usuarioId);
    }

    @Transactional
    public void excluirUsuarioDefinitivamente(Long usuarioId, AdminExcluirUsuarioRequestDTO req, String ip) {
        Usuario admin = usuarioService.buscarUsuarioLogado();
        Usuario alvo = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioIdNaoExisteException("Usuário de ID " + usuarioId + " não encontrado."));

        if (Objects.equals(admin.getId(), alvo.getId())) {
            throw new BusinessException("Você não pode excluir a própria conta pelo painel admin.");
        }
        if (alvo.getPapel() == Papel.ADMIN_SITE && usuarioRepository.contarAtivosPorPapel(Papel.ADMIN_SITE) <= 1) {
            throw new BusinessException("Não é possível excluir o último ADMIN_SITE ativo da plataforma.");
        }
        if (req.confirmacaoEmail() == null || !alvo.getEmail().equalsIgnoreCase(req.confirmacaoEmail().strip())) {
            throw new BusinessException("O e-mail de confirmação não confere com o e-mail da conta selecionada.");
        }

        String emailAlvo = alvo.getEmail();
        String nomeAlvo = alvo.getNome();
        usuarioService.excluirUsuario(usuarioId);

        registrarAuditoria(
                admin,
                "EXCLUIR_USUARIO",
                "USUARIO",
                String.valueOf(usuarioId),
                emailAlvo,
                req.motivo().strip(),
                Map.of("nome", nomeAlvo, "email", emailAlvo),
                ip
        );
    }

    // -------------------------------------------------------------------------
    // Famílias
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public AdminPageResponseDTO<AdminFamiliaResumoDTO> listarFamilias(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100), Sort.by(Sort.Direction.DESC, "id"));
        Page<Familia> result = familiaRepository.findAll(pageable);

        List<Long> familiaIds = result.getContent().stream().map(Familia::getId).toList();
        if (familiaIds.isEmpty()) {
            return new AdminPageResponseDTO<>(List.of(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
        }

        Map<Long, Long> membrosPorFamilia = new HashMap<>();
        @SuppressWarnings("unchecked")
        List<Object[]> membrosRows = entityManager.createQuery(
                        "SELECT u.familia.id, COUNT(u) FROM Usuario u WHERE u.familia.id IN :fids GROUP BY u.familia.id")
                .setParameter("fids", familiaIds)
                .getResultList();
        for (Object[] row : membrosRows) {
            membrosPorFamilia.put((Long) row[0], ((Number) row[1]).longValue());
        }

        Map<Long, Long> convitesPorFamilia = new HashMap<>();
        @SuppressWarnings("unchecked")
        List<Object[]> convitesRows = entityManager.createQuery(
                        "SELECT c.familia.id, COUNT(c) FROM Convite c WHERE c.familia.id IN :fids AND c.status = :st GROUP BY c.familia.id")
                .setParameter("fids", familiaIds)
                .setParameter("st", StatusConvite.PENDENTE)
                .getResultList();
        for (Object[] row : convitesRows) {
            convitesPorFamilia.put((Long) row[0], ((Number) row[1]).longValue());
        }

        Map<Long, Usuario> adminPorFamilia = new HashMap<>();
        @SuppressWarnings("unchecked")
        List<Usuario> admins = entityManager.createQuery(
                        "SELECT u FROM Usuario u WHERE u.familia.id IN :fids AND u.papel = :papel")
                .setParameter("fids", familiaIds)
                .setParameter("papel", Papel.ADMIN_FAMILIA)
                .getResultList();
        for (Usuario adm : admins) {
            if (adm.getFamilia() != null) {
                adminPorFamilia.putIfAbsent(adm.getFamilia().getId(), adm);
            }
        }

        List<AdminFamiliaResumoDTO> content = result.getContent().stream().map(f -> {
            Usuario adminFam = adminPorFamilia.get(f.getId());
            return new AdminFamiliaResumoDTO(
                    f.getId(),
                    f.getNome(),
                    membrosPorFamilia.getOrDefault(f.getId(), 0L),
                    adminFam != null ? adminFam.getNome() : null,
                    adminFam != null ? adminFam.getEmail() : null,
                    convitesPorFamilia.getOrDefault(f.getId(), 0L)
            );
        }).toList();

        return new AdminPageResponseDTO<>(content, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    // -------------------------------------------------------------------------
    // Catálogos Globais: Categorias & Conquistas
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<AdminCategoriaResumoDTO> listarCategorias() {
        List<TipoCategoria> categorias = tipoCategoriaRepository.findAll(Sort.by(Sort.Direction.ASC, "nome"));
        @SuppressWarnings("unchecked")
        List<Object[]> counts = entityManager.createQuery(
                        "SELECT t.categoria.id, COUNT(t) FROM Transacao t GROUP BY t.categoria.id")
                .getResultList();
        Map<Long, Long> countMap = new HashMap<>();
        for (Object[] row : counts) {
            countMap.put((Long) row[0], ((Number) row[1]).longValue());
        }
        return categorias.stream()
                .map(c -> new AdminCategoriaResumoDTO(c.getId(), c.getNome(), countMap.getOrDefault(c.getId(), 0L)))
                .toList();
    }

    @Transactional
    public AdminCategoriaResumoDTO criarCategoria(AdminCategoriaRequestDTO req, String ip) {
        Usuario admin = usuarioService.buscarUsuarioLogado();
        String nome = req.nome().strip();
        boolean jaExiste = tipoCategoriaRepository.findAll().stream()
                .anyMatch(c -> c.getNome().equalsIgnoreCase(nome));
        if (jaExiste) {
            throw new BusinessException("Já existe uma categoria com o nome '" + nome + "'.");
        }
        TipoCategoria nova = new TipoCategoria();
        nova.setNome(nome);
        TipoCategoria salva = tipoCategoriaRepository.save(nova);

        registrarAuditoria(admin, "CRIAR_CATEGORIA", "TIPO_CATEGORIA", String.valueOf(salva.getId()), salva.getNome(), null, Map.of("nome", salva.getNome()), ip);
        return new AdminCategoriaResumoDTO(salva.getId(), salva.getNome(), 0L);
    }

    @Transactional
    public AdminCategoriaResumoDTO atualizarCategoria(Long id, AdminCategoriaRequestDTO req, String ip) {
        Usuario admin = usuarioService.buscarUsuarioLogado();
        TipoCategoria cat = tipoCategoriaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Categoria não encontrada."));
        String nomeAnterior = cat.getNome();
        String novoNome = req.nome().strip();
        cat.setNome(novoNome);
        tipoCategoriaRepository.save(cat);

        long totalTransacoes = queryCount("SELECT COUNT(t) FROM Transacao t WHERE t.categoria.id = :cid", Map.of("cid", id));
        registrarAuditoria(admin, "EDITAR_CATEGORIA", "TIPO_CATEGORIA", String.valueOf(id), novoNome, null, Map.of("antes", nomeAnterior, "depois", novoNome), ip);
        return new AdminCategoriaResumoDTO(cat.getId(), cat.getNome(), totalTransacoes);
    }

    @Transactional
    public void excluirCategoria(Long id, String ip) {
        Usuario admin = usuarioService.buscarUsuarioLogado();
        TipoCategoria cat = tipoCategoriaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Categoria não encontrada."));
        long totalTransacoes = queryCount("SELECT COUNT(t) FROM Transacao t WHERE t.categoria.id = :cid", Map.of("cid", id));
        if (totalTransacoes > 0) {
            throw new BusinessException("Não é possível excluir a categoria '" + cat.getNome() + "' pois existem " + totalTransacoes + " transação(ões) vinculadas.");
        }
        String nome = cat.getNome();
        tipoCategoriaRepository.delete(cat);
        registrarAuditoria(admin, "EXCLUIR_CATEGORIA", "TIPO_CATEGORIA", String.valueOf(id), nome, null, Map.of("nome", nome), ip);
    }

    @Transactional(readOnly = true)
    public List<AdminConquistaResumoDTO> listarConquistas() {
        List<Conquista> conquistas = conquistaRepository.findAll(Sort.by(Sort.Direction.ASC, "categoria", "pontosXp"));
        @SuppressWarnings("unchecked")
        List<Object[]> counts = entityManager.createQuery(
                        "SELECT uc.conquista.id, COUNT(uc) FROM UsuarioConquista uc GROUP BY uc.conquista.id")
                .getResultList();
        Map<String, Long> countMap = new HashMap<>();
        for (Object[] row : counts) {
            countMap.put((String) row[0], ((Number) row[1]).longValue());
        }
        return conquistas.stream()
                .map(c -> new AdminConquistaResumoDTO(
                        c.getId(),
                        c.getTitulo(),
                        c.getDescricao(),
                        c.getIcone(),
                        c.getPontosXp(),
                        c.getCategoria(),
                        countMap.getOrDefault(c.getId(), 0L)
                ))
                .toList();
    }

    @Transactional
    public AdminConquistaResumoDTO criarConquista(AdminConquistaRequestDTO req, String ip) {
        Usuario admin = usuarioService.buscarUsuarioLogado();
        if (req.id() == null || req.id().isBlank()) {
            throw new BusinessException("Informe um identificador único (ID) para a conquista.");
        }
        String idNormalizado = req.id().strip().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9_]", "_");
        if (conquistaRepository.existsById(idNormalizado)) {
            throw new BusinessException("Já existe uma conquista com o ID '" + idNormalizado + "'.");
        }
        Conquista nova = new Conquista(
                idNormalizado,
                req.titulo().strip(),
                req.descricao().strip(),
                req.icone().strip(),
                req.pontosXp(),
                req.categoria()
        );
        conquistaRepository.save(nova);
        registrarAuditoria(admin, "CRIAR_CONQUISTA", "CONQUISTA", idNormalizado, nova.getTitulo(), null, Map.of("pontosXp", nova.getPontosXp()), ip);
        return new AdminConquistaResumoDTO(nova.getId(), nova.getTitulo(), nova.getDescricao(), nova.getIcone(), nova.getPontosXp(), nova.getCategoria(), 0L);
    }

    @Transactional
    public AdminConquistaResumoDTO atualizarConquista(String id, AdminConquistaRequestDTO req, String ip) {
        Usuario admin = usuarioService.buscarUsuarioLogado();
        Conquista c = conquistaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Conquista '" + id + "' não encontrada."));
        c.setTitulo(req.titulo().strip());
        c.setDescricao(req.descricao().strip());
        c.setIcone(req.icone().strip());
        c.setPontosXp(req.pontosXp());
        c.setCategoria(req.categoria());
        conquistaRepository.save(c);

        long totalDesbloqueios = queryCount("SELECT COUNT(uc) FROM UsuarioConquista uc WHERE uc.conquista.id = :cid", Map.of("cid", id));
        registrarAuditoria(admin, "EDITAR_CONQUISTA", "CONQUISTA", id, c.getTitulo(), null, Map.of("pontosXp", c.getPontosXp(), "categoria", c.getCategoria().name()), ip);
        return new AdminConquistaResumoDTO(c.getId(), c.getTitulo(), c.getDescricao(), c.getIcone(), c.getPontosXp(), c.getCategoria(), totalDesbloqueios);
    }

    // -------------------------------------------------------------------------
    // Comunicação & Avisos (Sininho / Notificacao)
    // -------------------------------------------------------------------------

    @Transactional
    public AdminNotificacaoResultadoDTO enviarNotificacao(AdminNotificacaoRequestDTO req, String ip) {
        Usuario admin = usuarioService.buscarUsuarioLogado();
        String titulo = req.titulo().strip();
        String mensagem = req.mensagem().strip();
        String link = (req.linkRedirecionamento() == null || req.linkRedirecionamento().isBlank())
                ? null
                : req.linkRedirecionamento().strip();

        if (req.usuarioId() != null) {
            Usuario destino = usuarioRepository.findById(req.usuarioId())
                    .orElseThrow(() -> new UsuarioIdNaoExisteException("Usuário de ID " + req.usuarioId() + " não encontrado."));
            notificacaoRepository.save(new Notificacao(destino, titulo, mensagem, TipoNotificacao.SISTEMA, link));
            registrarAuditoria(
                    admin,
                    "ENVIAR_NOTIFICACAO_INDIVIDUAL",
                    "NOTIFICACAO",
                    String.valueOf(destino.getId()),
                    destino.getEmail(),
                    null,
                    Map.of("titulo", titulo),
                    ip
            );
            return new AdminNotificacaoResultadoDTO(1, false);
        }

        if ("RISCO_CHURN".equalsIgnoreCase(req.segmento())) {
            LocalDateTime ha14Dias = LocalDateTime.now().minusDays(14);
            @SuppressWarnings("unchecked")
            List<Usuario> emRisco = entityManager.createQuery(
                            """
                            SELECT u FROM Usuario u
                            WHERE (u.ativo IS NULL OR u.ativo = true)
                              AND (EXISTS (SELECT 1 FROM Transacao t WHERE t.contaFinanceira.usuario.id = u.id)
                                   OR EXISTS (SELECT 1 FROM Meta m WHERE m.usuario.id = u.id))
                              AND (u.ultimoAcessoEm IS NULL OR u.ultimoAcessoEm < :d14)
                              AND NOT EXISTS (SELECT 1 FROM Transacao t2 WHERE t2.contaFinanceira.usuario.id = u.id AND COALESCE(t2.criadoEm, t2.dataHora) >= :d14)
                              AND NOT EXISTS (SELECT 1 FROM AporteMeta a2 WHERE a2.usuario.id = u.id AND a2.dataAporte >= :d14)
                            """)
                    .setParameter("d14", ha14Dias)
                    .getResultList();

            List<Notificacao> lote = new ArrayList<>(emRisco.size());
            for (Usuario u : emRisco) {
                lote.add(new Notificacao(u, titulo, mensagem, TipoNotificacao.SISTEMA, link));
            }
            notificacaoRepository.saveAll(lote);

            registrarAuditoria(
                    admin,
                    "ENVIAR_NOTIFICACAO_RISCO_CHURN",
                    "NOTIFICACAO",
                    "RISCO_CHURN",
                    "Usuários em risco de churn (" + emRisco.size() + ")",
                    null,
                    Map.of("titulo", titulo, "destinatarios", emRisco.size()),
                    ip
            );
            return new AdminNotificacaoResultadoDTO(emRisco.size(), false);
        }

        List<Usuario> ativos = usuarioRepository.listarAtivos();
        List<Notificacao> lote = new ArrayList<>(ativos.size());
        for (Usuario u : ativos) {
            lote.add(new Notificacao(u, titulo, mensagem, TipoNotificacao.SISTEMA, link));
        }
        notificacaoRepository.saveAll(lote);

        registrarAuditoria(
                admin,
                "ENVIAR_NOTIFICACAO_GLOBAL",
                "NOTIFICACAO",
                "ALL",
                "Todos os usuários ativos (" + ativos.size() + ")",
                null,
                Map.of("titulo", titulo, "destinatarios", ativos.size()),
                ip
        );
        return new AdminNotificacaoResultadoDTO(ativos.size(), true);
    }

    // -------------------------------------------------------------------------
    // Auditoria Admin
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public AdminPageResponseDTO<AdminAuditLogDTO> listarAuditoria(
            String acao,
            String adminEmail,
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.min(Math.max(1, size), 100),
                Sort.by(Sort.Direction.DESC, "criadoEm")
        );

        org.springframework.data.jpa.domain.Specification<AdminAuditLog> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (acao != null && !acao.isBlank()) {
                predicates.add(cb.equal(root.get("acao"), acao.strip()));
            }
            if (adminEmail != null && !adminEmail.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("adminEmail")), "%" + adminEmail.strip().toLowerCase(Locale.ROOT) + "%"));
            }
            if (dataInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("criadoEm"), dataInicio));
            }
            if (dataFim != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("criadoEm"), dataFim));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        Page<AdminAuditLog> result = auditLogRepository.findAll(spec, pageable);

        List<AdminAuditLogDTO> content = result.getContent().stream()
                .map(l -> new AdminAuditLogDTO(
                        l.getId(),
                        l.getAdminUsuarioId(),
                        l.getAdminEmail(),
                        l.getAcao(),
                        l.getEntidadeAlvo(),
                        l.getAlvoId(),
                        l.getAlvoDescricao(),
                        l.getMotivo(),
                        l.getDetalhesJson(),
                        l.getIpOrigem(),
                        l.getCriadoEm()
                ))
                .toList();

        return new AdminPageResponseDTO<>(content, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private long queryCount(String jpql, Map<String, Object> params) {
        var query = entityManager.createQuery(jpql, Long.class);
        params.forEach(query::setParameter);
        Long res = query.getSingleResult();
        return res != null ? res : 0L;
    }

    private void registrarAuditoria(
            Usuario admin,
            String acao,
            String entidadeAlvo,
            String alvoId,
            String alvoDescricao,
            String motivo,
            Object detalhes,
            String ip
    ) {
        String detalhesJson = null;
        if (detalhes != null) {
            try {
                detalhesJson = objectMapper.writeValueAsString(detalhes);
            } catch (JsonProcessingException ignored) {
                detalhesJson = detalhes.toString();
            }
        }
        AdminAuditLog logEntry = new AdminAuditLog(
                admin.getId(),
                admin.getEmail(),
                acao,
                entidadeAlvo,
                alvoId,
                alvoDescricao,
                motivo,
                detalhesJson,
                ip
        );
        auditLogRepository.save(logEntry);
    }
}
