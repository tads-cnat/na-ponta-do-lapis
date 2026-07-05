package com.npl.na_ponta_do_lapis.service;

import com.npl.na_ponta_do_lapis.entity.TipoCategoria;
import com.npl.na_ponta_do_lapis.repository.TipoCategoriaRepository;
import com.npl.na_ponta_do_lapis.web.dto.CategoriaDTO;
import com.npl.na_ponta_do_lapis.web.exception.TipoCategoriaIdNaoExisteException;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TipoCategoraService {

    private static final Logger log = LoggerFactory.getLogger(TipoCategoraService.class);

    private TipoCategoriaRepository tipoCategoriaRepository;
    private UsuarioService usuarioService;

    public TipoCategoraService(TipoCategoriaRepository tipoCategoriaRepository, UsuarioService usuarioService) {
        this.tipoCategoriaRepository = tipoCategoriaRepository;
        this.usuarioService = usuarioService;
    }

    @Transactional(readOnly = true)
    public List<CategoriaDTO> listarCategorias() {
        log.debug("Listando categorias");
        List<CategoriaDTO> categorias = tipoCategoriaRepository.findAll().stream()
                .map(tipoCategoria -> new CategoriaDTO(tipoCategoria.getId(), tipoCategoria.getNome()))
                .toList();
        log.debug("Total de categorias encontradas: {}", categorias.size());
        return categorias;
    }

    @Transactional(readOnly = true)
    public TipoCategoria buscarPorId(Long id){
        log.debug("Buscando categoria por ID: {}", id);
        return tipoCategoriaRepository.findById(id).orElseThrow(
                () -> new TipoCategoriaIdNaoExisteException("Categoria de ID:" + id+" não encontrado!")
        );
    }

    @Transactional(readOnly = true)
    public TipoCategoria buscarPorNome(String nome){
        log.debug("Buscando categoria por nome: {}", nome);
        return tipoCategoriaRepository.findByNome(nome).orElseThrow(
                () -> new TipoCategoriaIdNaoExisteException("Categoria de Nome:" + nome+" não encontrado!")
        );
    }

    @Transactional
    public TipoCategoria criarCategoria(TipoCategoria categoria){
        log.debug("Criando categoria: {}", categoria.getNome());
        return tipoCategoriaRepository.save(categoria);
    }

    @Transactional
    public void deletarCategoria(Long id){
        log.debug("Deletando categoria de ID: {}", id);
        if (!tipoCategoriaRepository.existsById(id)){
            throw new TipoCategoriaIdNaoExisteException("Categoria de ID: " + id+ " não existe!");
        }
        tipoCategoriaRepository.deleteById(id);
    }
}
