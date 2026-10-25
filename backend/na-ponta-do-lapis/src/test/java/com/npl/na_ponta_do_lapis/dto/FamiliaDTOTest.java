package com.npl.na_ponta_do_lapis.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.npl.na_ponta_do_lapis.entity.Familia;
import com.npl.na_ponta_do_lapis.web.dto.FamiliaDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FamiliaDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void toEntityDeveCopiarNomeEFotoParaAFamilia() {
        FamiliaDTO dto = new FamiliaDTO("Família Natanael", "familia.png");

        Familia familia = dto.toEntity();

        assertAll(
                () -> assertEquals("Família Natanael", familia.getNome()),
                () -> assertEquals("familia.png", familia.getFotoFamilia()),
                () -> assertNull(familia.getId()),
                () -> assertNull(familia.getMembros())
        );
    }

    @Test
    void toEntityDevePermitirFotoNula() {
        FamiliaDTO dto = new FamiliaDTO("Família Natanael", null);

        Familia familia = dto.toEntity();

        assertAll(
                () -> assertEquals("Família Natanael", familia.getNome()),
                () -> assertNull(familia.getFotoFamilia())
        );
    }

    @Test
    void deveDesserializarJsonParaDto() throws Exception {
        FamiliaDTO dto = objectMapper.readValue(
                "{\"nome\":\"Família Natanael\",\"foto\":\"familia.png\"}",
                FamiliaDTO.class
        );

        assertAll(
                () -> assertEquals("Família Natanael", dto.nome()),
                () -> assertEquals("familia.png", dto.foto())
        );
    }
}