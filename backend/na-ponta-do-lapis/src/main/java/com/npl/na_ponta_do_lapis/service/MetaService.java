package com.npl.na_ponta_do_lapis.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.npl.na_ponta_do_lapis.entity.Meta;
import com.npl.na_ponta_do_lapis.repository.MetaRepository;
import com.npl.na_ponta_do_lapis.service.factory.CalculadoraMetaFactory;
import com.npl.na_ponta_do_lapis.service.strategy.CalculadoraMetaStrategy;
import com.npl.na_ponta_do_lapis.web.dto.MetaDTO;
import com.npl.na_ponta_do_lapis.web.dto.MetaResponseDTO;
import com.npl.na_ponta_do_lapis.web.exception.MetaNaoEncontradaException;

@Service
public class MetaService {

    private final CalculadoraMetaFactory factory;
    private final MetaRepository metaRepository;

    public MetaService(MetaRepository metaRepository, CalculadoraMetaFactory factory) {
        this.metaRepository = metaRepository;
        this.factory = factory;
    }

    @Transactional(readOnly = true)
    public List<MetaResponseDTO> listarTodas() {
        return metaRepository.findAll()
                .stream()
                .map(this::montarResponseComProgresso)
                .toList();
    }

    @Transactional(readOnly = true)
    public MetaResponseDTO buscarPorId(Long id) {
        Meta meta = metaRepository.findById(id).orElseThrow(MetaNaoEncontradaException::new);
        return montarResponseComProgresso(meta);
    }

    @Transactional
    public MetaResponseDTO criar(MetaDTO dto) {
        Meta novaMeta = dto.toEntity();
        Meta metaSalva = metaRepository.save(novaMeta);

        return montarResponseComProgresso(metaSalva);
    }

    @Transactional
    public MetaResponseDTO atualizar(Long id, MetaDTO dto) {
        Meta meta = metaRepository.findById(id).orElseThrow(MetaNaoEncontradaException::new);

        meta.setNome(dto.nome());
        meta.setDescricao(dto.descricao());
        meta.setFotoUrl(dto.fotoUrl());
        meta.setValor(dto.valor());
        meta.setDataLimite(dto.dataLimite());
        meta.setTipoMeta(dto.tipoMeta());

        Meta atualizado = metaRepository.save(meta);
        return montarResponseComProgresso(atualizado);
    }

    @Transactional
    public void deletar(Long id) {
        Meta meta = metaRepository.findById(id).orElseThrow(MetaNaoEncontradaException::new);
        metaRepository.delete(meta);
    }

    // Método auxiliar privado que orquestra os padrões Factory e Strategy
    private MetaResponseDTO montarResponseComProgresso(Meta meta) {
        CalculadoraMetaStrategy calculadora = factory.obterCalculadora(meta.getTipoMeta());
        
        double progresso = calculadora.calcularProgresso(meta.getValorAtual(), meta.getValor());
        
        return new MetaResponseDTO(meta, progresso);
    }
}