package com.npl.na_ponta_do_lapis.service;

import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.TipoCategoria;
import com.npl.na_ponta_do_lapis.entity.Transacao;
import com.npl.na_ponta_do_lapis.entity.enums.EstadoTransacao;
import com.npl.na_ponta_do_lapis.entity.enums.TipoTransacao;
import com.npl.na_ponta_do_lapis.repository.TransacaoRepository;
import com.npl.na_ponta_do_lapis.web.dto.TransacaoRequestDTO;
import com.npl.na_ponta_do_lapis.web.exception.SaldoInsuficienteException;
import com.npl.na_ponta_do_lapis.web.exception.TransacaoNaoExisteException;
import jakarta.transaction.Transactional;
import org.jspecify.annotations.NonNull;
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
        novaTrasacao.setEstado(EstadoTransacao.PENDENTE);

        TipoCategoria categoria = tipoCategoriaService.buscarPorId(transacao.idCategoria());
        ContaFinanceira conta = contaFinanceiraService.buscarContaPorIdObject(transacao.idContaFinanceira());

        novaTrasacao.setCategoria(categoria);
        return validarTransacaoESalvar(novaTrasacao, conta);

    }

    public List<Transacao> listarTransacoes(){
        return transacaoRepository.findAll();
    }

    public Transacao buscarPorId(Long id) {
        return transacaoRepository.findById(id)
                .orElseThrow(() -> new TransacaoNaoExisteException("Transação com ID:"+ id + " não encontrada"));
    }

    @Transactional
    public Transacao atualizarTransacao(Long id, TransacaoRequestDTO dto) {
        // Busca a transação original no banco antes de qualquer mudança.
        // Se o ID não existir, a exceção personalizada será lançada aqui.
        Transacao transacaoExistente = buscarPorId(id);
        // Identifica a conta onde a transação ocorreu originalmente para realizar o estorno.
        ContaFinanceira contaFinanceira = transacaoExistente.getContaFinanceira();

        // Antes de editar, precisa "anular" o impacto que essa transação teve no saldo.
        // Se era uma saída (DESPESA), devolvemos o valor ao saldo.
        // Se era uma entrada (RECEITA), retiramos o valor do saldo.
        if (transacaoExistente.getTipo() == TipoTransacao.DESPESA){
            contaFinanceira.setSaldo(contaFinanceira.getSaldo().add(transacaoExistente.getValor()));
        } else {
            contaFinanceira.setSaldo(contaFinanceira.getSaldo().subtract(transacaoExistente.getValor()));
        }

        // Seta os novos dados do DTO na entidade
        transacaoExistente.setDescricao(dto.descricao());
        transacaoExistente.setValor(dto.valor());
        transacaoExistente.setEstado(dto.estado());
        transacaoExistente.setTipo(dto.tipo());
        transacaoExistente.setDataHora(dto.dataHora());

        // Busca a nova Categoria e a nova Conta Financeira (caso o usuário tenha trocado a conta da transação).
        transacaoExistente.setCategoria(tipoCategoriaService.buscarPorId(dto.idCategoria()));
        ContaFinanceira novaContaFinanceira = contaFinanceiraService.buscarContaPorIdObject(dto.idContaFinanceira());
        // método que verifica se a (nova) conta tem saldo e aplica o (novo) valor.
        return validarTransacaoESalvar(transacaoExistente, novaContaFinanceira);
    }

    public void removerTransacao(Long id) {
        Transacao transacao = buscarPorId(id);
        transacaoRepository.delete(transacao);
    }

    @NonNull
    private Transacao validarTransacaoESalvar(Transacao novaTrasacao, ContaFinanceira conta) {
        // Associa a transação à conta financeira específica
        novaTrasacao.setContaFinanceira(conta);
        // VERIFICAÇÃO DE TIPO: O sistema precisa saber se tira ou coloca dinheiro
        if (novaTrasacao.getTipo() == TipoTransacao.DESPESA) {
            // Compara o saldo da conta com o valor da transação.
            // Se o saldo < valor, o compareTo retorna -1.
            if (conta.getSaldo().compareTo(novaTrasacao.getValor()) < 0) {
                // Se não tem dinheiro, para tudo aqui e lança a exceção!
                throw new SaldoInsuficienteException("Saldo insuficiente na conta: " + conta.getNome());
            }
            // Se passou na validação, subtrai o valor do saldo da conta.
            conta.setSaldo(conta.getSaldo().subtract(novaTrasacao.getValor()));

        } else {
            // Se for uma RECEITA, apenas soma o valor ao saldo atual da conta.
            conta.setSaldo(conta.getSaldo().add(novaTrasacao.getValor()));
        }
        return transacaoRepository.save(novaTrasacao);
    }

}
