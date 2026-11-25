package com.npl.na_ponta_do_lapis.security;

import com.npl.na_ponta_do_lapis.config.CacheConfig;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerencia o cache distribuído de autenticação do usuário (chaveada por e-mail/username normalizados)
 * com fallback silencioso ao banco de dados em caso de indisponibilidade do provedor de cache.
 */
@Service
public class UsuarioAuthCacheService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioAuthCacheService.class);
    private static final Duration THROTTLE_ULTIMO_ACESSO = Duration.ofHours(1);
    private static final int MAX_THROTTLE_ENTRIES = 10_000;

    private final UsuarioRepository usuarioRepository;
    private final ObjectProvider<CacheManager> cacheManagerProvider;
    private final ConcurrentHashMap<Long, Instant> ultimoRegistroAcesso = new ConcurrentHashMap<>();

    public UsuarioAuthCacheService(
            UsuarioRepository usuarioRepository,
            ObjectProvider<CacheManager> cacheManagerProvider
    ) {
        this.usuarioRepository = usuarioRepository;
        this.cacheManagerProvider = cacheManagerProvider;
    }

    public Optional<Usuario> buscarPorIdentificador(String normalizedIdentifier) {
        String key = normalizeKey(normalizedIdentifier);
        if (key.isEmpty()) {
            return Optional.empty();
        }

        Cache cache = getCache();
        if (cache != null) {
            try {
                UsuarioAuthCacheDTO cached = cache.get(key, UsuarioAuthCacheDTO.class);
                if (cached != null) {
                    return Optional.of(cached.toUsuario());
                }
            } catch (RuntimeException ex) {
                log.warn("Falha ao ler cache de autenticação (chave='{}'): {}. Buscando no banco.", key, ex.getMessage());
            }
        }

        Optional<Usuario> found = usuarioRepository.findByEmailIgnoreCase(normalizedIdentifier)
                .or(() -> usuarioRepository.findByUsernameIgnoreCase(normalizedIdentifier));

        if (found.isPresent() && cache != null) {
            putSafely(cache, key, UsuarioAuthCacheDTO.from(found.get()));
        }

        return found;
    }

    public void evictPorUsuario(Usuario usuario) {
        if (usuario == null) {
            return;
        }
        evict(usuario.getEmail(), usuario.getUsername());
    }

    public void evict(String... identifiers) {
        Cache cache = getCache();
        if (cache == null || identifiers == null) {
            return;
        }
        for (String identifier : identifiers) {
            String key = normalizeKey(identifier);
            if (key.isEmpty()) {
                continue;
            }
            try {
                cache.evict(key);
            } catch (RuntimeException ex) {
                log.warn("Falha ao invalidar cache de autenticação (chave='{}'): {}", key, ex.getMessage());
            }
        }
    }

    private void putSafely(Cache cache, String key, UsuarioAuthCacheDTO dto) {
        try {
            cache.put(key, dto);
        } catch (RuntimeException ex) {
            log.warn("Falha ao gravar cache de autenticação (chave='{}'): {}", key, ex.getMessage());
        }
    }

    private Cache getCache() {
        CacheManager cacheManager = cacheManagerProvider.getIfAvailable();
        return cacheManager != null ? cacheManager.getCache(CacheConfig.CACHE_USUARIO_AUTH) : null;
    }

    private String normalizeKey(String identifier) {
        return identifier == null ? "" : identifier.strip().toLowerCase(Locale.ROOT);
    }

    /**
     * Atualiza {@code ultimoAcessoEm} do usuário autenticado no máximo 1 vez por hora
     * para refletir acessos contínuos via JWT em métricas de WAU/MAU sem gerar escrita excessiva no banco.
     */
    @Transactional
    public void registrarAcessoComThrottle(Long usuarioId) {
        if (usuarioId == null) {
            return;
        }
        Instant agora = Instant.now();
        Instant ultimo = ultimoRegistroAcesso.get(usuarioId);
        if (ultimo != null && Duration.between(ultimo, agora).compareTo(THROTTLE_ULTIMO_ACESSO) < 0) {
            return;
        }
        if (ultimoRegistroAcesso.size() >= MAX_THROTTLE_ENTRIES) {
            ultimoRegistroAcesso.clear();
        }
        ultimoRegistroAcesso.put(usuarioId, agora);
        try {
            usuarioRepository.atualizarUltimoAcesso(usuarioId, LocalDateTime.now());
        } catch (RuntimeException ex) {
            log.debug("Falha ao atualizar ultimoAcessoEm para usuário {}: {}", usuarioId, ex.getMessage());
        }
    }
}

