package com.npl.na_ponta_do_lapis.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.npl.na_ponta_do_lapis.entity.Convite;
import com.npl.na_ponta_do_lapis.entity.Familia;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.Papel;
import com.npl.na_ponta_do_lapis.entity.enums.StatusConvite;
import com.npl.na_ponta_do_lapis.web.dto.ConviteResponseDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConviteResponseDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deveMapearDadosDoConvite() {
        ConviteResponseDTO dto = new ConviteResponseDTO(criarConvite());

        assertAll(
                () -> assertEquals(10L, dto.id()),
                () -> assertEquals(StatusConvite.PENDENTE, dto.status()),
                () -> assertEquals(2L, dto.destinatario().id()),
                () -> assertEquals("brunonatanael", dto.destinatario().username()),
                () -> assertEquals(5L, dto.familia().id()),
                () -> assertEquals("Família Natanael", dto.familia().nome())
        );
    }

    @Test
    void destinatarioDeveSerNuloQuandoConviteNaoTemDestinatario() {
        Convite convite = criarConvite();
        convite.setDestinatario(null);

        ConviteResponseDTO dto = new ConviteResponseDTO(convite);

        assertNull(dto.destinatario());
    }

    @Test
    void familiaDeveSerNulaQuandoConviteNaoTemFamilia() {
        Convite convite = criarConvite();
        convite.setFamilia(null);

        ConviteResponseDTO dto = new ConviteResponseDTO(convite);

        assertNull(dto.familia());
    }

    @Test
    void deveSerializarConviteComoJson() throws Exception {
        String json = objectMapper.writeValueAsString(new ConviteResponseDTO(criarConvite()));

        JsonNode no = objectMapper.readTree(json);

        assertAll(
                () -> assertEquals(10L, no.get("id").asLong()),
                () -> assertEquals("PENDENTE", no.get("status").asText()),
                () -> assertEquals(2L, no.get("destinatario").get("id").asLong()),
                () -> assertEquals(5L, no.get("familia").get("id").asLong())
        );
    }

    private Convite criarConvite() {
        Usuario destinatario = new Usuario();
        destinatario.setId(2L);
        destinatario.setNome("Bruno Natanael");
        destinatario.setEmail("bruno@email.com");
        destinatario.setUsername("brunonatanael");
        destinatario.setPapel(Papel.USUARIO);

        Familia familia = new Familia();
        familia.setId(5L);
        familia.setNome("Família Natanael");
        familia.setFotoFamilia("familia.png");

        Convite convite = new Convite();
        convite.setId(10L);
        convite.setDestinatario(destinatario);
        convite.setFamilia(familia);
        convite.setStatus(StatusConvite.PENDENTE);
        return convite;
    }
}