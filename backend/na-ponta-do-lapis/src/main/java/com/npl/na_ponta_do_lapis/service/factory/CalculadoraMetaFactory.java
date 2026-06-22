package com.npl.na_ponta_do_lapis.service.factory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.npl.na_ponta_do_lapis.entity.enums.TipoMeta;
import com.npl.na_ponta_do_lapis.service.strategy.CalculadoraMetaStrategy;

@Component
public class CalculadoraMetaFactory {
    private final Map<TipoMeta, CalculadoraMetaStrategy> strategies;

    public CalculadoraMetaFactory(List<CalculadoraMetaStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(CalculadoraMetaStrategy::getTipo, s -> s));
    }

    public CalculadoraMetaStrategy obterCalculadora(TipoMeta tipo) {
        CalculadoraMetaStrategy strategy = strategies.get(tipo);
        if (strategy == null) {
            throw new IllegalArgumentException("Estratégia não encontrada para o tipo: " + tipo);
        }
        return strategy;
    }
}
