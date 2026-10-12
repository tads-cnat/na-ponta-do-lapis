package com.npl.na_ponta_do_lapis.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.npl.na_ponta_do_lapis.entity.Marcador;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.web.dto.MarcadorResponseDTO;

@DisplayName("MarcadorResponseDTO (saída)")
class MarcadorResponseDTOTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private static Marcador marcadorCompleto() {
        Usuario usuario = new Usuario();
        usuario.setId(7L);
        usuario.setEmail("pedro@email.com");

        Marcador marcador = new Marcador();
        marcador.setId(10L);
        marcador.setNome("Trabalho");
        marcador.setCor("#FF0000");
        marcador.setUsuario(usuario);
        return marcador;
    }

    @Test
    @DisplayName("mapeia id, nome e cor do marcador")
    void deveMapearTodosOsCampos() {
        MarcadorResponseDTO dto = new MarcadorResponseDTO(marcadorCompleto());

        assertAll(
                () -> assertThat(dto.id()).isEqualTo(10L),
                () -> assertThat(dto.nome()).isEqualTo("Trabalho"),
                () -> assertThat(dto.cor()).isEqualTo("#FF0000")
        );
    }

    @Test
    @DisplayName("id é null quando o marcador ainda não foi persistido")
    void idDeveSerNullSemPersistencia() {
        Marcador marcador = marcadorCompleto();
        marcador.setId(null);

        assertThat(new MarcadorResponseDTO(marcador).id()).isNull();
    }

    @Test
    @DisplayName("lança NullPointerException quando o marcador é nulo")
    void deveFalharQuandoMarcadorForNulo() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> new MarcadorResponseDTO((Marcador) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("serializa apenas id, nome e cor, sem expor o usuário")
    void deveSerializarSemExporUsuario() throws Exception {
        String json = mapper.writeValueAsString(new MarcadorResponseDTO(marcadorCompleto()));

        assertThat(json).isEqualTo("{\"id\":10,\"nome\":\"Trabalho\",\"cor\":\"#FF0000\"}");
    }
}
