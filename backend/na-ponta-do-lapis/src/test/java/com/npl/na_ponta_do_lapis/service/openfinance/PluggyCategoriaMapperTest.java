package com.npl.na_ponta_do_lapis.service.openfinance;

import com.npl.na_ponta_do_lapis.entity.TipoCategoria;
import com.npl.na_ponta_do_lapis.repository.TipoCategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PluggyCategoriaMapperTest {

    @Mock
    private TipoCategoriaRepository tipoCategoriaRepository;

    @InjectMocks
    private PluggyCategoriaMapper mapper;

    @BeforeEach
    void setUp() {
        when(tipoCategoriaRepository.findByNome(anyString())).thenAnswer(invocation -> {
            String nome = invocation.getArgument(0);
            TipoCategoria cat = new TipoCategoria();
            cat.setNome(nome);
            return Optional.of(cat);
        });
    }

    @Test
    @DisplayName("Deve mapear corretamente alimentação e restaurantes")
    void deveMapearAlimentacao() {
        assertEquals("Alimentação", mapper.mapearCategoria("Restaurants", "Almoço no shopping").getNome());
        assertEquals("Alimentação", mapper.mapearCategoria(null, "PADARIA DO ZE").getNome());
        assertEquals("Alimentação", mapper.mapearCategoria("Food & Drink", "Ifood pedido").getNome());
        assertEquals("Alimentação", mapper.mapearCategoria(null, "SUPERMERCADO DIA").getNome());
    }

    @Test
    @DisplayName("Deve mapear transporte, combustível e viagens")
    void deveMapearTransporte() {
        assertEquals("Transporte", mapper.mapearCategoria("Transportation", "Corrida").getNome());
        assertEquals("Transporte", mapper.mapearCategoria(null, "UBER TRIP").getNome());
        assertEquals("Transporte", mapper.mapearCategoria(null, "POSTO SHELL COMBUSTIVEL").getNome());
        assertEquals("Transporte", mapper.mapearCategoria(null, "ESTAPAR ESTACIONAMENTO").getNome());
    }

    @Test
    @DisplayName("Deve mapear saúde e farmácia")
    void deveMapearSaude() {
        assertEquals("Saúde", mapper.mapearCategoria("Health", "Exames").getNome());
        assertEquals("Saúde", mapper.mapearCategoria(null, "DROGASIL FARMACIA").getNome());
        assertEquals("Saúde", mapper.mapearCategoria(null, "CONSULTA MEDICA").getNome());
    }

    @Test
    @DisplayName("Deve mapear educação")
    void deveMapearEducacao() {
        assertEquals("Educação", mapper.mapearCategoria("Education", "Faculdade").getNome());
        assertEquals("Educação", mapper.mapearCategoria(null, "CURSO ALURA ONLINE").getNome());
    }

    @Test
    @DisplayName("Deve mapear lazer, streaming e entretenimento")
    void deveMapearLazer() {
        assertEquals("Lazer", mapper.mapearCategoria("Entertainment", "Cinema").getNome());
        assertEquals("Lazer", mapper.mapearCategoria(null, "NETFLIX ASSINATURA").getNome());
        assertEquals("Lazer", mapper.mapearCategoria(null, "SPOTIFY PREMIUM").getNome());
        assertEquals("Lazer", mapper.mapearCategoria(null, "STEAM GAMES").getNome());
    }

    @Test
    @DisplayName("Deve mapear para Outros quando descrição ou categoria não casarem")
    void deveMapearParaOutros() {
        assertEquals("Outros", mapper.mapearCategoria(null, "TRANSFERENCIA PIX ALEATORIA").getNome());
        assertEquals("Outros", mapper.mapearCategoria("Miscellaneous", "COMPRA DIVERSA").getNome());
        assertEquals("Outros", mapper.mapearCategoria(null, null).getNome());
    }
}
