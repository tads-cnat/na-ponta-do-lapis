package com.npl.na_ponta_do_lapis.service.fatura.ia;

/**
 * Provedores de IA suportados pela análise de faturas.
 *
 * Apenas Gemini e Claude aceitam PDF/imagem diretamente no fluxo multimodal.
 */
public enum ProvedorIa {
    GEMINI("Gemini", true),
    CLAUDE("Claude", true),
    OPENAI("OpenAI", false),
    DEEPSEEK("DeepSeek", false),
    GROQ("Groq", false);

    private final String nomeExibicao;
    private final boolean suportaPdf;

    ProvedorIa(String nomeExibicao, boolean suportaPdf) {
        this.nomeExibicao = nomeExibicao;
        this.suportaPdf = suportaPdf;
    }

    public String getNomeExibicao() {
        return nomeExibicao;
    }

    public boolean isSuportaPdf() {
        return suportaPdf;
    }
}
