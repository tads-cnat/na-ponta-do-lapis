package com.npl.na_ponta_do_lapis.web.dto;

import com.npl.na_ponta_do_lapis.entity.Meta;
import com.npl.na_ponta_do_lapis.entity.enums.TipoMeta;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record MetaDTO(

        @Schema(example = "Reserva de Emergência" )
        @Size(min = 3, max= 50, message = "Nome deve ter entre 3 e 50 caracteres")
        @NotBlank(message = "Nome é obrigatorio")
        String nome,

        @Schema(example = "Guardar dinheiro para imprevistos")
        String descricao,

        @Schema(example = "5000.00")
        @NotNull(message = "Valor é obrigatorio")
        @Positive(message = "Valor deve ser positivo")
        BigDecimal valor,

        @Schema(example = "https://link-da-foto.com/image.png")
        String fotoUrl,

        @Schema(example = "2026-12-31")
        @Future(message = "Data limite deve ser no futuro")
        LocalDate dataLimite,

        @Schema(example = "POUPANCA")
        @NotNull(message = "Tipo meta é Obrigatorio")
        TipoMeta tipoMeta
) {
    public Meta toEntity() {
        Meta meta = new Meta();
        meta.setNome(this.nome);
        meta.setDescricao(this.descricao);
        meta.setValor(this.valor);
        meta.setFotoUrl(this.fotoUrl);
        meta.setDataLimite(this.dataLimite);
        meta.setTipoMeta(this.tipoMeta);
        meta.setValorAtual(BigDecimal.ZERO); // Inicializa com zero na criação
        return meta;
    }
}