package com.npl.na_ponta_do_lapis.service;

import com.npl.na_ponta_do_lapis.entity.TipoMeta;
import com.npl.na_ponta_do_lapis.repository.TipoMetaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TipoMetaService {
    private final TipoMetaRepository tipoMetaRepository;

    public TipoMetaService(TipoMetaRepository tipoMetaRepository) {
        this.tipoMetaRepository = tipoMetaRepository;
    }

    public TipoMeta criarTipoMeta(TipoMeta tipoMeta){
        return tipoMetaRepository.save(tipoMeta);
    }

    public List<TipoMeta> listarTipoMeta(){
        return tipoMetaRepository.findAll();
    }
}
