package com.npl.na_ponta_do_lapis.service.fatura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.npl.na_ponta_do_lapis.entity.PadraoFatura;
import com.npl.na_ponta_do_lapis.repository.PadraoFaturaRepository;

class PadraoFaturaServiceTest {

    @Test
    void novaVersaoDesativaAnteriorEIncrementaVersao() {
        PadraoFaturaRepository repository = mock(PadraoFaturaRepository.class);
        PadraoFaturaService service = new PadraoFaturaService(repository);

        PadraoFatura anterior = new PadraoFatura();
        anterior.setEmissor("Nubank");
        anterior.setVersao(3);
        anterior.setAtivo(true);

        PadraoFatura novo = new PadraoFatura();
        novo.setEmissor("Nubank");
        novo.setRegexIdentificacao("(?i)nubank");
        novo.setRegexTransacao("(?<descricao>.*)(?<data>\\d{2}/\\d{2})(?<valor>\\d+,\\d{2})");

        when(repository.findByEmissorAndAtivoTrueOrderByVersaoDesc("Nubank"))
                .thenReturn(List.of(anterior));
        when(repository.save(any(PadraoFatura.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PadraoFatura salvo = service.salvarNovaVersao(novo);

        assertThat(anterior.getAtivo()).isFalse();
        assertThat(salvo.getVersao()).isEqualTo(4);
        assertThat(salvo.getOrigem()).isEqualTo("IA");
        assertThat(salvo.getAtivo()).isTrue();
        verify(repository).save(anterior);
    }
}
