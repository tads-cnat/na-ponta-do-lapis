package com.npl.na_ponta_do_lapis.service.fatura.ia;

import java.util.List;

import org.springframework.stereotype.Service;

import com.npl.na_ponta_do_lapis.service.fatura.FaturaAnaliseException;

/**
 * Escolhe o cliente do provedor e monta o prompt de extração.
 */
@Service
public class FaturaIaService {

    private final List<IaProviderClient> clientes;
    private final FaturaIaParser parser;

    public FaturaIaService(List<IaProviderClient> clientes, FaturaIaParser parser) {
        this.clientes = clientes;
        this.parser = parser;
    }

    public FaturaIaParser.ResultadoIa analisarTexto(ProvedorIa provedor, String apiKey,
                                                    String textoSanitizado, List<String> categorias) {
        IaProviderClient cliente = cliente(provedor);
        String resposta = cliente.completar(provedor, apiKey, promptTexto(textoSanitizado, categorias));
        return parser.parse(resposta, textoSanitizado, categorias);
    }

    public FaturaIaParser.ResultadoIa analisarPdf(ProvedorIa provedor, String apiKey,
                                                  byte[] pdf, List<String> categorias) {
        if (!provedor.isSuportaPdf()) {
            throw FaturaAnaliseException.provedorSemSuportePdf();
        }
        IaProviderClient cliente = cliente(provedor);
        String resposta = cliente.completarComPdf(provedor, apiKey, promptPdf(categorias), pdf);
        return parser.parse(resposta, null, categorias);
    }

    private IaProviderClient cliente(ProvedorIa provedor) {
        if (provedor == null) {
            throw FaturaAnaliseException.precisaIa();
        }
        return clientes.stream()
                .filter(c -> c.suporta(provedor))
                .findFirst()
                .orElseThrow(FaturaAnaliseException::precisaIa);
    }

    private String promptTexto(String textoSanitizado, List<String> categorias) {
        return """
                Você é um extrator de faturas de cartão de crédito brasileiras.
                Analise o texto abaixo e retorne SOMENTE um JSON válido, sem markdown, neste formato:
                {
                  "padrao": {
                    "emissor": "Nome do emissor",
                    "regexIdentificacao": "regex que localiza o emissor no texto",
                    "regexTransacao": "regex com grupos nomeados (?<descricao>...) (?<data>...) (?<valor>...)",
                    "regexTotal": "regex cujo grupo 1 captura o total da fatura",
                    "formatoData": "dd/MM/yyyy"
                  },
                  "valorTotalFatura": 0.00,
                  "transacoes": [
                    {"descricao":"Amazon","valor":50.00,"data":"2026-01-15","categoria":"Outros","numeroParcela":2,"totalParcelas":10}
                  ]
                }

                Regras obrigatórias:
                - Ignore linhas de pagamento de fatura, pagamento recebido, saldo anterior, total, vencimento e limite.
                - Use valores positivos para as compras (despesas).
                - Datas sempre em YYYY-MM-DD.
                - Categorias permitidas: %s. Se não souber, use "Outros".
                - Se a descrição tiver algo como (02/10) ou 02/10, preencha numeroParcela=2 e totalParcelas=10 e remova esse trecho da descrição.
                - O regexTransacao deve funcionar linha a linha e usar os grupos nomeados descricao, data e valor.
                - O regexIdentificacao deve ser um regex simples que encontre o nome do emissor.
                - O regexTotal deve ter exatamente um grupo de captura com o valor total.
                - Não invente transações. Extraia apenas o que estiver no texto.

                Texto da fatura (dados pessoais já mascarados):
                %s
                """.formatted(String.join(", ", categorias), textoSanitizado);
    }

    private String promptPdf(List<String> categorias) {
        return """
                Você é um extrator de faturas de cartão de crédito brasileiras.
                Leia o PDF anexado e retorne SOMENTE um JSON válido, sem markdown, neste formato:
                {
                  "padrao": {
                    "emissor": "Nome do emissor",
                    "regexIdentificacao": "regex que localiza o emissor no texto",
                    "regexTransacao": "regex com grupos nomeados (?<descricao>...) (?<data>...) (?<valor>...)",
                    "regexTotal": "regex cujo grupo 1 captura o total da fatura",
                    "formatoData": "dd/MM/yyyy"
                  },
                  "valorTotalFatura": 0.00,
                  "transacoes": [
                    {"descricao":"Amazon","valor":50.00,"data":"2026-01-15","categoria":"Outros","numeroParcela":2,"totalParcelas":10}
                  ]
                }

                Regras obrigatórias:
                - Ignore linhas de pagamento de fatura, pagamento recebido, saldo anterior, total, vencimento e limite.
                - Use valores positivos para as compras (despesas).
                - Datas sempre em YYYY-MM-DD.
                - Categorias permitidas: %s. Se não souber, use "Outros".
                - Se a descrição tiver algo como (02/10) ou 02/10, preencha numeroParcela=2 e totalParcelas=10 e remova esse trecho da descrição.
                - Não invente transações. Extraia apenas o que estiver no PDF.
                """.formatted(String.join(", ", categorias));
    }
}
