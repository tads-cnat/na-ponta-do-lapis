package com.npl.na_ponta_do_lapis.service;

import com.npl.na_ponta_do_lapis.entity.Conquista;
import com.npl.na_ponta_do_lapis.entity.Meta;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.UsuarioConquista;
import com.npl.na_ponta_do_lapis.entity.enums.StatusMeta;
import com.npl.na_ponta_do_lapis.entity.enums.TipoMeta;
import com.npl.na_ponta_do_lapis.entity.enums.TipoNotificacao;
import com.npl.na_ponta_do_lapis.repository.AporteMetaRepository;
import com.npl.na_ponta_do_lapis.repository.ConquistaRepository;
import com.npl.na_ponta_do_lapis.repository.MetaRepository;
import com.npl.na_ponta_do_lapis.repository.UsuarioConquistaRepository;
import com.npl.na_ponta_do_lapis.web.dto.ConquistaItemDTO;
import com.npl.na_ponta_do_lapis.web.dto.PerfilGamificadoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConquistaService {

    private static final Logger log = LoggerFactory.getLogger(ConquistaService.class);

    private final ConquistaRepository conquistaRepository;
    private final UsuarioConquistaRepository usuarioConquistaRepository;
    private final MetaRepository metaRepository;
    private final AporteMetaRepository aporteMetaRepository;
    private final NotificacaoService notificacaoService;

    public ConquistaService(
            ConquistaRepository conquistaRepository,
            UsuarioConquistaRepository usuarioConquistaRepository,
            MetaRepository metaRepository,
            AporteMetaRepository aporteMetaRepository,
            NotificacaoService notificacaoService
    ) {
        this.conquistaRepository = conquistaRepository;
        this.usuarioConquistaRepository = usuarioConquistaRepository;
        this.metaRepository = metaRepository;
        this.aporteMetaRepository = aporteMetaRepository;
        this.notificacaoService = notificacaoService;
    }

    @Transactional(readOnly = true)
    public PerfilGamificadoDTO obterPerfilGamificado(Long usuarioId) {
        List<Conquista> todasConquistas = conquistaRepository.findAll();
        List<UsuarioConquista> conquistasDoUsuario = usuarioConquistaRepository.findByUsuarioIdOrderByDataDesbloqueioDesc(usuarioId);

        Map<String, LocalDateTime> mapaDesbloqueadas = conquistasDoUsuario.stream()
                .collect(Collectors.toMap(
                        uc -> uc.getConquista().getId(),
                        UsuarioConquista::getDataDesbloqueio,
                        (existing, replacement) -> existing
                ));

        int totalXp = conquistasDoUsuario.stream()
                .mapToInt(uc -> uc.getConquista().getPontosXp())
                .sum();

        long metasConcluidas = metaRepository.countByUsuarioIdAndStatus(usuarioId, StatusMeta.CONCLUIDA);

        List<ConquistaItemDTO> itens = todasConquistas.stream()
                .map(c -> {
                    boolean desbloqueada = mapaDesbloqueadas.containsKey(c.getId());
                    LocalDateTime data = mapaDesbloqueadas.get(c.getId());
                    return ConquistaItemDTO.fromEntity(c, desbloqueada, data);
                })
                .sorted((a, b) -> {
                    // Desbloqueadas primeiro, depois por XP desc
                    if (a.desbloqueada() != b.desbloqueada()) {
                        return a.desbloqueada() ? -1 : 1;
                    }
                    return Integer.compare(b.pontosXp(), a.pontosXp());
                })
                .toList();

        // Cálculo de Nível (1 a 5)
        NivelInfo nivelInfo = calcularNivel(totalXp);

        return new PerfilGamificadoDTO(
                totalXp,
                nivelInfo.nivel,
                nivelInfo.tituloNivel,
                nivelInfo.xpAtualNoNivel,
                nivelInfo.xpNecessarioParaProximoNivel,
                nivelInfo.progressoNivel,
                (int) metasConcluidas,
                conquistasDoUsuario.size(),
                todasConquistas.size(),
                itens
        );
    }

    @Transactional
    public boolean desbloquear(Usuario usuario, String conquistaId) {
        if (usuarioConquistaRepository.existsByUsuarioIdAndConquistaId(usuario.getId(), conquistaId)) {
            return false; // Já possui
        }

        Optional<Conquista> conquistaOpt = conquistaRepository.findById(conquistaId);
        if (conquistaOpt.isEmpty()) {
            log.warn("Conquista com ID {} não encontrada no catálogo", conquistaId);
            return false;
        }

        Conquista conquista = conquistaOpt.get();
        UsuarioConquista usuarioConquista = new UsuarioConquista(usuario, conquista, LocalDateTime.now());
        usuarioConquistaRepository.save(usuarioConquista);

        log.info("Conquista desbloqueada: usuário {} -> conquista {} (+{} XP)",
                usuario.getId(), conquista.getId(), conquista.getPontosXp());

        // Dispara notificação in-app
        try {
            notificacaoService.criarNotificacao(
                    usuario,
                    "Nova Conquista Desbloqueada! 🏆",
                    "Você desbloqueou '" + conquista.getTitulo() + "' (+" + conquista.getPontosXp() + " XP)!",
                    TipoNotificacao.CONQUISTA_DESBLOQUEADA,
                    "/perfil?aba=conquistas"
            );
        } catch (Exception e) {
            log.error("Erro ao enviar notificação de conquista desbloqueada: {}", e.getMessage());
        }

        return true;
    }

    @Transactional
    public void verificarConquistasCriacaoMeta(Meta meta, Usuario usuario) {
        try {
            // 1. PRIMEIRA_META: Cadastrou a primeira meta
            long totalMetas = metaRepository.countByUsuarioId(usuario.getId());
            if (totalMetas >= 1) {
                desbloquear(usuario, "PRIMEIRA_META");
            }

            // 2. ORCAMENTO_BLINDADO: Criou teto de gastos
            if (meta.getTipoMeta() == TipoMeta.GASTO) {
                desbloquear(usuario, "ORCAMENTO_BLINDADO");
            }

            // 3. LIVRE_DE_DIVIDAS: Criou meta para quitar dívidas
            if (meta.getTipoMeta() == TipoMeta.DIVIDA) {
                desbloquear(usuario, "LIVRE_DE_DIVIDAS");
            }

            // 4. FAMILIA_UNIDA: Criou meta associada à família
            if (meta.getFamilia() != null) {
                desbloquear(usuario, "FAMILIA_UNIDA");
            }
        } catch (Exception e) {
            log.error("Erro ao verificar conquistas na criação da meta {}: {}", meta.getId(), e.getMessage());
        }
    }

    @Transactional
    public void verificarConquistasAporte(Meta meta, Usuario usuario) {
        try {
            long totalAportes = aporteMetaRepository.countByUsuarioId(usuario.getId());
            if (totalAportes >= 1) {
                desbloquear(usuario, "PRIMEIRO_APORTE");
            }
            if (totalAportes >= 5) {
                desbloquear(usuario, "POUPADOR_BRONZE");
            }

            // Se meta foi concluída
            if (meta.getStatus() == StatusMeta.CONCLUIDA) {
                desbloquear(usuario, "META_BATIDA_1");

                long concluidas = metaRepository.countByUsuarioIdAndStatus(usuario.getId(), StatusMeta.CONCLUIDA);
                if (concluidas >= 3) {
                    desbloquear(usuario, "MESTRE_DAS_METAS");
                }
            }
        } catch (Exception e) {
            log.error("Erro ao verificar conquistas de aporte na meta {}: {}", meta.getId(), e.getMessage());
        }
    }

    private NivelInfo calcularNivel(int totalXp) {
        if (totalXp < 200) {
            // Nível 1: 0 a 199 XP (próximo nível em 200)
            int atual = totalXp;
            int necessario = 200;
            double progresso = (atual * 100.0) / necessario;
            return new NivelInfo(1, "Iniciante Consciente", atual, necessario, Math.min(100.0, progresso));
        } else if (totalXp < 500) {
            // Nível 2: 200 a 499 XP (faixa de 300 XP)
            int atual = totalXp - 200;
            int necessario = 300;
            double progresso = (atual * 100.0) / necessario;
            return new NivelInfo(2, "Poupador em Foco", atual, necessario, Math.min(100.0, progresso));
        } else if (totalXp < 1000) {
            // Nível 3: 500 a 999 XP (faixa de 500 XP)
            int atual = totalXp - 500;
            int necessario = 500;
            double progresso = (atual * 100.0) / necessario;
            return new NivelInfo(3, "Estrategista Financeiro", atual, necessario, Math.min(100.0, progresso));
        } else if (totalXp < 2000) {
            // Nível 4: 1000 a 1999 XP (faixa de 1000 XP)
            int atual = totalXp - 1000;
            int necessario = 1000;
            double progresso = (atual * 100.0) / necessario;
            return new NivelInfo(4, "Investidor Disciplinado", atual, necessario, Math.min(100.0, progresso));
        } else {
            // Nível 5: 2000+ XP (Nível Máximo)
            int atual = totalXp - 2000;
            return new NivelInfo(5, "Mestre da Liberdade Financeira", atual, 0, 100.0);
        }
    }

    private record NivelInfo(
            int nivel,
            String tituloNivel,
            int xpAtualNoNivel,
            int xpNecessarioParaProximoNivel,
            double progressoNivel
    ) {}
}

