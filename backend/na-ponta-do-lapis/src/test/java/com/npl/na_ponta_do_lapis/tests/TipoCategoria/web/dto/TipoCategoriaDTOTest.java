package com.npl.na_ponta_do_lapis.tests.TipoCategoria.web.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.npl.na_ponta_do_lapis.entity.TipoCategoria;
import com.npl.na_ponta_do_lapis.web.dto.CategoriaDTO;
import com.npl.na_ponta_do_lapis.web.dto.CategoriaResumoDTO;
import com.npl.na_ponta_do_lapis.web.dto.TipoCategoriaDTO;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TipoCategoriaDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldConvertToEntity() {
        TipoCategoriaDTO dto = new TipoCategoriaDTO("Alimentação");

        TipoCategoria categoria = dto.toEntity();

        assertEquals("Alimentação", categoria.getNome());
        assertNull(categoria.getId());
    }

    @Test
    void shouldAcceptValidName() {
        TipoCategoriaDTO dto = new TipoCategoriaDTO("Alimentação");

        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void shouldRejectNullOrBlankName() {
        TipoCategoriaDTO nullName = new TipoCategoriaDTO(null);
        TipoCategoriaDTO blankName = new TipoCategoriaDTO("  ");

        assertFalse(validator.validate(nullName).isEmpty());
        assertFalse(validator.validate(blankName).isEmpty());
    }

    @Test
    void shouldDeserializeJsonIntoDto() throws Exception {
        TipoCategoriaDTO dto = objectMapper.readValue(
                "{\"nome\":\"Alimentação\"}",
                TipoCategoriaDTO.class
        );

        assertEquals("Alimentação", dto.nome());
    }

    @Test
    void shouldSerializeCategoryDtoAsJson() throws Exception {
        CategoriaDTO dto = new CategoriaDTO(7L, "Alimentação");

        String json = objectMapper.writeValueAsString(dto);

        assertEquals("{\"id\":7,\"nome\":\"Alimentação\"}", json);
    }

    @Test
    void shouldSerializeCategorySummaryDtoAsJson() throws Exception {
        CategoriaResumoDTO dto = new CategoriaResumoDTO(7L, "Alimentação");

        String json = objectMapper.writeValueAsString(dto);

        assertEquals("{\"id\":7,\"nome\":\"Alimentação\"}", json);
    }
}
