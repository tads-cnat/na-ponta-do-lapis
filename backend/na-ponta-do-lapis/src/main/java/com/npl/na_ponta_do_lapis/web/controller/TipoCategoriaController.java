package com.npl.na_ponta_do_lapis.web.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.npl.na_ponta_do_lapis.entity.TipoCategoria;
import com.npl.na_ponta_do_lapis.service.TipoCategoraService;
import com.npl.na_ponta_do_lapis.web.dto.CategoriaDTO;
import com.npl.na_ponta_do_lapis.web.dto.TipoCategoriaDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/categorias")
@Tag(name = "Categorias", description = "Gerenciamento de CRUD categorias")
public class TipoCategoriaController {

    private TipoCategoraService tipoCategoriaService;

    public TipoCategoriaController(TipoCategoraService tipoCategoraService){
        this.tipoCategoriaService = tipoCategoraService;
    }

    @Operation(summary = "Listar TipoCategoria do Usuário Logado")
    @GetMapping
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    public ResponseEntity<List<CategoriaDTO>> listarCategorias(){
        return ResponseEntity.status(HttpStatus.OK).body(tipoCategoriaService.listarCategorias());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    public ResponseEntity<TipoCategoria> buscarPorId(@PathVariable Long id){
        boolean categoriaPertenceAoUsuarioLogado = tipoCategoriaService.listarCategorias()
                .stream()
                .anyMatch(categoria -> id.equals(categoria.id()));

        if (!categoriaPertenceAoUsuarioLogado) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.status(HttpStatus.OK).body(tipoCategoriaService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA')")
    public ResponseEntity<TipoCategoriaDTO> criarCategoria(@RequestBody @Valid TipoCategoriaDTO tipoCategoriaDTO){
        TipoCategoria tipoCategoria = tipoCategoriaDTO.toEntity();
        tipoCategoriaService.criarCategoria(tipoCategoria);
     return ResponseEntity.status(HttpStatus.CREATED).body(tipoCategoriaDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USUARIO') OR hasRole('ADMIN_FAMILIA') OR hasRole('ADMIN_SITE')")
    public ResponseEntity<Void> deletarCategoria(@PathVariable Long id){
        tipoCategoriaService.deletarCategoria(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}


