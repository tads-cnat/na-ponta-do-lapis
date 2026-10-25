package com.npl.na_ponta_do_lapis.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.npl.na_ponta_do_lapis.web.dto.ConviteDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConviteDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deveExporUsernameDoDestinatarioEIdDaFamilia() {
        ConviteDTO dto = new ConviteDTO("brunonatanael", 5L);

        assertAll(
                () -> assertEquals("brunonatanael", dto.destinatarioUsername()),
                () -> assertEquals(5L, dto.familiaId())
        );
    }

    @Test
    void deveDesserializarJsonParaDto() throws Exception {
        ConviteDTO dto = objectMapper.readValue(
                "{\"destinatarioUsername\":\"brunonatanael\",\"familiaId\":5}",
                ConviteDTO.class
        );

        assertAll(
                () -> assertEquals("brunonatanael", dto.destinatarioUsername()),
                () -> assertEquals(5L, dto.familiaId())
        );
    }
}