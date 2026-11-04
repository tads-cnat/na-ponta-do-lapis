package com.npl.na_ponta_do_lapis.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "DTO para exclusão em lote de transações")
public record TransacoesExclusaoLoteDTO(
        @Schema(description = "Lista de identificadores das transações a serem excluídas", example = "[1, 2, 3]")
        @NotEmpty(message = "A lista de IDs não pode ser vazia.")
        List<Long> ids
) {
}

