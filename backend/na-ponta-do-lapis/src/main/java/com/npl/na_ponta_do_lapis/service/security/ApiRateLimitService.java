package com.npl.na_ponta_do_lapis.service.security;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.npl.na_ponta_do_lapis.web.exception.RateLimitExceededException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Rate limiter distribuído (via Redis quando disponível, com fallback em memória)
 * para proteger endpoints autenticados contra rajadas e automação abusiva.
 */
@Service
public class ApiRateLimitService {

    private static final Logger log = LoggerFactory.getLogger(ApiRateLimitService.class);
    private static final String PREFIX = "npl:ratelimit:";
    private static final int MAX_LOCAL_ENTRIES = 10_000;

    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;
    private final Map<String, WindowCounter> localCounters = new ConcurrentHashMap<>();

    public ApiRateLimitService(ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        this.redisTemplateProvider = redisTemplateProvider;
    }

    public void enforce(String bucket, String userKey, String ip, int maxRequestsPerUser, Duration window) {
        String normalizedUser = (userKey == null || userKey.isBlank()) ? "anon" : userKey.trim().toLowerCase();
        String normalizedIp = (ip == null || ip.isBlank()) ? "unknown" : ip.trim();

        boolean userAllowed = allow(bucket + ":u:" + normalizedUser, maxRequestsPerUser, window);
        boolean ipAllowed = allow(bucket + ":ip:" + normalizedIp, Math.max(maxRequestsPerUser * 2, maxRequestsPerUser + 5), window);

        if (!userAllowed || !ipAllowed) {
            throw new RateLimitExceededException(
                    "Muitas requisições em sequência. Aguarde alguns segundos e tente novamente.");
        }
    }

    public boolean allow(String key, int maxRequests, Duration window) {
        StringRedisTemplate redisTemplate = redisTemplateProvider.getIfAvailable();
        if (redisTemplate != null) {
            try {
                String redisKey = PREFIX + key;
                Long count = redisTemplate.opsForValue().increment(redisKey);
                if (count != null && count == 1L) {
                    redisTemplate.expire(redisKey, window);
                }
                return count == null || count <= maxRequests;
            } catch (Exception ex) {
                log.warn("Falha no rate limit via Redis, usando fallback local: {}", ex.getMessage());
            }
        }
        return allowLocal(key, maxRequests, window.toMillis());
    }

    public String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            return (comma > 0 ? xff.substring(0, comma) : xff).trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private boolean allowLocal(String key, int maxRequests, long windowMs) {
        long now = System.currentTimeMillis();
        if (localCounters.size() > MAX_LOCAL_ENTRIES) {
            localCounters.entrySet().removeIf(entry -> now - entry.getValue().windowStartMs >= windowMs);
        }
        WindowCounter counter = localCounters.compute(key, (k, existing) -> {
            if (existing == null || now - existing.windowStartMs >= windowMs) {
                return new WindowCounter(now, new AtomicInteger(1));
            }
            existing.count.incrementAndGet();
            return existing;
        });
        return counter.count.get() <= maxRequests;
    }

    private static final class WindowCounter {
        private final long windowStartMs;
        private final AtomicInteger count;

        private WindowCounter(long windowStartMs, AtomicInteger count) {
            this.windowStartMs = windowStartMs;
            this.count = count;
        }
    }
}
