package com.npl.na_ponta_do_lapis.service.fatura.ia;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.npl.na_ponta_do_lapis.entity.PadraoFatura;
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

    public PadraoFatura refinarPadrao(ProvedorIa provedor, String apiKey,
                                      PadraoFatura padraoInicial,
                                      String textoSanitizado,
                                      BigDecimal totalFatura,
                                      List<TransacaoFaturaDTO> transacoesCapturadas) {
        if (padraoInicial == null || provedor == null || apiKey == null || apiKey.isBlank()) {
            return null;
        }
        BigDecimal somaAtual = FaturaRegexService.somar(transacoesCapturadas);
        BigDecimal diferenca = totalFatura != null ? totalFatura.subtract(somaAtual) : BigDecimal.ZERO;

        String prompt = promptRefinamento(padraoInicial, textoSanitizado, totalFatura, somaAtual, diferenca);
        IaProviderClient cliente = cliente(provedor);
        String resposta = cliente.completar(provedor, apiKey, prompt);
        return parser.parsePadraoRefinado(resposta, textoSanitizado);
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
                Você é um extrator especialista de faturas de cartão de crédito brasileiras (formatos texto e tabelas Markdown).
                Leia o texto abaixo e devolva SOMENTE um JSON válido, sem markdown (sem ```json), sem comentários e sem texto adicional:
                {
                  "valorTotalFatura": 0.00,
                  "transacoes": [
                    {"descricao":"Amazon","valor":50.00,"data":"2026-01-15","categoria":"Outros","tipo":"DESPESA","numeroParcela":2,"totalParcelas":10}
                  ],
                  "padrao": {
                    "emissor": "Nome do emissor",
                    "regexIdentificacao": "regex que localiza o emissor no texto",
                    "regexTransacao": "regex Java com grupos nomeados (?<descricao>...) (?<data>...) (?<valor>...)",
                    "regexTotal": "regex cujo grupo 1 captura o total a pagar",
                    "formatoData": "dd MMM"
                  }
                }

                %s

                Regras obrigatórias de extração e conciliação:
                1. "valorTotalFatura" deve ser rigorosamente o Total a Pagar desta fatura.
                   Nunca use "saldo em aberto total", "saldo da próxima fatura" nem valores de simulação.
                2. Extraia todas as compras e despesas reais de todos os titulares/cartões como "tipo":"DESPESA".
                   Estornos, devoluções, reembolsos, créditos e pagamentos efetuados são "tipo":"RECEITA".
                   Atenção: compras ou tarifas com nomes comerciais como "MERCADOLIVRE*REEMBOLSO", "Pix no crédito"
                   ou serviços cobrados na fatura são "tipo":"DESPESA". Apenas marque como "tipo":"RECEITA" se for realmente
                   um estorno, crédito ou pagamento que abate o saldo devedor da fatura (com sinal negativo ou indicador explícito).
                3. Use sempre valores numéricos positivos no campo "valor". O campo "tipo" é que define o sinal.
                4. Datas sempre no padrão YYYY-MM-DD. Se a linha contiver só dia e mês (ex: "26 SET"), utilize o ano
                   da data de emissão ou vencimento da fatura.
                5. Na descrição, remova máscara de cartão (ex: "•••• 4908"), "R$", ruídos de Markdown (|) e o trecho
                   "Parcela X/Y". O parcelamento deve ir nos campos numeroParcela e totalParcelas.
                6. NÃO inclua:
                   - simulações de parcelamento ou crédito rotativo ("Parcelar em 3 meses", juros totais, CET, etc.);
                   - limites de crédito, limites de saque/pix/boleto e valor máximo para transações;
                   - linhas técnicas internas que se anulam (ex: "Saldo em atraso" com "Crédito de atraso",
                     "Encerramento de dívida" com "Juros de dívida encerrada").
                7. CONCILIAÇÃO OBRIGATÓRIA COM O RESUMO DA FATURA:
                   A fórmula contábil do total da fatura é:
                   Total a Pagar = (Soma de todas as DESPESAS) - (Soma de todas as RECEITAS).
                   Para que essa conta feche perfeitamente com "valorTotalFatura":
                   - Se houver "Fatura anterior" (saldo não quitado do mês anterior), inclua como transação "tipo":"DESPESA".
                   - Se houver "Pagamento recebido" no resumo, inclua como transação consolidada "tipo":"RECEITA".
                     Neste caso, NÃO inclua os pagamentos detalhados individuais da seção "Pagamentos e Financiamentos" para não duplicar.
                   - Se houver "Outros lançamentos": inclua como "tipo":"RECEITA" se tiver sinal negativo (−R$), ou "tipo":"DESPESA" se positivo.
                   - Se houver "IOF de compras internacionais" no resumo e ele NÃO estiver listado como transação nas compras, inclua como "tipo":"DESPESA".
                8. AUTO-VERIFICAÇÃO OBRIGATÓRIA ANTES DE RESPONDER:
                   Calcule: SaldoCalculado = (Soma das DESPESAS) - (Soma das RECEITAS).
                   Confira se SaldoCalculado == valorTotalFatura.
                   - Se SaldoCalculado for diferente de valorTotalFatura, revise a inclusão dos itens do resumo (Fatura anterior, Pagamento recebido, Outros lançamentos).
                   - Se ainda sobrar uma diferença mínima de centavos (por arredondamento bancário de IOF/taxas), adicione uma transação:
                     {"descricao":"Ajuste de conciliação da fatura", "valor": <diferenca_absoluta>, "data":"<data_fatura>", "tipo": "<DESPESA_se_faltou_ou_RECEITA_se_sobrou>", "categoria":"Outros"}
                9. O campo "padrao" é OPCIONAL. Use null se não tiver certeza de que o regex funciona.
                   É melhor deixar null do que fornecer regex incorreto.

                Regras do "padrao", quando você incluir:
                - "formatoData" deve ser o formato REAL das datas presentes no texto:
                  - Se no documento estiver "26 SET", use "dd MMM".
                  - Se estiver "14 de mar. 2026", use "dd 'de' MMM. yyyy".
                  - Se estiver "14 de março de 2026", use "dd 'de' MMMM 'de' yyyy".
                  - Se estiver "14/03/2026", use "dd/MM/yyyy".
                - "regexTransacao" deve usar (?m), conter obrigatoriamente os grupos nomeados (?<data>...), (?<descricao>...) e (?<valor>...),
                  e casar com as compras e transações reais do cartão.
                  NÃO case linhas informativas de pagamento de faturas anteriores (ex: "PAGAMENTO ON LINE - + R$ ...").
                - O grupo "valor" deve aceitar sinal opcional e capturar o número completo com vírgula (ex: [\\d.,]+).
                - "regexTotal" deve casar RIGOROSAMENTE com o Total a Pagar / Total da fatura / Total da sua fatura / Fatura atual da fatura vigente, com exatamente 1 grupo de captura.
                  NUNCA aponte para juros, encargos, multas, créditos rotativos ou próxima fatura.
                - "regexIdentificacao" deve ser um regex simples para identificar o emissor (ex: "(?i)\\binter\\b" ou "(?i)nubank").
                - O regex deve ser genérico e reutilizável: nunca inclua nomes de lojas ou valores específicos desta fatura.

                Categorias permitidas: %s. Se não souber, use "Outros".

                Texto da fatura (dados pessoais já mascarados):
                %s
                """.formatted(contextoParaPrompt(contexto), String.join(", ", categorias), textoSanitizado);
    }

    private String promptPdf(List<String> categorias) {
        return """
                Você é um extrator especialista de faturas de cartão de crédito brasileiras.
                Leia o PDF anexado e devolva SOMENTE um JSON válido, sem markdown (sem ```json), sem comentários e sem texto adicional:
                {
                  "valorTotalFatura": 0.00,
                  "transacoes": [
                    {"descricao":"Amazon","valor":50.00,"data":"2026-01-15","categoria":"Outros","tipo":"DESPESA","numeroParcela":2,"totalParcelas":10}
                  ],
                  "padrao": {
                    "emissor": "Nome do emissor",
                    "regexIdentificacao": "regex que localiza o emissor no texto",
                    "regexTransacao": "regex Java com grupos nomeados (?<descricao>...) (?<data>...) (?<valor>...)",
                    "regexTotal": "regex cujo grupo 1 captura o total a pagar",
                    "formatoData": "dd MMM"
                  }
                }

                Regras obrigatórias de extração e conciliação:
                1. "valorTotalFatura" deve ser rigorosamente o Total a Pagar desta fatura.
                   Nunca use "saldo em aberto total", "saldo da próxima fatura" nem valores de simulação.
                2. Extraia todas as compras e despesas reais de todos os titulares/cartões como "tipo":"DESPESA".
                   Estornos, devoluções, reembolsos, créditos e pagamentos efetuados são "tipo":"RECEITA".
                   Atenção: compras ou tarifas com nomes comerciais como "MERCADOLIVRE*REEMBOLSO", "Pix no crédito"
                   ou serviços cobrados na fatura são "tipo":"DESPESA". Apenas marque como "tipo":"RECEITA" se for realmente
                   um estorno, crédito ou pagamento que abate o saldo devedor da fatura (com sinal negativo ou indicador explícito).
                3. Use sempre valores numéricos positivos no campo "valor". O campo "tipo" é que define o sinal.
                4. Datas sempre no padrão YYYY-MM-DD. Se a linha contiver só dia e mês (ex: "26 SET"), utilize o ano
                   da data de emissão ou vencimento da fatura.
                5. Na descrição, remova máscara de cartão, "R$" e o trecho "Parcela X/Y".
                   O parcelamento deve ir nos campos numeroParcela e totalParcelas.
                6. NÃO inclua:
                   - simulações de parcelamento ou crédito rotativo ("Parcelar em 3 meses", juros totais, CET, etc.);
                   - limites de crédito, limites de saque/pix/boleto e valor máximo para transações;
                   - linhas técnicas internas que se anulam (ex: "Saldo em atraso" com "Crédito de atraso",
                     "Encerramento de dívida" com "Juros de dívida encerrada").
                7. CONCILIAÇÃO OBRIGATÓRIA COM O RESUMO DA FATURA:
                   A fórmula contábil do total da fatura é:
                   Total a Pagar = (Soma de todas as DESPESAS) - (Soma de todas as RECEITAS).
                   Para que essa conta feche perfeitamente com "valorTotalFatura":
                   - Se houver "Fatura anterior" (saldo não quitado do mês anterior), inclua como transação "tipo":"DESPESA".
                   - Se houver "Pagamento recebido" no resumo, inclua como transação consolidada "tipo":"RECEITA".
                     Neste caso, NÃO inclua os pagamentos detalhados individuais da seção "Pagamentos e Financiamentos" para não duplicar.
                   - Se houver "Outros lançamentos": inclua como "tipo":"RECEITA" se tiver sinal negativo (−R$), ou "tipo":"DESPESA" se positivo.
                   - Se houver "IOF de compras internacionais" no resumo e ele NÃO estiver listado como transação nas compras, inclua como "tipo":"DESPESA".
                8. AUTO-VERIFICAÇÃO OBRIGATÓRIA ANTES DE RESPONDER:
                   Calcule: SaldoCalculado = (Soma das DESPESAS) - (Soma das RECEITAS).
                   Confira se SaldoCalculado == valorTotalFatura.
                   - Se SaldoCalculado for diferente de valorTotalFatura, revise a inclusão dos itens do resumo (Fatura anterior, Pagamento recebido, Outros lançamentos).
                   - Se ainda sobrar uma diferença mínima de centavos (por arredondamento bancário de IOF/taxas), adicione uma transação:
                     {"descricao":"Ajuste de conciliação da fatura", "valor": <diferenca_absoluta>, "data":"<data_fatura>", "tipo": "<DESPESA_se_faltou_ou_RECEITA_se_sobrou>", "categoria":"Outros"}
                9. O campo "padrao" é OPCIONAL. Use null se não tiver certeza de que o regex funciona.
                   É melhor deixar null do que fornecer regex incorreto.

                Regras do "padrao", quando você incluir:
                - "formatoData" deve ser o formato REAL das datas presentes no documento:
                  - Se no documento estiver "26 SET", use "dd MMM".
                  - Se estiver "14 de mar. 2026", use "dd 'de' MMM. yyyy".
                  - Se estiver "14 de março de 2026", use "dd 'de' MMMM 'de' yyyy".
                  - Se estiver "14/03/2026", use "dd/MM/yyyy".
                - "regexTransacao" deve usar (?m), conter obrigatoriamente os grupos nomeados (?<data>...), (?<descricao>...) e (?<valor>...),
                  e casar com as compras e transações reais do cartão.
                  NÃO case linhas informativas de pagamento de faturas anteriores (ex: "PAGAMENTO ON LINE - + R$ ...").
                - O grupo "valor" deve aceitar sinal opcional e capturar o número completo com vírgula (ex: [\\d.,]+).
                - "regexTotal" deve casar RIGOROSAMENTE com o Total a Pagar / Total da fatura / Total da sua fatura / Fatura atual da fatura vigente, com exatamente 1 grupo de captura.
                  NUNCA aponte para juros, encargos, multas, créditos rotativos ou próxima fatura.
                - "regexIdentificacao" deve ser um regex simples para identificar o emissor (ex: "(?i)\\binter\\b" ou "(?i)nubank").
                - O regex deve ser genérico e reutilizável: nunca inclua nomes de lojas ou valores específicos desta fatura.

                Categorias permitidas: %s. Se não souber, use "Outros".
                """.formatted(String.join(", ", categorias));
    }

    private String promptRefinamento(PadraoFatura padrao, String textoSanitizado,
                                     BigDecimal totalEsperado, BigDecimal somaAtual, BigDecimal diferenca) {
        return """
                Você é um especialista em expressões regulares Java para extração de faturas de cartão de crédito.
                Foi gerado um padrão regex preliminar que não capturou 100%% das despesas ou teve pequena divergência de soma.
                
                Padrão atual:
                - Emissor: "%s"
                - regexIdentificacao: "%s"
                - regexTransacao: "%s"
                - regexTotal: "%s"
                - formatoData: "%s"
                
                Diagnóstico contábil:
                - Total esperado da fatura: R$ %s
                - Soma capturada com o regex atual: R$ %s
                - Diferença pendente: R$ %s
                
                Instruções de refinamento:
                1. Ajuste "regexTransacao" e/ou "regexTotal" para que o regex capture todas as transações vigentes e feche exatamente com o Total Esperado.
                2. Certifique-se de que "regexTransacao" use (?m), contenha os grupos nomeados (?<data>...), (?<descricao>...) e (?<valor>...), e aceite variações de hífens (ex: (?:-\\s+)?), símbolos de R$ e sinais.
                3. NÃO capture comprovantes de pagamentos anteriores informativos (como "PAGAMENTO ON LINE - + R$ ...") nem parcelas de próximas faturas.
                4. "regexTotal" deve capturar rigorosamente o total a pagar da fatura (grupo 1), sem casar com limites, juros rotativos ou encargos.
                
                Texto da fatura (dados sensíveis mascarados):
                %s
                
                Responda SOMENTE um JSON válido com o padrão corrigido (sem markdown, sem comentários):
                {
                  "padrao": {
                    "emissor": "%s",
                    "regexIdentificacao": "%s",
                    "regexTransacao": "regex Java ajustado com (?<data>...) (?<descricao>...) (?<valor>...)",
                    "regexTotal": "regex Java do total com 1 grupo de captura",
                    "formatoData": "%s"
                  }
                }
                """.formatted(
                padrao.getEmissor(),
                padrao.getRegexIdentificacao(),
                padrao.getRegexTransacao(),
                padrao.getRegexTotal() != null ? padrao.getRegexTotal() : "",
                padrao.getFormatoData() != null ? padrao.getFormatoData() : "dd/MM/yyyy",
                totalEsperado != null ? totalEsperado.toPlainString() : "0.00",
                somaAtual != null ? somaAtual.toPlainString() : "0.00",
                diferenca != null ? diferenca.toPlainString() : "0.00",
                textoSanitizado,
                padrao.getEmissor(),
                padrao.getRegexIdentificacao(),
                padrao.getFormatoData() != null ? padrao.getFormatoData() : "dd/MM/yyyy"
        );
    }

    private String contextoParaPrompt(ContextoFaturaIa contexto) {
        if (contexto == null || !contexto.temAlgumValor()) {
            return "O sistema não detectou um bloco de resumo nesta fatura.";
        }
        StringBuilder texto = new StringBuilder("Valores do resumo detectados pelo sistema nesta fatura:\n");
        adicionarValor(texto, "Total a pagar", contexto.totalFatura());
        adicionarValor(texto, "Fatura anterior", contexto.faturaAnterior());
        adicionarValor(texto, "Pagamento recebido", contexto.pagamentoRecebido());
        adicionarValor(texto, "Total de compras", contexto.totalCompras());
        adicionarValor(texto, "IOF de compras internacionais", contexto.iofInternacional());
        adicionarValor(texto, "Outros lançamentos", contexto.outrosLancamentos());
        texto.append("\nInstrução contábil: certifique-se de que a soma das despesas menos receitas coincida ")
             .append("com o 'Total a pagar' acima. Utilize 'Fatura anterior' (DESPESA), 'Pagamento recebido' (RECEITA) ")
             .append("e 'Outros lançamentos' conforme necessário para a conciliação.");
        return texto.toString().trim();
    }

    private void adicionarValor(StringBuilder texto, String rotulo, BigDecimal valor) {
        if (valor != null) {
            texto.append("- ").append(rotulo).append(": R$ ").append(valor.toPlainString()).append('\n');
        }
    }

    /**
     * Contexto já calculado localmente e enviado à IA para ela conferir a soma.
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
