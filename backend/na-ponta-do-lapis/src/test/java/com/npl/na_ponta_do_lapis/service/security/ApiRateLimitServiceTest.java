package com.npl.na_ponta_do_lapis.service.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;

import com.npl.na_ponta_do_lapis.web.exception.RateLimitExceededException;

class ApiRateLimitServiceTest {

    private ApiRateLimitService rateLimitService;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        ObjectProvider<StringRedisTemplate> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);
        rateLimitService = new ApiRateLimitService(provider);
    }

    @Test
    void devePermitirAteOLimiteEBloquearExcessoPorUsuario() {
        for (int i = 0; i < 3; i++) {
            assertDoesNotThrow(() -> rateLimitService.enforce(
                    "transacoes:criar", "user@npl.com", "10.0.0.1", 3, Duration.ofMinutes(1)));
        }

        assertThrows(RateLimitExceededException.class, () -> rateLimitService.enforce(
                "transacoes:criar", "user@npl.com", "10.0.0.1", 3, Duration.ofMinutes(1)));

        // Outro usuário no mesmo IP ainda consegue operar dentro do limite de IP
        assertDoesNotThrow(() -> rateLimitService.enforce(
                "transacoes:criar", "outro@npl.com", "10.0.0.1", 3, Duration.ofMinutes(1)));
    }

    @Test
    void deveExtrairPrimeiroIpDeXForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.10, 10.0.0.2");
        assertEquals("203.0.113.10", rateLimitService.resolveClientIp(request));
    }
}
