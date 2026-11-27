package com.npl.na_ponta_do_lapis.config;

import com.npl.na_ponta_do_lapis.entity.Conquista;
import com.npl.na_ponta_do_lapis.entity.enums.CategoriaConquista;
import com.npl.na_ponta_do_lapis.repository.ConquistaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Garante de forma idempotente que todas as conquistas do catálogo atual estejam
 * cadastradas no banco em qualquer ambiente, preservando edições feitas no Painel Admin.
 */
@Component
@Profile("!test")
@Order(0)
public class ConquistaSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ConquistaSeeder.class);

    private final ConquistaRepository conquistaRepository;

    public ConquistaSeeder(ConquistaRepository conquistaRepository) {
        this.conquistaRepository = conquistaRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        List<Conquista> catalogoAtual = List.of(
                new Conquista("PRIMEIRA_META", "Primeiro Passo", "Cadastrou a sua primeira meta financeira no sistema.", "pi-flag", 50, CategoriaConquista.METAS),
                new Conquista("PRIMEIRO_APORTE", "Cofrinho Aberto", "Fez o seu primeiro aporte financeiro em uma meta.", "pi-wallet", 50, CategoriaConquista.ECONOMIA),
                new Conquista("POUPADOR_BRONZE", "Poupador Bronze", "Realizou 5 aportes e manteve o ritmo das suas metas.", "pi-star", 100, CategoriaConquista.ECONOMIA),
                new Conquista("META_BATIDA_1", "Alvo Conquistado", "Concluiu com sucesso a sua primeira meta financeira!", "pi-check-circle", 150, CategoriaConquista.METAS),
                new Conquista("MESTRE_DAS_METAS", "Mestre dos Objetivos", "Concluiu 3 ou mais metas financeiras com êxito.", "pi-crown", 300, CategoriaConquista.METAS),
                new Conquista("ORCAMENTO_BLINDADO", "Orçamento Blindado", "Criou um teto de gastos para controlar despesas.", "pi-shield", 75, CategoriaConquista.DISCIPLINA),
                new Conquista("LIVRE_DE_DIVIDAS", "Adeus Dívida", "Criou uma meta para quitar dívidas e pendências financeiras.", "pi-bolt", 75, CategoriaConquista.DISCIPLINA),
                new Conquista("FAMILIA_UNIDA", "Finanças em Família", "Criou ou compartilhou uma meta com seu grupo familiar.", "pi-users", 100, CategoriaConquista.FAMILIA),
                new Conquista("ASSINATURA_CONTROLADA", "Radar de Assinaturas", "Cadastrou sua primeira compra recorrente ou assinatura para acompanhar gastos fixos.", "pi-sync", 60, CategoriaConquista.DISCIPLINA),
                new Conquista("GRAFITH_CONSULTOR", "Papo com o Grafith", "Consultou o Grafith IA pela primeira vez para analisar suas finanças.", "pi-sparkles", 50, CategoriaConquista.DISCIPLINA)
        );

        List<Conquista> faltantes = catalogoAtual.stream()
                .filter(c -> !conquistaRepository.existsById(c.getId()))
                .toList();

        if (!faltantes.isEmpty()) {
            conquistaRepository.saveAll(faltantes);
            log.info("[ConquistaSeeder] {} nova(s) conquista(s) adicionada(s) ao catálogo.", faltantes.size());
        }
    }
}
