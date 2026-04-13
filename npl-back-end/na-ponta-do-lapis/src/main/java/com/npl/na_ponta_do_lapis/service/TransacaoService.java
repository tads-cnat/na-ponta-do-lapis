package com.npl.na_ponta_do_lapis.service;

import com.npl.na_ponta_do_lapis.entity.Transacao;
import com.npl.na_ponta_do_lapis.repository.TransacaoRepository;
import com.npl.na_ponta_do_lapis.web.Controller.Exception.TransacaoNaoExisteException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransacaoService {
    private final TransacaoRepository transacaoRepository;

    public TransacaoService(TransacaoRepository transacaoRepository) {
        this.transacaoRepository = transacaoRepository;
    }

    public Transacao criarTransacao(Transacao transacao) {
        return transacaoRepository.save(transacao);
    }

    public List<Transacao> listarTransacoes(){
        return transacaoRepository.findAll();
    }

    public Transacao buscarPorId(Long id) {
        return transacaoRepository.findById(id)
                .orElseThrow(() -> new TransacaoNaoExisteException("Transação com ID:"+ id + " não encontrada"));
    }

    public Transacao atualizarTransacao(Long id, Transacao novaTransacao) {
        Transacao transacao = buscarPorId(id);
        transacao.setDescricao(novaTransacao.getDescricao());
        transacao.setValor(novaTransacao.getValor());
        transacao.setCategoria(novaTransacao.getCategoria());
        transacao.setEstado(novaTransacao.getEstado());
        transacao.setTipo(novaTransacao.getTipo());
        transacao.setDataHora(novaTransacao.getDataHora());
        transacao.setContaFinanceira(novaTransacao.getContaFinanceira());
        return transacaoRepository.save(transacao);
    }

    public void removerTransacao(Long id) {
        Transacao transacao = buscarPorId(id);
        transacaoRepository.delete(transacao);
    }

}
