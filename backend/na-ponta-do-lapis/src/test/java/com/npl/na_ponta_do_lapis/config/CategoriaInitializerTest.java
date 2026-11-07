package com.npl.na_ponta_do_lapis.config;

import com.npl.na_ponta_do_lapis.entity.TipoCategoria;
import com.npl.na_ponta_do_lapis.repository.TipoCategoriaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaInitializerTest {

    @Mock
    private TipoCategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaInitializer categoriaInitializer;

    @Captor
    private ArgumentCaptor<TipoCategoria> categoriaCaptor;

    @Test
    @DisplayName("Deve inicializar todas as categorias padrão quando o banco estiver sem elas")
    void deveInicializarCategoriasPadraoQuandoBancoVazio() {
        when(categoriaRepository.findByNome(anyString())).thenReturn(Optional.empty());

        categoriaInitializer.run();

        verify(categoriaRepository, times(6)).save(categoriaCaptor.capture());
        List<String> nomesSalvos = categoriaCaptor.getAllValues().stream()
                .map(TipoCategoria::getNome)
                .toList();

        assertThat(nomesSalvos).containsExactlyInAnyOrder(
                "Alimentação",
                "Transporte",
                "Saúde",
                "Educação",
                "Lazer",
                "Outros"
        );
    }

    @Test
    @DisplayName("Não deve reinserir categorias que já existem no banco")
    void naoDeveDuplicarCategoriasExistentes() {
        when(categoriaRepository.findByNome("Alimentação")).thenReturn(Optional.of(new TipoCategoria()));
        when(categoriaRepository.findByNome("Transporte")).thenReturn(Optional.of(new TipoCategoria()));
        when(categoriaRepository.findByNome("Saúde")).thenReturn(Optional.of(new TipoCategoria()));
        when(categoriaRepository.findByNome("Educação")).thenReturn(Optional.of(new TipoCategoria()));
        when(categoriaRepository.findByNome("Lazer")).thenReturn(Optional.of(new TipoCategoria()));
        when(categoriaRepository.findByNome("Outros")).thenReturn(Optional.of(new TipoCategoria()));

        categoriaInitializer.run();

        verify(categoriaRepository, never()).save(any());
    }
}

