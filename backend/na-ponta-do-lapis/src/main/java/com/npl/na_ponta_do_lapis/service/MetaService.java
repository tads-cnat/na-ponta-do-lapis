package com.npl.na_ponta_do_lapis.service;

import com.npl.na_ponta_do_lapis.entity.Meta;
import com.npl.na_ponta_do_lapis.entity.TipoMeta;
import com.npl.na_ponta_do_lapis.repository.MetaRepository;
import com.npl.na_ponta_do_lapis.repository.TipoMetaRepository;
import com.npl.na_ponta_do_lapis.web.dto.MetaDTO;
import com.npl.na_ponta_do_lapis.web.dto.MetaResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MetaService {
    private final MetaRepository metaRepository;
    private final TipoMetaRepository tipoMetaRepository;

    public MetaService(MetaRepository metaRepository, TipoMetaRepository tipoMetaRepository) {
        this.metaRepository = metaRepository;
        this.tipoMetaRepository = tipoMetaRepository;
    }

    public List<MetaResponseDTO> listarMetas() {
        return metaRepository.findAll()
                .stream()
                .map(MetaResponseDTO::new)
                .toList();
    }

    public MetaResponseDTO criarMeta(MetaDTO dto) {
        TipoMeta tipo = tipoMetaRepository.findById(dto.tipoMetaId())
                .orElseThrow(() -> new RuntimeException("Tipo de Meta não encontrado"));

        Meta novaMeta = dto.toEntity(tipo);
        Meta metaSalva = metaRepository.save(novaMeta);

        return new MetaResponseDTO(metaSalva);
    }
}
