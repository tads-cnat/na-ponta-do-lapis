package com.npl.na_ponta_do_lapis.service;

import com.npl.na_ponta_do_lapis.entity.ChaveIaUsuario;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.repository.ChaveIaUsuarioRepository;
import com.npl.na_ponta_do_lapis.service.crypto.CryptoService;
import com.npl.na_ponta_do_lapis.service.fatura.ia.ProvedorIa;
import com.npl.na_ponta_do_lapis.web.dto.ChaveIaRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.ChaveIaResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChaveIaServiceImplTest {

    @Mock
    private ChaveIaUsuarioRepository repository;

    @Mock
    private CryptoService cryptoService;

    @InjectMocks
    private ChaveIaServiceImpl service;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Tester");
    }

    @Test
    @DisplayName("Primeira chave cadastrada deve se tornar padrão automaticamente")
    void primeiraChaveDeveSerPadrao() {
        when(repository.findByUsuarioIdOrderByCriadoEmDesc(1L)).thenReturn(List.of());
        when(cryptoService.encrypt("AIzaSy123")).thenReturn("cifrado123");
        when(cryptoService.decrypt("cifrado123")).thenReturn("AIzaSy123");
        when(cryptoService.mask("AIzaSy123")).thenReturn("AIza••••••••y123");
        when(repository.save(any(ChaveIaUsuario.class))).thenAnswer(inv -> {
            ChaveIaUsuario c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        ChaveIaRequestDTO dto = new ChaveIaRequestDTO("Gemini Teste", ProvedorIa.GEMINI, "AIzaSy123", false);
        ChaveIaResponseDTO response = service.cadastrarChave(usuario, dto);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.apelido()).isEqualTo("Gemini Teste");
        assertThat(response.padrao()).isTrue();
        assertThat(response.chaveMascarada()).isEqualTo("AIza••••••••y123");
    }

    @Test
    @DisplayName("Deve desmarcar padrão anterior se nova chave for marcada como padrão")
    void deveDesmarcarPadraoAnterior() {
        ChaveIaUsuario chaveAntiga = new ChaveIaUsuario(usuario, "Antiga", ProvedorIa.OPENAI, "cifra", true);
        when(repository.findByUsuarioIdOrderByCriadoEmDesc(1L)).thenReturn(List.of(chaveAntiga));
        when(repository.findByUsuarioIdAndPadraoTrue(1L)).thenReturn(Optional.of(chaveAntiga));
        when(cryptoService.encrypt("nova-chave")).thenReturn("cifra-nova");
        when(cryptoService.decrypt("cifra-nova")).thenReturn("nova-chave");
        when(cryptoService.mask("nova-chave")).thenReturn("nova••••••••have");
        when(repository.save(any(ChaveIaUsuario.class))).thenAnswer(inv -> inv.getArgument(0));

        ChaveIaRequestDTO dto = new ChaveIaRequestDTO("Claude Novo", ProvedorIa.CLAUDE, "nova-chave", true);
        ChaveIaResponseDTO response = service.cadastrarChave(usuario, dto);

        assertThat(response.padrao()).isTrue();
        assertThat(chaveAntiga.isPadrao()).isFalse();
    }

    @Test
    @DisplayName("Deve listar chaves do usuário com valores mascarados")
    void deveListarChavesComMascara() {
        ChaveIaUsuario c1 = new ChaveIaUsuario(usuario, "Gemini", ProvedorIa.GEMINI, "c1", true);
        c1.setId(1L);

        when(repository.findByUsuarioIdOrderByCriadoEmDesc(1L)).thenReturn(List.of(c1));
        when(cryptoService.decrypt("c1")).thenReturn("AIzaSecret1");
        when(cryptoService.mask("AIzaSecret1")).thenReturn("AIza••••••••ret1");

        List<ChaveIaResponseDTO> lista = service.listarChaves(usuario);

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).chaveMascarada()).isEqualTo("AIza••••••••ret1");
    }

    @Test
    @DisplayName("Deve definir nova chave como padrão")
    void deveDefinirComoPadrao() {
        ChaveIaUsuario antiga = new ChaveIaUsuario(usuario, "Antiga", ProvedorIa.GEMINI, "c1", true);
        ChaveIaUsuario nova = new ChaveIaUsuario(usuario, "Nova", ProvedorIa.CLAUDE, "c2", false);

        when(repository.findByIdAndUsuarioId(2L, 1L)).thenReturn(Optional.of(nova));
        when(repository.findByUsuarioIdAndPadraoTrue(1L)).thenReturn(Optional.of(antiga));

        service.definirComoPadrao(usuario, 2L);

        assertThat(antiga.isPadrao()).isFalse();
        assertThat(nova.isPadrao()).isTrue();
    }

    @Test
    @DisplayName("Deve deletar chave e eleger nova padrão se a deletada era a padrão")
    void deveDeletarChaveEElegerNovaPadrao() {
        ChaveIaUsuario chaveParaDeletar = new ChaveIaUsuario(usuario, "Padrao", ProvedorIa.GEMINI, "c1", true);
        chaveParaDeletar.setId(10L);

        ChaveIaUsuario chaveRestante = new ChaveIaUsuario(usuario, "Outra", ProvedorIa.OPENAI, "c2", false);
        chaveRestante.setId(20L);

        when(repository.findByIdAndUsuarioId(10L, 1L)).thenReturn(Optional.of(chaveParaDeletar));
        when(repository.findByUsuarioIdOrderByCriadoEmDesc(1L)).thenReturn(new ArrayList<>(List.of(chaveRestante)));

        service.deletarChave(usuario, 10L);

        verify(repository).delete(chaveParaDeletar);
        assertThat(chaveRestante.isPadrao()).isTrue();
        verify(repository).save(chaveRestante);
    }

    @Test
    @DisplayName("Deve lançar exceção se chave não pertencer ao usuário")
    void deveLancarExcecaoSeChaveNaoPertencerAoUsuario() {
        when(repository.findByIdAndUsuarioId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obterChaveDecifrada(usuario, 99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Chave de IA não encontrada");
    }
}

