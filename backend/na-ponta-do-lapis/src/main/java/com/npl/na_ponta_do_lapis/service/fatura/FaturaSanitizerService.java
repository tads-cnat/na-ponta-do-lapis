package com.npl.na_ponta_do_lapis.service.fatura;

import org.springframework.stereotype.Service;

/**
 * Remove ou mascara dados pessoais do texto da fatura antes de enviá-lo à IA.
 *
 * O PDF original nunca é sanitizado (não é possível sem descaracterizá-lo); por isso
 * o envio multimodal só acontece com consentimento explícito.
 */
@Service
public class FaturaSanitizerService {

    private static final String CPF = "\\b\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}\\b";
    private static final String CNPJ = "\\b\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}\\b";
    private static final String CARTAO = "(?<!\\d)(?:\\d[ -]?){13,19}(?!\\d)";
    private static final String EMAIL = "(?i)\\b[\\w.+-]+@[\\w-]+(?:\\.[\\w-]+)+\\b";
    private static final String CEP = "\\b\\d{5}-?\\d{3}\\b";
    private static final String TELEFONE = "(?:\\(?\\d{2}\\)?\\s?)?(?:9\\s?)?\\d{4}[-\\s]?\\d{4}\\b";
    private static final String ENDERECO = "(?i)\\b(?:rua|avenida|av\\.?|travessa|alameda|pra[çc]a|rodovia|estrada|logradouro)\\s+[^\\n,;]{3,90}";

    public String sanitizar(String texto) {
        if (texto == null || texto.isBlank()) {
            return "";
        }

        String sanitizado = texto;
        sanitizado = sanitizado.replaceAll(CNPJ, "**.***.***/****-**");
        sanitizado = sanitizado.replaceAll(CPF, "***.***.***-**");
        sanitizado = sanitizado.replaceAll(CARTAO, "**** **** **** ****");
        sanitizado = sanitizado.replaceAll(EMAIL, "***@***.***");
        sanitizado = sanitizado.replaceAll(ENDERECO, "*** endereco ***");
        sanitizado = sanitizado.replaceAll(CEP, "***-**");
        sanitizado = sanitizado.replaceAll(TELEFONE, "*** telefone ***");
        return sanitizado;
    }
}
