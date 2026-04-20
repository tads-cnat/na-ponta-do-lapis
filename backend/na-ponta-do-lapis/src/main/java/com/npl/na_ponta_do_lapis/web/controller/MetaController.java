package com.npl.na_ponta_do_lapis.web.controller;

<<<<<<< HEAD

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD:backend/na-ponta-do-lapis/src/main/java/com/npl/na_ponta_do_lapis/web/controller/MetaController.java
<<<<<<< HEAD
=======
>>>>>>> 76e0bd3 (corrigindo imports e pacotes)
<<<<<<< HEAD
=======
>>>>>>> 9b4c446 (fix: corrigindo principais bugs de merge)
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.npl.na_ponta_do_lapis.service.MetaService;
import com.npl.na_ponta_do_lapis.web.dto.MetaDTO;
import com.npl.na_ponta_do_lapis.web.dto.MetaResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
<<<<<<< HEAD
=======
=======
>>>>>>> ccf3bb4 (chore: aplica regras do gitigore)
<<<<<<< HEAD
=======
>>>>>>> 5ce2002 (feat: implementa dtos de meta e tipoMeta):npl-back-end/na-ponta-do-lapis/src/main/java/com/npl/na_ponta_do_lapis/web/Controller/MetaController.java
=======
>>>>>>> ccf3bb4 (chore: aplica regras do gitigore)
=======
>>>>>>> 76e0bd3 (corrigindo imports e pacotes)
import com.npl.na_ponta_do_lapis.service.MetaService;
import com.npl.na_ponta_do_lapis.web.dto.MetaDTO;
import com.npl.na_ponta_do_lapis.web.dto.MetaResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
>>>>>>> ccf3bb4 (chore: aplica regras do gitigore)
=======
=======
>>>>>>> 76e0bd3 (corrigindo imports e pacotes)
>>>>>>> ccf3bb4 (chore: aplica regras do gitigore)
=======
>>>>>>> ccf3bb4 (chore: aplica regras do gitigore)
=======
>>>>>>> 9b4c446 (fix: corrigindo principais bugs de merge)

@RestController
@RequestMapping("/metas")
@Tag(name = "Metas", description = "Gerenciamento de metas financeiras")
public class MetaController {
    private final MetaService service;

    public MetaController(MetaService service) {
        this.service = service;
    }

    @Operation(summary = "Listar metas")
    @GetMapping
    public ResponseEntity<List<MetaResponseDTO>> listar(){
        List<MetaResponseDTO> response = service.listarMetas();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Criar metas")
    @PostMapping
    public ResponseEntity<MetaResponseDTO> criar(@RequestBody @Valid MetaDTO metaDTO){
        MetaResponseDTO novaMeta = service.criarMeta(metaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaMeta);
    }
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 76e0bd3 (corrigindo imports e pacotes)
}
=======
}
>>>>>>> ccf3bb4 (chore: aplica regras do gitigore)
=======
}
>>>>>>> ccf3bb4 (chore: aplica regras do gitigore)
<<<<<<< HEAD
=======
}
>>>>>>> ccf3bb4 (chore: aplica regras do gitigore)
=======
>>>>>>> 76e0bd3 (corrigindo imports e pacotes)
=======
}
>>>>>>> 9b4c446 (fix: corrigindo principais bugs de merge)
