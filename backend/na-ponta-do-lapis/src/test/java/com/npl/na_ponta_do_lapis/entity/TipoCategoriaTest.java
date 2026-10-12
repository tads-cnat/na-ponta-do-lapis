package com.npl.na_ponta_do_lapis.entity;

import org.junit.jupiter.api.Test;

import com.npl.na_ponta_do_lapis.entity.TipoCategoria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TipoCategoriaTest {

    @Test
    void shouldStoreAndReturnIdAndName() {
        TipoCategoria categoria = new TipoCategoria();

        categoria.setId(7L);
        categoria.setNome("Alimentação");

        assertEquals(7L, categoria.getId());
        assertEquals("Alimentação", categoria.getNome());
    }

    @Test
    void shouldUseIdForEqualityAndHashCode() {
        TipoCategoria categoria = new TipoCategoria();
        TipoCategoria mesmaCategoria = new TipoCategoria();
        TipoCategoria outraCategoria = new TipoCategoria();
        categoria.setId(7L);
        mesmaCategoria.setId(7L);
        outraCategoria.setId(8L);

        assertEquals(categoria, mesmaCategoria);
        assertEquals(categoria.hashCode(), mesmaCategoria.hashCode());
        assertNotEquals(categoria, outraCategoria);
        assertNotEquals(categoria, null);
        assertNotEquals(categoria, new Object());
    }

    @Test
    void shouldTreatCategoriesWithoutIdAsEqual() {
        TipoCategoria primeira = new TipoCategoria();
        TipoCategoria segunda = new TipoCategoria();
        primeira.setNome("Alimentação");
        segunda.setNome("Transporte");

        assertEquals(primeira, segunda);
        assertEquals(primeira.hashCode(), segunda.hashCode());
    }

    @Test
    void shouldIncludeIdAndNameInToString() {
        TipoCategoria categoria = new TipoCategoria();
        categoria.setId(7L);
        categoria.setNome("Alimentação");

        assertEquals("TipoCategoria{id=7, nome='Alimentação'}", categoria.toString());
    }

    @Test
    void shouldHaveNullFieldsWhenCreated() {
        TipoCategoria categoria = new TipoCategoria();

        assertNull(categoria.getId());
        assertNull(categoria.getNome());
    }
}
