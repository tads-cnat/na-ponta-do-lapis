package com.npl.na_ponta_do_lapis.web.dto;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado da análise de uma fatura de cartão de crédito")
public record FaturaAnaliseResponseDTO(
    List<TransacaoFaturaDTO> transacoes,
    @Schema(example = "REGEX", description = "REGEX ou IA")
    String fonte,
    @Schema(example = "Nubank")
    String emissor,
    BigDecimal somaCalculada,
    BigDecimal valorTotalFatura,
    BigDecimal diferenca,
    boolean alertaSoma,
    Integer padraoVersao,
    boolean pdfEscaneado,
    boolean ocrUtilizado,
    boolean consentimentoPdfIa,
    @Schema(description = "Quando verdadeiro, o frontend deve pedir autorização para enviar o PDF à IA")
    boolean precisaConsentimentoPdf,
    String mensagem
) {
}
