package com.npl.na_ponta_do_lapis.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.npl.na_ponta_do_lapis.entity.Meta;
import com.npl.na_ponta_do_lapis.entity.TipoMeta;
import com.npl.na_ponta_do_lapis.repository.MetaRepository;
import com.npl.na_ponta_do_lapis.repository.TipoMetaRepository;
import com.npl.na_ponta_do_lapis.web.dto.MetaDTO;
import com.npl.na_ponta_do_lapis.web.dto.MetaResponseDTO;
import com.npl.na_ponta_do_lapis.web.exception.MetaNaoEncontradaException;

@Service
public class MetaService {
    private final MetaRepository metaRepository;
    private final TipoMetaRepository tipoMetaRepository;

    public MetaService(MetaRepository metaRepository, TipoMetaRepository tipoMetaRepository) {
        this.metaRepository = metaRepository;
        this.tipoMetaRepository = tipoMetaRepository;
    }

    @Transactional
    public List<MetaResponseDTO> listarTodas() {
        return metaRepository.findAll()
                .stream()
                .map(MetaResponseDTO::new)
                .toList();
    }

    @Transactional
    public MetaResponseDTO buscarPorId(Long id){
        Meta meta = metaRepository.findById(id).orElseThrow(MetaNaoEncontradaException::new);
        return new MetaResponseDTO(meta);
    }

    @Transactional
    public MetaResponseDTO criar(MetaDTO dto) {
        TipoMeta tipo = tipoMetaRepository.findById(dto.tipoMetaId())
                .orElseThrow(() -> new RuntimeException("Tipo de Meta não encontrado"));

        Meta novaMeta = dto.toEntity(tipo);
        Meta metaSalva = metaRepository.save(novaMeta);

        return new MetaResponseDTO(metaSalva);
    }

    @Transactional
    public MetaResponseDTO atualizar(Long id, MetaDTO dto){
        Meta meta = metaRepository.findById(id).orElseThrow(MetaNaoEncontradaException::new);
        TipoMeta tipo = tipoMetaRepository.findById(dto.tipoMetaId()).orElseThrow(() -> new RuntimeException(" Tipo Meta não encontrada"));

        meta.setNome(dto.nome());
        meta.setDescricao(dto.descricao());
        meta.setFotoUrl(dto.fotoUrl());
        meta.setValor(dto.valor());
        meta.setDataLimite(dto.dataLimite());
        meta.setTipoMeta(tipo);

        Meta atualizado = metaRepository.save(meta);

        return new MetaResponseDTO(atualizado);

    }

    @Transactional
    public void  deletar(Long id){
        Meta meta = metaRepository.findById(id).orElseThrow(MetaNaoEncontradaException::new);   
        metaRepository.delete(meta);
    }
}
