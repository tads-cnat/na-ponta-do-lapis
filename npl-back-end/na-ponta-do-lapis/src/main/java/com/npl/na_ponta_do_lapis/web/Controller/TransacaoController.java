package com.npl.na_ponta_do_lapis.web.Controller;

import com.npl.na_ponta_do_lapis.entity.Transacao;
import com.npl.na_ponta_do_lapis.service.TransacaoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transacoes")
public class TransacaoController {

    private final TransacaoService transacaoService;

    public TransacaoController(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @PostMapping
    public Transacao criarTransacao(@RequestBody Transacao transacao) {
        return transacaoService.criarTransacao(transacao);
    }

    @GetMapping
    public List<Transacao> listarTransacoes() {
        return transacaoService.listarTransacoes();
    }

    @GetMapping("/{id}")
    public Transacao buscarPorId(@PathVariable Long id) {
        return transacaoService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Transacao atualizarTransacao(@PathVariable Long id,
                               @RequestBody Transacao transacao) {
        return transacaoService.atualizarTransacao(id, transacao);
    }

    @DeleteMapping("/{id}")
    public void remover(@PathVariable Long id) {
        transacaoService.removerTransacao(id);
    }
}
