package com.npl.na_ponta_do_lapis.service;

import com.npl.na_ponta_do_lapis.entity.Conquista;
import com.npl.na_ponta_do_lapis.entity.Meta;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.UsuarioConquista;
import com.npl.na_ponta_do_lapis.entity.enums.CategoriaConquista;
import com.npl.na_ponta_do_lapis.entity.enums.StatusMeta;
import com.npl.na_ponta_do_lapis.entity.enums.TipoMeta;
import com.npl.na_ponta_do_lapis.repository.AporteMetaRepository;
import com.npl.na_ponta_do_lapis.repository.ConquistaRepository;
import com.npl.na_ponta_do_lapis.repository.MetaRepository;
import com.npl.na_ponta_do_lapis.repository.UsuarioConquistaRepository;
import com.npl.na_ponta_do_lapis.web.dto.PerfilGamificadoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConquistaServiceTest {

    @Mock
    private ConquistaRepository conquistaRepository;

    @Mock
    private UsuarioConquistaRepository usuarioConquistaRepository;

    @Mock
    private MetaRepository metaRepository;

    @Mock
    private AporteMetaRepository aporteMetaRepository;

    @Mock
    private NotificacaoService notificacaoService;

    @InjectMocks
    private ConquistaService conquistaService;

    private Usuario usuario;
    private Conquista conquista1;
    private Conquista conquista2;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Lucas Henrique");

        conquista1 = new Conquista("PRIMEIRA_META", "Primeiro Passo", "Desc", "pi-flag", 50, CategoriaConquista.METAS);
        conquista2 = new Conquista("META_BATIDA_1", "Alvo Conquistado", "Desc", "pi-check-circle", 150, CategoriaConquista.METAS);
    }

    @Test
    @DisplayName("Deve calcular nível e XP do perfil gamificado corretamente")
    void deveObterPerfilGamificadoCorretamente() {
        UsuarioConquista uc1 = new UsuarioConquista(usuario, conquista1, LocalDateTime.now());
        UsuarioConquista uc2 = new UsuarioConquista(usuario, conquista2, LocalDateTime.now());

        when(conquistaRepository.findAll()).thenReturn(List.of(conquista1, conquista2));
        when(usuarioConquistaRepository.findByUsuarioIdOrderByDataDesbloqueioDesc(1L)).thenReturn(List.of(uc1, uc2));
        when(metaRepository.countByUsuarioIdAndStatus(1L, StatusMeta.CONCLUIDA)).thenReturn(1L);

        PerfilGamificadoDTO perfil = conquistaService.obterPerfilGamificado(1L);

        assertThat(perfil.totalXp()).isEqualTo(200);
        assertThat(perfil.nivel()).isEqualTo(2); // 200 XP entra no Nível 2
        assertThat(perfil.tituloNivel()).isEqualTo("Poupador em Foco");
        assertThat(perfil.totalConquistasDesbloqueadas()).isEqualTo(2);
        assertThat(perfil.totalMetasConcluidas()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve desbloquear conquista com sucesso se o usuário ainda não possuir")
    void deveDesbloquearConquistaComSucesso() {
        when(usuarioConquistaRepository.existsByUsuarioIdAndConquistaId(1L, "PRIMEIRA_META")).thenReturn(false);
        when(conquistaRepository.findById("PRIMEIRA_META")).thenReturn(Optional.of(conquista1));

        boolean desbloqueou = conquistaService.desbloquear(usuario, "PRIMEIRA_META");

        assertThat(desbloqueou).isTrue();
        verify(usuarioConquistaRepository).save(any(UsuarioConquista.class));
        verify(notificacaoService).criarNotificacao(eq(usuario), anyString(), anyString(), any(), eq("/perfil?aba=conquistas"));
    }

    @Test
    @DisplayName("Não deve desbloquear novamente conquista já desbloqueada")
    void naoDeveDesbloquearConquistaJaDesbloqueada() {
        when(usuarioConquistaRepository.existsByUsuarioIdAndConquistaId(1L, "PRIMEIRA_META")).thenReturn(true);

        boolean desbloqueou = conquistaService.desbloquear(usuario, "PRIMEIRA_META");

        assertThat(desbloqueou).isFalse();
        verify(usuarioConquistaRepository, never()).save(any());
        verify(notificacaoService, never()).criarNotificacao(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Deve disparar conquistas na criação da meta de gastos e primeira meta")
    void deveDispararConquistasNaCriacaoMeta() {
        Meta meta = new Meta();
        meta.setId(10L);
        meta.setTipoMeta(TipoMeta.GASTO);
        meta.setUsuario(usuario);

        when(metaRepository.countByUsuarioId(1L)).thenReturn(1L);
        when(usuarioConquistaRepository.existsByUsuarioIdAndConquistaId(eq(1L), anyString())).thenReturn(false);
        when(conquistaRepository.findById("PRIMEIRA_META")).thenReturn(Optional.of(conquista1));
        when(conquistaRepository.findById("ORCAMENTO_BLINDADO")).thenReturn(Optional.of(
                new Conquista("ORCAMENTO_BLINDADO", "Orçamento Blindado", "Desc", "pi-shield", 75, CategoriaConquista.DISCIPLINA)
        ));

        conquistaService.verificarConquistasCriacaoMeta(meta, usuario);

        verify(usuarioConquistaRepository, atLeast(2)).save(any(UsuarioConquista.class));
    }
}

