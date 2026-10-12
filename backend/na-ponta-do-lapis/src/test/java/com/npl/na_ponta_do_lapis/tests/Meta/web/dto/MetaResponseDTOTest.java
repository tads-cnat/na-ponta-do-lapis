package com.npl.na_ponta_do_lapis.tests.Meta.web.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.Meta;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.TipoMeta;
import com.npl.na_ponta_do_lapis.web.dto.MetaResponseDTO;

@DisplayName("MetaResponseDTO (saída)")
class MetaResponseDTOTest {

    private static Usuario usuarioComId(Long id) {
        Usuario u = new Usuario();
        u.setId(id);
        return u;
    }

    private static ContaFinanceira contaComId(Long id) {
        ContaFinanceira c = new ContaFinanceira();
        c.setId(id);
        return c;
    }

    private static Meta metaCompleta() {
        Meta meta = new Meta();
        meta.setId(10L);
        meta.setNome("Reserva de Emergência");
        meta.setDescricao("Guardar dinheiro para imprevistos");
        meta.setValor(new BigDecimal("5000.00"));
        meta.setValorAtual(new BigDecimal("1250.00"));
        meta.setFotoUrl("https://link-da-foto.com/image.png");
        meta.setDataLimite(LocalDate.of(2026, 12, 31));
        meta.setTipoMeta(TipoMeta.POUPANCA);
        meta.setUsuario(usuarioComId(7L));
        meta.setConta(contaComId(3L));
        return meta;
    }

    @Test
    @DisplayName("mapeia todos os campos da Meta")
    void deveMapearTodosOsCampos() {
        Meta meta = metaCompleta();

        MetaResponseDTO dto = new MetaResponseDTO(meta, 25.0);

        assertAll(
                () -> assertThat(dto.id()).isEqualTo(10L),
                () -> assertThat(dto.nome()).isEqualTo("Reserva de Emergência"),
                () -> assertThat(dto.descricao()).isEqualTo("Guardar dinheiro para imprevistos"),
                () -> assertThat(dto.fotoUrl()).isEqualTo("https://link-da-foto.com/image.png"),
                () -> assertThat(dto.dataLimite()).isEqualTo(LocalDate.of(2026, 12, 31)),
                () -> assertThat(dto.tipoMeta()).isEqualTo(TipoMeta.POUPANCA),
                () -> assertThat(dto.usuarioId()).isEqualTo(7L),
                () -> assertThat(dto.contaId()).isEqualTo(3L),
                () -> assertThat(dto.progresso()).isEqualTo(25.0)
        );
    }

    @Test
    @DisplayName("valorTotal vem de Meta.valor e valorAtual de Meta.valorAtual (nomes diferentes, fácil de trocar)")
    void devePreservarSemanticaDosValores() {
        MetaResponseDTO dto = new MetaResponseDTO(metaCompleta(), 25.0);

        assertAll(
                () -> assertThat(dto.valorTotal()).isEqualByComparingTo("5000.00"),
                () -> assertThat(dto.valorAtual()).isEqualByComparingTo("1250.00")
        );
    }

    @Test
    @DisplayName("usuarioId é null quando a meta não tem usuário")
    void usuarioIdDeveSerNullSemUsuario() {
        Meta meta = metaCompleta();
        meta.setUsuario(null);
        assertThat(new MetaResponseDTO(meta, 0.0).usuarioId()).isNull();
    }

    @Test
    @DisplayName("contaId é null quando a meta não tem conta (conta é opcional)")
    void contaIdDeveSerNullSemConta() {
        Meta meta = metaCompleta();
        meta.setConta(null);
        assertThat(new MetaResponseDTO(meta, 0.0).contaId()).isNull();
    }

    @ParameterizedTest(name = "progresso {0} é repassado sem transformação")
    @ValueSource(doubles = {0.0, 33.33, 100.0})
    void deveRepassarProgressoSemTransformacao(double progresso) {
        assertThat(new MetaResponseDTO(metaCompleta(), progresso).progresso()).isEqualTo(progresso);
    }
}
