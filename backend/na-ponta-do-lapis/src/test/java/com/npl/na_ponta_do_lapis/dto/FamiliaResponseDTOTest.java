package com.npl.na_ponta_do_lapis.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.npl.na_ponta_do_lapis.entity.Familia;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.Papel;
import com.npl.na_ponta_do_lapis.web.dto.FamiliaResponseDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FamiliaResponseDTOTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deveMapearDadosDaFamilia() {
        FamiliaResponseDTO dto = new FamiliaResponseDTO(criarFamilia());

        assertAll(
                () -> assertEquals(5L, dto.id()),
                () -> assertEquals("Família Natanael", dto.nome()),
                () -> assertEquals("familia.png", dto.foto())
        );
    }

    @Test
    void membrosDeveSerListaVaziaQuandoFamiliaNaoTemMembros() {
        FamiliaResponseDTO dto = new FamiliaResponseDTO(criarFamilia());

        assertAll(
                () -> assertNotNull(dto.membros()),
                () -> assertTrue(dto.membros().isEmpty())
        );
    }

    @Test
    void deveMapearMembrosParaUsuarioResponseDTO() {
        Familia familia = criarFamilia();
        familia.setMembros(List.of(
                criarMembro(familia, 1L, "Bruno Natanael", "brunonatanael"),
                criarMembro(familia, 2L, "Ana Natanael", "ananatanael")
        ));

        FamiliaResponseDTO dto = new FamiliaResponseDTO(familia);

        assertAll(
                () -> assertEquals(2, dto.membros().size()),
                () -> assertEquals(1L, dto.membros().get(0).id()),
                () -> assertEquals("brunonatanael", dto.membros().get(0).username()),
                () -> assertEquals(5L, dto.membros().get(0).idFamilia()),
                () -> assertEquals(2L, dto.membros().get(1).id()),
                () -> assertEquals("ananatanael", dto.membros().get(1).username())
        );
    }

    @Test
    void deveSerializarFamiliaComoJson() throws Exception {
        Familia familia = criarFamilia();
        familia.setMembros(List.of(criarMembro(familia, 1L, "Bruno Natanael", "brunonatanael")));

        String json = objectMapper.writeValueAsString(new FamiliaResponseDTO(familia));

        JsonNode no = objectMapper.readTree(json);

        assertAll(
                () -> assertEquals(5L, no.get("id").asLong()),
                () -> assertEquals("Família Natanael", no.get("nome").asText()),
                () -> assertEquals("familia.png", no.get("foto").asText()),
                () -> assertEquals(1, no.get("membros").size()),
                () -> assertEquals("brunonatanael", no.get("membros").get(0).get("username").asText())
        );
    }

    private Familia criarFamilia() {
        Familia familia = new Familia();
        familia.setId(5L);
        familia.setNome("Família Natanael");
        familia.setFotoFamilia("familia.png");
        return familia;
    }

    private Usuario criarMembro(Familia familia, Long id, String nome, String username) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome(nome);
        usuario.setEmail(username + "@email.com");
        usuario.setUsername(username);
        usuario.setPapel(Papel.USUARIO);
        usuario.setFamilia(familia);
        return usuario;
    }
}