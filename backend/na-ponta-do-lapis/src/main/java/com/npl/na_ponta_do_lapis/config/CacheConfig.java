package com.npl.na_ponta_do_lapis.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.npl.na_ponta_do_lapis.web.dto.CategoriaDTO;
import com.npl.na_ponta_do_lapis.security.UsuarioAuthCacheDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    public static final String CACHE_USUARIO_AUTH = "usuario_auth";
    public static final String CACHE_CATEGORIAS = "categorias";

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Falha ao ler do cache '{}' (chave='{}'): {}. Recorrendo ao banco.",
                        cacheName(cache), key, exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Falha ao gravar no cache '{}' (chave='{}'): {}",
                        cacheName(cache), key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Falha ao invalidar cache '{}' (chave='{}'): {}",
                        cacheName(cache), key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Falha ao limpar cache '{}': {}",
                        cacheName(cache), exception.getMessage());
            }

            private String cacheName(Cache cache) {
                return cache != null ? cache.getName() : "desconhecido";
            }
        };
    }

    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        ObjectMapper mapper = JsonMapper.builder()
                .findAndAddModules()
                .build();

        Jackson2JsonRedisSerializer<UsuarioAuthCacheDTO> usuarioSerializer =
                new Jackson2JsonRedisSerializer<>(mapper, UsuarioAuthCacheDTO.class);

        JavaType categoriasListType = mapper.getTypeFactory()
                .constructCollectionType(List.class, CategoriaDTO.class);
        Jackson2JsonRedisSerializer<List<CategoriaDTO>> categoriasSerializer =
                new Jackson2JsonRedisSerializer<>(mapper, categoriasListType);

        RedisCacheConfiguration usuarioConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(usuarioSerializer));

        RedisCacheConfiguration categoriasConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(categoriasSerializer));

        return builder -> builder
                .withCacheConfiguration(CACHE_USUARIO_AUTH, usuarioConfig)
                .withCacheConfiguration(CACHE_CATEGORIAS, categoriasConfig);
    }
}
