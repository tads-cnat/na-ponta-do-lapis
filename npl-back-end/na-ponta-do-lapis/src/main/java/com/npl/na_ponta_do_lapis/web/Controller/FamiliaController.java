package com.npl.na_ponta_do_lapis.web.Controller;


import com.npl.na_ponta_do_lapis.service.FamiliaService;
import com.npl.na_ponta_do_lapis.web.Controller.dto.FamiliaDTO;
import com.npl.na_ponta_do_lapis.web.Controller.dto.FamiliaResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/familia")
@Tag(name = "Família", description = "Gerenciamento de Famílias")
public class FamiliaController {

    private FamiliaService familiaService;

    public FamiliaController(FamiliaService familiaService){ this.familiaService = familiaService;}

    @Operation(summary = "Listar Famílias")
    @GetMapping
    private ResponseEntity<List<FamiliaResponseDTO>> listarFamilias(){
        return ResponseEntity.status(HttpStatus.OK).body(familiaService.listarFamilias());
    }

    @Operation(summary = "Buscar por Id")
    @GetMapping("/{id}")
    private ResponseEntity<FamiliaResponseDTO> buscarPorId(@PathVariable Long id){
        return ResponseEntity.status(HttpStatus.OK).body(familiaService.buscarFamiliaPorID(id));
    }

    @Operation(summary = "Cadastrar Família")
    @PostMapping
    private ResponseEntity<FamiliaResponseDTO> cadastrarFamilia(@RequestBody FamiliaDTO familiaDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(familiaService.criarFamilia(familiaDTO));
    }

    @Operation(summary = "Excluir Família")
    @DeleteMapping("/{id}")
    private ResponseEntity<Void> excluirFamilia(@PathVariable Long id){
        familiaService.excluirFamilia(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(summary = "Editar Família")
    @PutMapping("/{id}")
    private ResponseEntity<FamiliaResponseDTO> editarFamilia(@PathVariable Long id,
                                                             @RequestParam(required = false) String nome,
                                                             @RequestParam(required = false) String foto){
        return ResponseEntity.status(HttpStatus.OK).body(familiaService.editarFamilia(id, new FamiliaDTO(nome, foto)));
    }

}
