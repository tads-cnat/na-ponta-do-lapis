package com.npl.na_ponta_do_lapis.service;

import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.TipoCategoria;
import com.npl.na_ponta_do_lapis.entity.Transacao;
import com.npl.na_ponta_do_lapis.entity.enums.TipoTransacao;
import com.npl.na_ponta_do_lapis.repository.TransacaoRepository;
import com.npl.na_ponta_do_lapis.web.dto.TransacaoRequestDTO;
import com.npl.na_ponta_do_lapis.web.exception.SaldoInsuficienteException;
import com.npl.na_ponta_do_lapis.web.exception.TransacaoNaoExisteException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransacaoService {
    private final TransacaoRepository transacaoRepository;

    private TipoCategoraService tipoCategoriaService;

    private ContaFinanceiraService contaFinanceiraService;

    public TransacaoService(TransacaoRepository transacaoRepository, TipoCategoraService tipoCategoriaService, ContaFinanceiraService contaFinanceiraService) {
        this.transacaoRepository = transacaoRepository;
        this.contaFinanceiraService = contaFinanceiraService;
        this.tipoCategoriaService = tipoCategoriaService;
    }

    @Transactional
    public Transacao criarTransacao(TransacaoRequestDTO transacao) {
        Transacao novaTrasacao = new Transacao();
        novaTrasacao.setDescricao(transacao.descricao());
        novaTrasacao.setValor(transacao.valor());
        novaTrasacao.setTipo(transacao.tipo());
        novaTrasacao.setDataHora(transacao.dataHora());

        TipoCategoria categoria = tipoCategoriaService.buscarPorId(transacao.idCategoria());
        ContaFinanceira conta = contaFinanceiraService.buscarEntidadePorId(transacao.idContaFinanceira());

        novaTrasacao.setCategoria(categoria);
        novaTrasacao.setContaFinanceira(conta);

        if (novaTrasacao.getTipo() == TipoTransacao.DESPESA){
            if (conta.getSaldo().compareTo(novaTrasacao.getValor()) < 0){
                throw new SaldoInsuficienteException("Saldo insuficiente na conta: " + conta.getNome());
            }
            conta.setSaldo(conta.getSaldo().subtract(novaTrasacao.getValor()));
        } else {
            conta.setSaldo(conta.getSaldo().add(novaTrasacao.getValor()));
        }
        return transacaoRepository.save(novaTrasacao);

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
