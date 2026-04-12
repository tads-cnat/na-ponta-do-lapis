package com.npl.na_ponta_do_lapis.web.Controller;

import com.npl.na_ponta_do_lapis.entity.Meta;
import com.npl.na_ponta_do_lapis.service.MetaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/metas")
@Tag(name = "Metas", description = "Gerenciamento de metas financeiras")
public class MetaController {
    private final MetaService service;

    public MetaController(MetaService service) {
        this.service = service;
    }

    @Operation(summary = "Listar metas")
    @PostMapping
    public Meta criar(@RequestBody Meta meta){
        return service.criarMeta(meta);
    }

    @Operation(summary = "Criar metas")
    @GetMapping
    public List<Meta> listar(){
        return service.listarMetas();
    }
}
