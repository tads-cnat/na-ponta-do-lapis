package com.npl.na_ponta_do_lapis.service.fatura.ia;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.npl.na_ponta_do_lapis.service.fatura.FaturaAnaliseException;
import com.npl.na_ponta_do_lapis.service.fatura.FaturaRegexService;
import com.npl.na_ponta_do_lapis.web.dto.TransacaoFaturaDTO;

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
        return analisarTexto(provedor, apiKey, textoSanitizado, categorias, ContextoFaturaIa.vazio());
    }

    public FaturaIaParser.ResultadoIa analisarTexto(ProvedorIa provedor, String apiKey,
                                                    String textoSanitizado, List<String> categorias,
                                                    ContextoFaturaIa contexto) {
        IaProviderClient cliente = cliente(provedor);
        String resposta = cliente.completar(provedor, apiKey, promptTexto(textoSanitizado, categorias, contexto));
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

    private String promptTexto(String textoSanitizado, List<String> categorias,
                               ContextoFaturaIa contexto) {
        return """
                Você é um extrator especialista de faturas de cartão de crédito brasileiras.
                Leia o texto abaixo e devolva SOMENTE um JSON válido, sem markdown (sem ```json), sem comentários e sem texto adicional:
                {
                  "emissor": "Nome do Banco ou Cartão",
                  "valorTotalFatura": 0.00,
                  "transacoes": [
                    {"descricao":"Amazon","valor":50.00,"data":"2026-01-15","categoria":"Outros","tipo":"DESPESA","numeroParcela":2,"totalParcelas":10}
                  ]
                }

                Regras fundamentais de extração:
                1. "emissor": informe o nome do banco ou emissor da fatura (ex: "Santander", "Nubank", "Inter", "Itaú", "Bradesco", "C6 Bank", etc.).
                2. "valorTotalFatura": informe o valor total a pagar da fatura ou o total de despesas.
                3. Extraia TODAS as compras, despesas, tarifas e estornos reais feitos com o cartão.
                   - Se houver múltiplos cartões ou titulares na fatura (ex: titular e adicionais), extraia as transações de TODOS eles.
                   - Compras, despesas, anuidade cobrada e tarifas são "tipo":"DESPESA".
                   - Estornos, reembolsos e devoluções reais são "tipo":"RECEITA".
                4. O que você NUNCA deve incluir como transação:
                   - Subtotais de cartões ou seções (ex: "VALOR TOTAL", "TOTAL DO CARTÃO", "SUBTOTAL"). Capture apenas as compras individuais.
                   - Pagamentos da fatura anterior (ex: "PAGAMENTO DE FATURA", "PAGAMENTO DE FATURA-INTERNET", "PAGAMENTO EM CONTA", "PAGAMENTO ON LINE"). O usuário deseja importar as despesas realizadas, não o pagamento da dívida passada.
                   - Simulações de financiamento ou parcelamento da fatura ("Opções de Parcelamento", "Crédito Rotativo", CET, juros simulados).
                   - Informações de limites (limite de crédito, saque, limite disponível).
                   - NUNCA invente transações fictícias ou sintéticas (como "Fatura anterior", "Pagamento recebido" ou "Ajuste de conciliação").
                5. Use sempre valores numéricos positivos no campo "valor". O campo "tipo" é que define o sinal.
                6. Datas no formato YYYY-MM-DD. Se a linha contiver só dia e mês (ex: "01/10" ou "26 SET"), utilize o ano
                   da emissão ou vencimento da fatura.
                7. Na descrição, limpe o nome comercial (remova máscara de cartão como "•••• 1234", "R$", e trechos como "Parcela X/Y").
                8. Compras parceladas: quando houver identificação de parcela (ex: "02/03", "10/12", "Parcela 2 de 10"),
                   preencha os campos inteiros "numeroParcela" e "totalParcelas".

                Categorias permitidas: %s. Se não souber, use "Outros".

                Texto da fatura (dados pessoais já mascarados):
                %s
                """.formatted(String.join(", ", categorias), textoSanitizado);
    }

    private String promptPdf(List<String> categorias) {
        return """
                Você é um extrator especialista de faturas de cartão de crédito brasileiras.
                Leia o PDF anexado e devolva SOMENTE um JSON válido, sem markdown (sem ```json), sem comentários e sem texto adicional:
                {
                  "emissor": "Nome do Banco ou Cartão",
                  "valorTotalFatura": 0.00,
                  "transacoes": [
                    {"descricao":"Amazon","valor":50.00,"data":"2026-01-15","categoria":"Outros","tipo":"DESPESA","numeroParcela":2,"totalParcelas":10}
                  ]
                }

                Regras fundamentais de extração:
                1. "emissor": informe o nome do banco ou emissor da fatura (ex: "Santander", "Nubank", "Inter", "Itaú", "Bradesco", "C6 Bank", etc.).
                2. "valorTotalFatura": informe o valor total a pagar da fatura ou o total de despesas.
                3. Extraia TODAS as compras, despesas, tarifas e estornos reais feitos com o cartão.
                   - Se houver múltiplos cartões ou titulares na fatura (ex: titular e adicionais), extraia as transações de TODOS eles.
                   - Compras, despesas, anuidade cobrada e tarifas são "tipo":"DESPESA".
                   - Estornos, reembolsos e devoluções reais são "tipo":"RECEITA".
                4. O que você NUNCA deve incluir como transação:
                   - Subtotais de cartões ou seções (ex: "VALOR TOTAL", "TOTAL DO CARTÃO", "SUBTOTAL"). Capture apenas as compras individuais.
                   - Pagamentos da fatura anterior (ex: "PAGAMENTO DE FATURA", "PAGAMENTO DE FATURA-INTERNET", "PAGAMENTO EM CONTA", "PAGAMENTO ON LINE"). O usuário deseja importar as despesas realizadas, não o pagamento da dívida passada.
                   - Simulações de financiamento ou parcelamento da fatura ("Opções de Parcelamento", "Crédito Rotativo", CET, juros simulados).
                   - Informações de limites (limite de crédito, saque, limite disponível).
                   - NUNCA invente transações fictícias ou sintéticas (como "Fatura anterior", "Pagamento recebido" ou "Ajuste de conciliação").
                5. Use sempre valores numéricos positivos no campo "valor". O campo "tipo" é que define o sinal.
                6. Datas no formato YYYY-MM-DD. Se a linha contiver só dia e mês (ex: "01/10" ou "26 SET"), utilize o ano
                   da emissão ou vencimento da fatura.
                7. Na descrição, limpe o nome comercial (remova máscara de cartão como "•••• 1234", "R$", e trechos como "Parcela X/Y").
                8. Compras parceladas: quando houver identificação de parcela (ex: "02/03", "10/12", "Parcela 2 de 10"),
                   preencha os campos inteiros "numeroParcela" e "totalParcelas".

                Categorias permitidas: %s. Se não souber, use "Outros".
                """.formatted(String.join(", ", categorias));
    }

    /**
     * Contexto opcional da fatura enviado à IA.
     */
    public record ContextoFaturaIa(BigDecimal totalFatura,
                                   BigDecimal faturaAnterior,
                                   BigDecimal pagamentoRecebido,
                                   BigDecimal totalCompras,
                                   BigDecimal iofInternacional,
                                   BigDecimal outrosLancamentos) {

        public static ContextoFaturaIa vazio() {
            return new ContextoFaturaIa(null, null, null, null, null, null);
        }

        public boolean temAlgumValor() {
            return totalFatura != null
                    || faturaAnterior != null
                    || pagamentoRecebido != null
                    || totalCompras != null
                    || iofInternacional != null
                    || outrosLancamentos != null;
        }
    }
}
