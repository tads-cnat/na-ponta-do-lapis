package com.npl.na_ponta_do_lapis.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FamiliaTest {

    private Familia familia;

    @BeforeEach
    void setUp() {
        familia = new Familia();
        familia.setId(1L);
        familia.setNome("Família Natanael");
        familia.setFotoFamilia("familia.png");
    }

    @Test
    void familiaNovaNaoDevePossuirAtributosPreenchidos() {
        Familia nova = new Familia();

        assertAll(
                () -> assertNull(nova.getId()),
                () -> assertNull(nova.getNome()),
                () -> assertNull(nova.getFotoFamilia()),
                () -> assertNull(nova.getMembros())
        );
    }

    @Test
    void settersEGettersDevemPreservarOsValoresAtribuidos() {
        Usuario membro = new Usuario();
        membro.setId(2L);
        List<Usuario> membros = List.of(membro);

        familia.setMembros(membros);

        assertAll(
                () -> assertEquals(1L, familia.getId()),
                () -> assertEquals("Família Natanael", familia.getNome()),
                () -> assertEquals("familia.png", familia.getFotoFamilia()),
                () -> assertSame(membros, familia.getMembros())
        );
    }

    @Test
    void equalsDeveRetornarTrueParaFamiliasComMesmoId() {
        Familia outra = new Familia();
        outra.setId(1L);

        assertEquals(familia, outra);
    }

    @Test
    void equalsDeveRetornarFalseParaFamiliasComIdsDiferentes() {
        Familia outra = new Familia();
        outra.setId(2L);

        assertNotEquals(familia, outra);
    }

    @Test
    void equalsDeveRetornarFalseQuandoComparadoComNull() {
        assertNotEquals(familia, null);
    }

    @Test
    void equalsDeveRetornarFalseQuandoComparadoComOutroTipoDeObjeto() {
        assertNotEquals(familia, "Não sou uma família");
    }

    @Test
    void hashCodeDeveSerIgualParaFamiliasComMesmoId() {
        Familia outra = new Familia();
        outra.setId(1L);

        assertEquals(familia.hashCode(), outra.hashCode());
    }
    @Test
    void equalsNaoDeveLancarExcecaoParaFamiliasSemId() {
        assertDoesNotThrow(() -> new Familia().equals(new Familia()));
    }
}