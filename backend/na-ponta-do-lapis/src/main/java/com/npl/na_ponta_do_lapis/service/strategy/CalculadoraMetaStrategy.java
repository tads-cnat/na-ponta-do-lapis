package com.npl.na_ponta_do_lapis.service.strategy;

import com.npl.na_ponta_do_lapis.entity.enums.TipoMeta;
import java.math.BigDecimal; 

public interface CalculadoraMetaStrategy {
    TipoMeta getTipo();
    
    double calcularProgresso(BigDecimal valorAtual, BigDecimal valorTotal); 
}