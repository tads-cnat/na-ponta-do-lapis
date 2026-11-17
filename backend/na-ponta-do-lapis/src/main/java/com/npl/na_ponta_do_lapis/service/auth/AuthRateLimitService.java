package com.npl.na_ponta_do_lapis.service.auth;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controla limites de taxa por IP e backoff progressivo por e-mail
 * para proteger fluxos públicos de cadastro, confirmação e recuperação de senha.
 */
@Service
public class AuthRateLimitService {

    private static final Duration IP_WINDOW = Duration.ofMinutes(15);
    private static final Duration EMAIL_WINDOW = Duration.ofHours(1);
    private static final Duration[] EMAIL_BACKOFF_STEPS = {
            Duration.ofSeconds(60),
            Duration.ofSeconds(120),
            Duration.ofSeconds(300)
    };
    private static final int MAX_EMAIL_SENDS_PER_HOUR = 3;

    private final Map<String, IpCounter> ipCounters = new ConcurrentHashMap<>();
    private final Map<String, EmailBackoffState> emailStates = new ConcurrentHashMap<>();

    /**
     * Verifica e consome uma tentativa para a chave de ação + IP dentro da janela de 15 minutos.
     */
    public boolean allowIpRequest(String action, String ip, int maxRequests) {
        String safeIp = (ip == null || ip.isBlank()) ? "unknown" : ip.strip();
        String key = action + ":" + safeIp;
        Instant now = Instant.now();

        cleanupIfNeeded(now);

        IpCounter updated = ipCounters.compute(key, (k, existing) -> {
            if (existing == null || existing.windowStart.plus(IP_WINDOW).isBefore(now)) {
                return new IpCounter(now, 1);
            }
            return new IpCounter(existing.windowStart, existing.count + 1);
        });

        return updated.count <= maxRequests;
    }

    /**
     * Verifica se um envio de e-mail é permitido pelo backoff progressivo (60s -> 120s -> 300s -> bloqueio 1h).
     * Retorna 0 quando permitido, ou os segundos restantes de espera quando bloqueado.
     */
    public long tryConsumeEmailSend(String action, String email) {
        if (email == null || email.isBlank()) {
            return 0;
        }
        String key = action + ":" + email.strip().toLowerCase();
        Instant now = Instant.now();

        EmailBackoffState current = emailStates.get(key);
        if (current != null) {
            if (current.windowStart.plus(EMAIL_WINDOW).isBefore(now)) {
                emailStates.remove(key);
                current = null;
            } else {
                if (current.sendCount >= MAX_EMAIL_SENDS_PER_HOUR) {
                    long remaining = Duration.between(now, current.windowStart.plus(EMAIL_WINDOW)).toSeconds();
                    return Math.max(1, remaining);
                }
                if (current.nextAllowedAt.isAfter(now)) {
                    long remaining = Duration.between(now, current.nextAllowedAt).toSeconds();
                    return Math.max(1, remaining);
                }
            }
        }

        int newCount = (current == null) ? 1 : current.sendCount + 1;
        Instant windowStart = (current == null) ? now : current.windowStart;
        Duration waitDuration = EMAIL_BACKOFF_STEPS[Math.min(newCount - 1, EMAIL_BACKOFF_STEPS.length - 1)];
        emailStates.put(key, new EmailBackoffState(windowStart, newCount, now.plus(waitDuration)));
        return 0;
    }

    /**
     * Limpa o histórico de backoff quando o usuário conclui a verificação ou redefinição.
     */
    public void resetEmailSend(String action, String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        emailStates.remove(action + ":" + email.strip().toLowerCase());
    }

    private void cleanupIfNeeded(Instant now) {
        if (ipCounters.size() > 2000) {
            ipCounters.entrySet().removeIf(entry -> entry.getValue().windowStart.plus(IP_WINDOW).isBefore(now));
        }
        if (emailStates.size() > 2000) {
            emailStates.entrySet().removeIf(entry -> entry.getValue().windowStart.plus(EMAIL_WINDOW).isBefore(now));
        }
    }

    private record IpCounter(Instant windowStart, int count) {}

    private record EmailBackoffState(Instant windowStart, int sendCount, Instant nextAllowedAt) {}
}

