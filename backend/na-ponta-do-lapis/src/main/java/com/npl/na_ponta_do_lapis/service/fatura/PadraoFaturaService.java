package com.npl.na_ponta_do_lapis.service.fatura;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.npl.na_ponta_do_lapis.entity.PadraoFatura;
import com.npl.na_ponta_do_lapis.repository.PadraoFaturaRepository;

/**
 * Persistência versionada dos padrões de fatura.
 *
 * Cada novo padrão do mesmo emissor desativa os anteriores. A operação é
 * transacional para não deixar o emissor sem padrão ativo em caso de falha.
 */
@Service
public class PadraoFaturaService {

    private final PadraoFaturaRepository padraoFaturaRepository;

    public PadraoFaturaService(PadraoFaturaRepository padraoFaturaRepository) {
        this.padraoFaturaRepository = padraoFaturaRepository;
    }

    @Transactional
    public PadraoFatura salvarNovaVersao(PadraoFatura novo) {
        List<PadraoFatura> ativos = padraoFaturaRepository
                .findByEmissorAndAtivoTrueOrderByVersaoDesc(novo.getEmissor());

        int proximaVersao = ativos.stream()
                .map(PadraoFatura::getVersao)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;

        ativos.forEach(padrao -> {
            padrao.setAtivo(false);
            padraoFaturaRepository.save(padrao);
        });

        novo.setVersao(proximaVersao);
        novo.setOrigem("IA");
        novo.setAtivo(true);
        return padraoFaturaRepository.save(novo);
    }
}
