package com.npl.na_ponta_do_lapis.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.npl.na_ponta_do_lapis.entity.PadraoFatura;
import com.npl.na_ponta_do_lapis.repository.PadraoFaturaRepository;

/**
 * Carga idempotente dos padrões iniciais de fatura.
 *
 * Roda independentemente de app.seed.enabled, mas só insere um emissor quando
 * ainda não existe padrão ativo para ele. Assim, um padrão gerado pela IA não é
 * sobrescrito a cada reinício.
 */
@Component
@Order(2)
public class PadraoFaturaSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PadraoFaturaSeeder.class);

    private static final String REGEX_NUBANK_TRANSACAO =
            "(?m)^\\s*[•·]?\\s*(?<data>\\d{1,2}\\s+"
                    + "(?:JAN|FEV|MAR|ABR|MAI|JUN|JUL|AGO|SET|OUT|NOV|DEZ)"
                    + "(?:\\s+\\d{4})?)\\s+(?<descricao>.+?)\\s+"
                    + "(?:R\\$\\s*)?(?<valor>-?\\d{1,3}(?:\\.\\d{3})*,\\d{2})\\s*$";

    private static final String REGEX_DATA_TRANSACAO =
            "(?m)^\\s*[•·]?\\s*(?<data>\\d{2}/\\d{2}(?:/\\d{4})?)\\s+"
                    + "(?<descricao>.+?)\\s+(?:R\\$\\s*)?"
                    + "(?<valor>-?\\d{1,3}(?:\\.\\d{3})*,\\d{2})\\s*$";

    private static final String REGEX_TOTAL =
            "(?i)total\\s*(?:da\\s*fatura|a\\s*pagar|desta\\s*fatura)?\\s*[:]?\\s*"
                    + "(?:R\\$\\s*)?(\\d{1,3}(?:\\.\\d{3})*,\\d{2})";

    private final PadraoFaturaRepository padraoFaturaRepository;

    public PadraoFaturaSeeder(PadraoFaturaRepository padraoFaturaRepository) {
        this.padraoFaturaRepository = padraoFaturaRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        criarSeAusente("Nubank",
                "(?i)nubank",
                REGEX_NUBANK_TRANSACAO,
                REGEX_TOTAL,
                "dd MMM yyyy");

        criarSeAusente("Inter",
                "(?i)banco\\s+inter|\\binter\\b",
                REGEX_DATA_TRANSACAO,
                REGEX_TOTAL,
                "dd/MM/yyyy");

        criarSeAusente("Itaú",
                "(?i)ita[úu]|banco\\s+ita[úu]",
                REGEX_DATA_TRANSACAO,
                REGEX_TOTAL,
                "dd/MM/yyyy");
    }

    private void criarSeAusente(String emissor, String regexIdentificacao,
                                String regexTransacao, String regexTotal, String formatoData) {
        if (padraoFaturaRepository.existsByEmissorAndAtivoTrue(emissor)) {
            return;
        }

        PadraoFatura padrao = new PadraoFatura();
        padrao.setEmissor(emissor);
        padrao.setRegexIdentificacao(regexIdentificacao);
        padrao.setRegexTransacao(regexTransacao);
        padrao.setRegexTotal(regexTotal);
        padrao.setFormatoData(formatoData);
        padrao.setVersao(1);
        padrao.setOrigem("SEED");
        padrao.setAtivo(true);
        padraoFaturaRepository.save(padrao);
        log.info("[PadraoFaturaSeeder] Padrão inicial criado para {}.", emissor);
    }
}
