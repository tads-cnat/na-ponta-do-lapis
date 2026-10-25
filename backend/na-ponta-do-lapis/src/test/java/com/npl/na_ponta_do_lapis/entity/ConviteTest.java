package com.npl.na_ponta_do_lapis.entity;

import com.npl.na_ponta_do_lapis.entity.enums.StatusConvite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class ConviteTest {

    private Convite convite;

    @BeforeEach
    void setUp() {
        convite = new Convite();
    }

    @Test
    void conviteNovoNaoDevePossuirAtributosPreenchidos() {
        assertAll(
                () -> assertNull(convite.getId()),
                () -> assertNull(convite.getDestinatario()),
                () -> assertNull(convite.getFamilia()),
                () -> assertNull(convite.getStatus())
        );
    }

    @Test
    void settersEGettersDevemPreservarOsValoresAtribuidos() {
        Usuario destinatario = new Usuario();
        destinatario.setId(2L);
        Familia familia = new Familia();
        familia.setId(5L);

        convite.setId(10L);
        convite.setDestinatario(destinatario);
        convite.setFamilia(familia);
        convite.setStatus(StatusConvite.PENDENTE);

        assertAll(
                () -> assertEquals(10L, convite.getId()),
                () -> assertSame(destinatario, convite.getDestinatario()),
                () -> assertSame(familia, convite.getFamilia()),
                () -> assertEquals(StatusConvite.PENDENTE, convite.getStatus())
        );
    }

    @ParameterizedTest
    @EnumSource(StatusConvite.class)
    void statusDevePoderSerAlteradoParaQualquerValorDoEnum(StatusConvite status) {
        convite.setStatus(StatusConvite.PENDENTE);

        convite.setStatus(status);

        assertEquals(status, convite.getStatus());
    }
}