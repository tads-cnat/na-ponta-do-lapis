package com.npl.na_ponta_do_lapis.service;

import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.TipoCategoria;
import com.npl.na_ponta_do_lapis.entity.Transacao;
<<<<<<< HEAD
<<<<<<< HEAD
import com.npl.na_ponta_do_lapis.entity.enums.EstadoTransacao;
=======
>>>>>>> e4f88b5 (refactor:corrigindo POST criaTransacoes e implementando regra de negorcio ao criar transacao)
=======
import com.npl.na_ponta_do_lapis.entity.enums.EstadoTransacao;
>>>>>>> f2c067a (refactor(Transacoes ): corrigindo endpoint transacao, add regra de negocio no atualizar Transacao e melhorando resposta dos endpoits transacoes)
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
<<<<<<< HEAD
<<<<<<< HEAD
        novaTrasacao.setEstado(EstadoTransacao.PENDENTE);

        TipoCategoria categoria = tipoCategoriaService.buscarPorId(transacao.idCategoria());
        ContaFinanceira conta = contaFinanceiraService.buscarContaPorIdObject(transacao.idContaFinanceira());

        novaTrasacao.setCategoria(categoria);
<<<<<<< HEAD
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
=======
=======
        novaTrasacao.setEstado(EstadoTransacao.PENDENTE);
>>>>>>> f2c067a (refactor(Transacoes ): corrigindo endpoint transacao, add regra de negocio no atualizar Transacao e melhorando resposta dos endpoits transacoes)

        TipoCategoria categoria = tipoCategoriaService.buscarPorId(transacao.idCategoria());
        ContaFinanceira conta = contaFinanceiraService.buscarContaPorIdObject(transacao.idContaFinanceira());

        novaTrasacao.setCategoria(categoria);
<<<<<<< HEAD
        novaTrasacao.setContaFinanceira(conta);

        if (novaTrasacao.getTipo() == TipoTransacao.DESPESA){
            if (conta.getSaldo().compareTo(novaTrasacao.getValor()) < 0){
                throw new SaldoInsuficienteException("Saldo insuficiente na conta: " + conta.getNome());
            }
            conta.setSaldo(conta.getSaldo().subtract(novaTrasacao.getValor()));
        } else {
>>>>>>> e4f88b5 (refactor:corrigindo POST criaTransacoes e implementando regra de negorcio ao criar transacao)
            conta.setSaldo(conta.getSaldo().add(novaTrasacao.getValor()));
        }
        return transacaoRepository.save(novaTrasacao);
=======
        return validarTransacaoESalvar(novaTrasacao, conta);
>>>>>>> f2c067a (refactor(Transacoes ): corrigindo endpoint transacao, add regra de negocio no atualizar Transacao e melhorando resposta dos endpoits transacoes)

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
<<<<<<< HEAD
        Transacao transacaoExistente = buscarPorId(id);
        // Identifica a conta onde a transação ocorreu originalmente para realizar o estorno.
        ContaFinanceira contaFinanceiraAntiga = transacaoExistente.getContaFinanceira();
=======
        // Se o ID não existir, a exceção personalizada será lançada aqui.
        Transacao transacaoExistente = buscarPorId(id);
        // Identifica a conta onde a transação ocorreu originalmente para realizar o estorno.
        ContaFinanceira contaFinanceira = transacaoExistente.getContaFinanceira();
>>>>>>> f2c067a (refactor(Transacoes ): corrigindo endpoint transacao, add regra de negocio no atualizar Transacao e melhorando resposta dos endpoits transacoes)

        // Antes de editar, precisa "anular" o impacto que essa transação teve no saldo.
        // Se era uma saída (DESPESA), devolvemos o valor ao saldo.
        // Se era uma entrada (RECEITA), retiramos o valor do saldo.
        if (transacaoExistente.getTipo() == TipoTransacao.DESPESA){
<<<<<<< HEAD
            contaFinanceiraAntiga.setSaldo(contaFinanceiraAntiga.getSaldo().add(transacaoExistente.getValor()));
        } else {
            contaFinanceiraAntiga.setSaldo(contaFinanceiraAntiga.getSaldo().subtract(transacaoExistente.getValor()));
=======
            contaFinanceira.setSaldo(contaFinanceira.getSaldo().add(transacaoExistente.getValor()));
        } else {
            contaFinanceira.setSaldo(contaFinanceira.getSaldo().subtract(transacaoExistente.getValor()));
>>>>>>> f2c067a (refactor(Transacoes ): corrigindo endpoint transacao, add regra de negocio no atualizar Transacao e melhorando resposta dos endpoits transacoes)
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
<<<<<<< HEAD


        // Associa a transação à conta financeira específica
        transacaoExistente.setContaFinanceira(novaContaFinanceira);
        // VERIFICAÇÃO DE TIPO: O sistema precisa saber se tira ou coloca dinheiro
        if (transacaoExistente.getTipo() == TipoTransacao.DESPESA) {
            // Valida se a conta (nova ou antiga) tem saldo para o novo valor
            if (novaContaFinanceira.getSaldo().compareTo(transacaoExistente.getValor()) < 0) {
                throw new SaldoInsuficienteException("Saldo insuficiente para atualizar esta transação.");
            }
            novaContaFinanceira.setSaldo(novaContaFinanceira.getSaldo().subtract(transacaoExistente.getValor()));
        } else {
            novaContaFinanceira.setSaldo(novaContaFinanceira.getSaldo().add(transacaoExistente.getValor()));
        }

        return transacaoRepository.save(transacaoExistente);
=======
        // método que verifica se a (nova) conta tem saldo e aplica o (novo) valor.
        return validarTransacaoESalvar(transacaoExistente, novaContaFinanceira);
>>>>>>> f2c067a (refactor(Transacoes ): corrigindo endpoint transacao, add regra de negocio no atualizar Transacao e melhorando resposta dos endpoits transacoes)
    }

    public void removerTransacao(Long id) {
        Transacao transacao = buscarPorId(id);
        transacaoRepository.delete(transacao);
    }

    @NonNull
    private Transacao validarTransacaoESalvar(Transacao novaTrasacao, ContaFinanceira conta) {
=======
>>>>>>> 84b4d73 (refactor: pequenas correções de legibilidade)
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
        Transacao transacaoExistente = buscarPorId(id);
        // Identifica a conta onde a transação ocorreu originalmente para realizar o estorno.
        ContaFinanceira contaFinanceiraAntiga = transacaoExistente.getContaFinanceira();

        // Antes de editar, precisa "anular" o impacto que essa transação teve no saldo.
        // Se era uma saída (DESPESA), devolvemos o valor ao saldo.
        // Se era uma entrada (RECEITA), retiramos o valor do saldo.
        if (transacaoExistente.getTipo() == TipoTransacao.DESPESA){
            contaFinanceiraAntiga.setSaldo(contaFinanceiraAntiga.getSaldo().add(transacaoExistente.getValor()));
        } else {
            contaFinanceiraAntiga.setSaldo(contaFinanceiraAntiga.getSaldo().subtract(transacaoExistente.getValor()));
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


        // Associa a transação à conta financeira específica
        transacaoExistente.setContaFinanceira(novaContaFinanceira);
        // VERIFICAÇÃO DE TIPO: O sistema precisa saber se tira ou coloca dinheiro
        if (transacaoExistente.getTipo() == TipoTransacao.DESPESA) {
            // Valida se a conta (nova ou antiga) tem saldo para o novo valor
            if (novaContaFinanceira.getSaldo().compareTo(transacaoExistente.getValor()) < 0) {
                throw new SaldoInsuficienteException("Saldo insuficiente para atualizar esta transação.");
            }
            novaContaFinanceira.setSaldo(novaContaFinanceira.getSaldo().subtract(transacaoExistente.getValor()));
        } else {
            novaContaFinanceira.setSaldo(novaContaFinanceira.getSaldo().add(transacaoExistente.getValor()));
        }

        return transacaoRepository.save(transacaoExistente);
    }

    public void removerTransacao(Long id) {
        Transacao transacao = buscarPorId(id);
        transacaoRepository.delete(transacao);
    }

}
