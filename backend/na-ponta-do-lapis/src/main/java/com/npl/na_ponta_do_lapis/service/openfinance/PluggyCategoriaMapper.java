package com.npl.na_ponta_do_lapis.service.openfinance;

import com.npl.na_ponta_do_lapis.entity.TipoCategoria;
import com.npl.na_ponta_do_lapis.repository.TipoCategoriaRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PluggyCategoriaMapper {

    private final TipoCategoriaRepository categoriaRepository;
    private final Map<String, TipoCategoria> cacheCategorias = new HashMap<>();

    public PluggyCategoriaMapper(TipoCategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public TipoCategoria mapearCategoria(String categoriaPluggy, String descricao) {
        String textoAnalise = (categoriaPluggy != null ? categoriaPluggy + " " : "") + (descricao != null ? descricao : "");
        String textoLower = textoAnalise.toLowerCase();

        String nomeCategoriaDestino = classificar(textoLower);
        return obterOuBuscarCategoria(nomeCategoriaDestino);
    }

    private String classificar(String texto) {
        if (texto.contains("food") || texto.contains("restaurant") || texto.contains("supermarket")
                || texto.contains("mercado") || texto.contains("lanche") || texto.contains("refeicao")
                || texto.contains("padaria") || texto.contains("alimenta") || texto.contains("ifood")
                || texto.contains("acougue") || texto.contains("feira")) {
            return "Alimentação";
        }

        if (texto.contains("transport") || texto.contains("gas station") || texto.contains("fuel")
                || texto.contains("combustivel") || texto.contains("posto") || texto.contains("uber")
                || texto.contains("99app") || texto.contains("onibus") || texto.contains("metro")
                || texto.contains("pedagio") || texto.contains("estacionamento")) {
            return "Transporte";
        }

        if (texto.contains("health") || texto.contains("pharmacy") || texto.contains("farmacia")
                || texto.contains("hospital") || texto.contains("medic") || texto.contains("clinica")
                || texto.contains("drogaria") || texto.contains("saude") || texto.contains("dentista")
                || texto.contains("consulta")) {
            return "Saúde";
        }

        if (texto.contains("education") || texto.contains("school") || texto.contains("university")
                || texto.contains("faculdade") || texto.contains("curso") || texto.contains("livro")
                || texto.contains("educacao") || texto.contains("escola") || texto.contains("udemy")
                || texto.contains("alura")) {
            return "Educação";
        }

        if (texto.contains("entertainment") || texto.contains("recreation") || texto.contains("cinema")
                || texto.contains("netflix") || texto.contains("spotify") || texto.contains("jogos")
                || texto.contains("lazer") || texto.contains("steam") || texto.contains("show")
                || texto.contains("teatro") || texto.contains("viagem") || texto.contains("hotel")) {
            return "Lazer";
        }

        return "Outros";
    }

    private TipoCategoria obterOuBuscarCategoria(String nome) {
        if (cacheCategorias.containsKey(nome)) {
            return cacheCategorias.get(nome);
        }

        TipoCategoria cat = categoriaRepository.findByNome(nome)
                .orElseGet(() -> categoriaRepository.findByNome("Outros")
                        .orElseThrow(() -> new IllegalStateException("Categoria 'Outros' deve estar presente no banco")));

        cacheCategorias.put(nome, cat);
        return cat;
    }
}
