package com.npl.na_ponta_do_lapis.config;

import com.npl.na_ponta_do_lapis.entity.TipoCategoria;
import com.npl.na_ponta_do_lapis.repository.TipoCategoriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Order(0)
public class CategoriaInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CategoriaInitializer.class);

    private static final List<String> CATEGORIAS_PADRAO = List.of(
            "Alimentação",
            "Transporte",
            "Saúde",
            "Educação",
            "Lazer",
            "Outros"
    );

    private final TipoCategoriaRepository categoriaRepository;

    public CategoriaInitializer(TipoCategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (String nome : CATEGORIAS_PADRAO) {
            if (categoriaRepository.findByNome(nome).isEmpty()) {
                TipoCategoria cat = new TipoCategoria();
                cat.setNome(nome);
                categoriaRepository.save(cat);
                log.info("[CategoriaInitializer] Categoria base inicializada: {}", nome);
            }
        }
    }
}

