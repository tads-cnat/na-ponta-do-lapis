package com.npl.na_ponta_do_lapis.service;

import com.npl.na_ponta_do_lapis.entity.TipoCategoria;
import com.npl.na_ponta_do_lapis.repository.TipoCategoriaRepository;
import com.npl.na_ponta_do_lapis.web.Controller.Exception.TipoCategoriaIdNaoExisteException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
public class TipoCategoraService {

    private TipoCategoriaRepository tipoCategoriaRepository;

    public TipoCategoraService(TipoCategoriaRepository tipoCategoriaRepository){
        this.tipoCategoriaRepository = tipoCategoriaRepository;
    }

    public List<TipoCategoria> listarCategorias(){
        return tipoCategoriaRepository.findAll();
    }

    public TipoCategoria buscarPorId(Long id){
        return tipoCategoriaRepository.findById(id).orElseThrow(
                () -> new TipoCategoriaIdNaoExisteException("Categoria de ID:" + id+" não encontrado!")
        );
    }

    @Transactional
    public TipoCategoria criarCategoria( TipoCategoria categoria){
        return tipoCategoriaRepository.save(categoria);
    }

    @Transactional
    public void deletarCategoria(Long id){
        if (!tipoCategoriaRepository.existsById(id)){
            throw new TipoCategoriaIdNaoExisteException("Categoria de ID: " + id+ " não existe!");
        }
        tipoCategoriaRepository.deleteById(id);
    }
}
