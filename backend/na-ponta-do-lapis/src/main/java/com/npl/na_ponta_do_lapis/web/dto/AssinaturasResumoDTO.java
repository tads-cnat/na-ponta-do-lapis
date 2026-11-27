package com.npl.na_ponta_do_lapis.web.dto;

import java.math.BigDecimal;
import java.util.List;

public record AssinaturasResumoDTO(
        BigDecimal totalMensalDespesas,
        BigDecimal totalMensalReceitas,
        BigDecimal totalAnualDespesas,
        long ativas,
        long pausadas,
        List<AssinaturaRecorrenteResponseDTO> assinaturas
) {
}
