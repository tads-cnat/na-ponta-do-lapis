package com.npl.na_ponta_do_lapis.service;

import com.npl.na_ponta_do_lapis.entity.TipoCategoria;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.repository.TipoCategoriaRepository;
import com.npl.na_ponta_do_lapis.repository.UsuarioRepository;
import com.npl.na_ponta_do_lapis.web.dto.CategoriaDTO;
import com.npl.na_ponta_do_lapis.web.dto.TipoCategoriaDTO;
import com.npl.na_ponta_do_lapis.web.exception.TipoCategoriaIdNaoExisteException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.npl.na_ponta_do_lapis.security.jwt.JwtAuthFilter.getEmailUsuarioLogado;

@Service
public class TipoCategoraService {

    private TipoCategoriaRepository tipoCategoriaRepository;
    private UsuarioService usuarioService;

    public TipoCategoraService(TipoCategoriaRepository tipoCategoriaRepository, UsuarioService usuarioService) {
        this.tipoCategoriaRepository = tipoCategoriaRepository;
        this.usuarioService = usuarioService;
    }

    public List<CategoriaDTO> listarCategorias() {
        String email = getEmailUsuarioLogado();
        List<CategoriaDTO> tipoCategoriaUsuarioLogado = tipoCategoriaRepository.findAll().stream()
                .map(tipoCategoria -> new CategoriaDTO(tipoCategoria.getId(), tipoCategoria.getNome()))
                .toList();

        return tipoCategoriaUsuarioLogado;
    }

    public TipoCategoria buscarPorId(Long id){
        return tipoCategoriaRepository.findById(id).orElseThrow(
                () -> new TipoCategoriaIdNaoExisteException("Categoria de ID:" + id+" não encontrado!")
        );
    }

    public TipoCategoria buscarPorNome(String nome){
        return tipoCategoriaRepository.findByNome(nome).orElseThrow(
                () -> new TipoCategoriaIdNaoExisteException("Categoria de Nome:" + nome+" não encontrado!")
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
