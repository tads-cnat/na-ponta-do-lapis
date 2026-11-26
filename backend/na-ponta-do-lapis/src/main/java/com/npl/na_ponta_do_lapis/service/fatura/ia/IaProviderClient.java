package com.npl.na_ponta_do_lapis.service.fatura.ia;

import com.npl.na_ponta_do_lapis.service.fatura.FaturaAnaliseException;

public interface IaProviderClient {

    boolean suporta(ProvedorIa provedor);

    String completar(ProvedorIa provedor, String apiKey, String prompt);

    /**
     * Completa um prompt em texto livre / conversa (sem forçar resposta JSON).
     */
    default String completarTexto(ProvedorIa provedor, String apiKey, String prompt) {
        return completar(provedor, apiKey, prompt);
    }

    /**
     * Completa uma conversa separando nativamente instruções de sistema e mensagem do usuário,
     * reduzindo o risco de prompt injection e limitando o tamanho da saída.
     */
    default String completarChat(ProvedorIa provedor, String apiKey, String systemPrompt, String userMessage) {
        return completarTexto(provedor, apiKey, systemPrompt + "\n\n" + userMessage);
    }

    default String completarComPdf(ProvedorIa provedor, String apiKey, String prompt, byte[] pdf) {
        throw FaturaAnaliseException.provedorSemSuportePdf();
    }
}
