package com.npl.na_ponta_do_lapis.service.fatura.ia;

import com.npl.na_ponta_do_lapis.service.fatura.FaturaAnaliseException;

public interface IaProviderClient {

    boolean suporta(ProvedorIa provedor);

    String completar(ProvedorIa provedor, String apiKey, String prompt);

    default String completarComPdf(ProvedorIa provedor, String apiKey, String prompt, byte[] pdf) {
        throw FaturaAnaliseException.provedorSemSuportePdf();
    }
}
