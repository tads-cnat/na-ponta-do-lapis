package com.npl.na_ponta_do_lapis.web.dto;

import com.npl.na_ponta_do_lapis.entity.Meta;
import com.npl.na_ponta_do_lapis.entity.TipoMeta;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

public record MetaDTO(
        @Schema(example = "Reserva de Emergência")
        String nome,

        @Schema(example = "Guardar dinheiro para imprevistos")
        String descricao,

        @Schema(example = "5000.00")
        BigDecimal valor,

        @Schema(example = "https://link-da-foto.com/image.png")
        String fotoUrl,

        @Schema(example = "2026-12-31")
        LocalDate dataLimite,

        @Schema(example = "1")
        Long tipoMetaId
) {
    public Meta toEntity(TipoMeta tipoMeta) {
        Meta meta = new Meta();
        meta.setNome(this.nome);
        meta.setDescricao(this.descricao);
        meta.setValor(this.valor);
        meta.setFotoUrl(this.fotoUrl);
        meta.setDataLimite(this.dataLimite);
        meta.setTipoMeta(tipoMeta);
        return meta;
    }
}