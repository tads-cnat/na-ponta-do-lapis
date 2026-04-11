package com.npl.na_ponta_do_lapis.web.Controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.service.ContaFinanceiraService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/contas")
@Tag(name = "Contas", description = "Gerenciamento de contas financeiras")
public class ContaFinanceiraController {
    private final ContaFinanceiraService service;

    public ContaFinanceiraController(ContaFinanceiraService service) {
        this.service = service;
    }

    @Operation(summary = "Listar contas")
    @PostMapping
    public ContaFinanceira criar(@RequestBody ContaFinanceira conta){
        return service.criarConta(conta);
    }

    @Operation(summary = "Criar contas")
    @GetMapping
    public List<ContaFinanceira> listar(){
        return service.listarContas();
    }
}