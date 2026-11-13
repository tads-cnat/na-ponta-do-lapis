package com.npl.na_ponta_do_lapis.security;

import com.npl.na_ponta_do_lapis.config.CacheConfig;
import com.npl.na_ponta_do_lapis.entity.Familia;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.Moeda;
import com.npl.na_ponta_do_lapis.entity.enums.Papel;
import com.npl.na_ponta_do_lapis.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioAuthCacheServiceTest {

    private UsuarioRepository usuarioRepository;
    private CacheManager cacheManager;
    private UsuarioAuthCacheService service;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        cacheManager = new ConcurrentMapCacheManager(CacheConfig.CACHE_USUARIO_AUTH);

        @SuppressWarnings("unchecked")
        ObjectProvider<CacheManager> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(cacheManager);

        service = new UsuarioAuthCacheService(usuarioRepository, provider);
    }

    @Test
    void deveBuscarNoBancoNoCacheMissEReconstruirDoCacheNoHit() {
        Usuario usuario = criarUsuarioExemplo();
        when(usuarioRepository.findByEmailIgnoreCase("Ana@Email.com")).thenReturn(Optional.of(usuario));

        Optional<Usuario> primeiraBusca = service.buscarPorIdentificador("Ana@Email.com");
        assertTrue(primeiraBusca.isPresent());
        assertEquals(10L, primeiraBusca.get().getId());
        assertEquals("Família Silva", primeiraBusca.get().getFamilia().getNome());

        Optional<Usuario> segundaBusca = service.buscarPorIdentificador("ana@email.com");
        assertTrue(segundaBusca.isPresent());
        assertEquals(10L, segundaBusca.get().getId());
        assertEquals(Papel.ADMIN_FAMILIA, segundaBusca.get().getPapel());
        assertEquals(Moeda.BRL, segundaBusca.get().getMoedaPadrao());
        assertEquals(20L, segundaBusca.get().getFamilia().getId());

        verify(usuarioRepository, times(1)).findByEmailIgnoreCase("Ana@Email.com");
        verify(usuarioRepository, never()).findByEmailIgnoreCase("ana@email.com");
    }

    @Test
    void deveRecorrerAoBancoQuandoLeituraDoCacheFalhar() {
        Cache cacheQuebrado = mock(Cache.class);
        when(cacheQuebrado.get("ana@email.com", UsuarioAuthCacheDTO.class))
                .thenThrow(new RuntimeException("Redis timeout"));

        CacheManager managerQuebrado = mock(CacheManager.class);
        when(managerQuebrado.getCache(CacheConfig.CACHE_USUARIO_AUTH)).thenReturn(cacheQuebrado);

        @SuppressWarnings("unchecked")
        ObjectProvider<CacheManager> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(managerQuebrado);

        UsuarioAuthCacheService resiliente = new UsuarioAuthCacheService(usuarioRepository, provider);
        Usuario usuario = criarUsuarioExemplo();
        when(usuarioRepository.findByEmailIgnoreCase("ana@email.com")).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = resiliente.buscarPorIdentificador("ana@email.com");
        assertTrue(resultado.isPresent());
        assertEquals("ana@email.com", resultado.get().getEmail());
        verify(usuarioRepository, times(1)).findByEmailIgnoreCase("ana@email.com");
    }

    @Test
    void deveInvalidarChavesDeEmailEUsernameAoEvictPorUsuario() {
        Usuario usuario = criarUsuarioExemplo();
        when(usuarioRepository.findByEmailIgnoreCase("ana@email.com")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByEmailIgnoreCase("anasilva")).thenReturn(Optional.empty());
        when(usuarioRepository.findByUsernameIgnoreCase("anasilva")).thenReturn(Optional.of(usuario));

        assertTrue(service.buscarPorIdentificador("ana@email.com").isPresent());
        assertTrue(service.buscarPorIdentificador("anasilva").isPresent());

        service.evictPorUsuario(usuario);

        Cache cache = cacheManager.getCache(CacheConfig.CACHE_USUARIO_AUTH);
        assertNotNull(cache);
        assertNull(cache.get("ana@email.com"));
        assertNull(cache.get("anasilva"));
    }

    private Usuario criarUsuarioExemplo() {
        Familia familia = new Familia();
        familia.setId(20L);
        familia.setNome("Família Silva");
        familia.setFotoFamilia("foto-fam.png");

        Usuario usuario = new Usuario();
        usuario.setId(10L);
        usuario.setNome("Ana Silva");
        usuario.setEmail("ana@email.com");
        usuario.setUsername("anasilva");
        usuario.setSenha("hash-seguro");
        usuario.setTelefone("11999999999");
        usuario.setFotoPerfil("avatar.png");
        usuario.setPapel(Papel.ADMIN_FAMILIA);
        usuario.setMoedaPadrao(Moeda.BRL);
        usuario.setFamilia(familia);
        return usuario;
    }
}

