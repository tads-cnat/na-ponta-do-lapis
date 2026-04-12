package com.npl.na_ponta_do_lapis.web.Controller;

import com.npl.na_ponta_do_lapis.entity.TipoMeta;
import com.npl.na_ponta_do_lapis.service.TipoMetaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("tipos_metas")
@Tag(name = "Tipo Meta", description = "Gerenciamento de tipos de metas financeiras")
public class TipoMetaController {
    private final TipoMetaService service;

    public TipoMetaController(TipoMetaService service) {
        this.service = service;
    }

    @Operation(summary = "Cria um tipo de meta financeira")
    @PostMapping
    public TipoMeta criar(@RequestBody TipoMeta tipoMeta){
        return service.criarTipoMeta(tipoMeta);
    }

    @Operation(summary = "Lista tipos de metas financeiras")
    @GetMapping
    public List<TipoMeta> listar(){
        return  service.listarTipoMeta();
    }

}
