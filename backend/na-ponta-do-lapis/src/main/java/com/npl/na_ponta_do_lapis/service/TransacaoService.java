package com.npl.na_ponta_do_lapis.service;

import com.npl.na_ponta_do_lapis.entity.*;
import com.npl.na_ponta_do_lapis.entity.enums.EstadoTransacao;
import com.npl.na_ponta_do_lapis.entity.enums.TipoTransacao;
import com.npl.na_ponta_do_lapis.repository.TransacaoRepository;
import com.npl.na_ponta_do_lapis.security.jwt.JwtUtil;
import com.npl.na_ponta_do_lapis.web.dto.TransacaoFaturaDTO;
import com.npl.na_ponta_do_lapis.web.dto.TransacaoRequestDTO;
import com.npl.na_ponta_do_lapis.web.exception.TransacaoNaoExisteException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static com.npl.na_ponta_do_lapis.security.jwt.JwtAuthFilter.getEmailUsuarioLogado;

@Service
public class TransacaoService {
    private final TransacaoRepository transacaoRepository;
    private final UsuarioService usuarioService;
    private final MarcadorService marcadorService;

    private TipoCategoraService tipoCategoriaService;

    private ContaFinanceiraService contaFinanceiraService;

    public TransacaoService(TransacaoRepository transacaoRepository, TipoCategoraService tipoCategoriaService, ContaFinanceiraService contaFinanceiraService, UsuarioService usuarioService, MarcadorService marcadorService) {
        this.transacaoRepository = transacaoRepository;
        this.contaFinanceiraService = contaFinanceiraService;
        this.tipoCategoriaService = tipoCategoriaService;
        this.usuarioService = usuarioService;
        this.marcadorService = marcadorService;
    }

    @Transactional
    public Transacao criarTransacao(TransacaoRequestDTO transacao) throws AccessDeniedException {
         ContaFinanceira conta = contaFinanceiraService.buscarContaPorIdObject(transacao.idContaFinanceira());
         String email = getEmailUsuarioLogado();

        if (!conta.getUsuario().getEmail().equals(email)){
            throw  new AccessDeniedException("Você não tem permissão para criar uma transação nessa conta financeira.");
        }


        Transacao novaTrasacao = new Transacao();
        novaTrasacao.setDescricao(transacao.descricao());
        novaTrasacao.setValor(transacao.valor());
        novaTrasacao.setTipo(transacao.tipo());

        LocalDateTime dataUsuario = transacao.dataHora();
        LocalTime horaAtual = LocalTime.now();
        novaTrasacao.setDataHora(dataUsuario.with(horaAtual));

        novaTrasacao.setEstado(EstadoTransacao.PENDENTE);

        TipoCategoria categoria = tipoCategoriaService.buscarPorId(transacao.idCategoria());

        novaTrasacao.setCategoria(categoria);
        // Associa a transação à conta financeira específica
        novaTrasacao.setContaFinanceira(conta);
        // VERIFICAÇÃO DE TIPO: O sistema precisa saber se tira ou coloca dinheiro
        if (transacao.marcadorId() != null){
            Marcador marcador = marcadorService.buscarMarcadorPorIdObject(transacao.marcadorId());

            if (!marcador.getUsuario().getEmail().equals(email)){
                throw new AccessDeniedException("Você não tem permissão para criar uma transação com esse marcador.");
            }
            novaTrasacao.setMarcador(marcador);
        }
        if (novaTrasacao.getTipo() == TipoTransacao.DESPESA) {
            conta.setSaldo(conta.getSaldo().subtract(novaTrasacao.getValor()));
        } else {
            // Se for uma RECEITA, apenas soma o valor ao saldo atual da conta.
            conta.setSaldo(conta.getSaldo().add(novaTrasacao.getValor()));
        }
        return transacaoRepository.save(novaTrasacao);

    }

    @Transactional
    public void salvarFaturaEmLote(List<TransacaoFaturaDTO> dtos, Long contaId) {
        
        // 1. Busca a conta financeira (cartão) onde as despesas vão entrar
        ContaFinanceira conta = contaFinanceiraService.buscarContaPorIdObject(contaId);

        // 2. Converte a lista de DTOs para a lista de Entidades
        List<Transacao> transacoesParaSalvar = dtos.stream().map(dto -> {
            Transacao t = new Transacao();
            t.setDescricao(dto.descricao());
            t.setValor(dto.valor());
            
            // Adiciona a hora zerada para converter a data (YYYY-MM-DD) do DTO em LocalDateTime
            t.setDataHora(dto.data().atStartOfDay()); 
            
            t.setTipo(TipoTransacao.DESPESA); // Regra de negócio: Faturas são despesas
            t.setEstado(EstadoTransacao.REALIZADA); // Ou "PENDENTE", se preferir que o usuário dê baixa manual depois
            t.setContaFinanceira(conta);
            
            // 3. Resolve a Categoria buscando pelo nome que veio do Angular
            TipoCategoria categoria = tipoCategoriaService.buscarPorNome(dto.categoria());
            t.setCategoria(categoria);

            return t;
        }).toList();

        // 4. Dispara o INSERT em lote 
        transacaoRepository.saveAll(transacoesParaSalvar);
    }

    public List<Transacao> listarTransacoesUsuarioNaSessao() {
        System.out.println("Usuário na sessão"+getEmailUsuarioLogado());
        return transacaoRepository.buscarTransacoesUsuarioLogado(getEmailUsuarioLogado());
    }

    public List<Transacao> buscarPorDescricao(String descricao) {
        return transacaoRepository.buscarPorDescricao(getEmailUsuarioLogado(), descricao);
    }

    public Transacao buscarPorId(Long id) {
        return transacaoRepository.findById(id)
                .orElseThrow(() -> new TransacaoNaoExisteException("Transação com ID:"+ id + " não encontrada"));
    }

    @Transactional
    public Transacao atualizarTransacao(Long id, TransacaoRequestDTO dto) throws AccessDeniedException {
        // Busca a transação original no banco antes de qualquer mudança.
        Transacao transacaoExistente = buscarPorId(id);
        // Identifica a conta onde a transação ocorreu originalmente para realizar o estorno.
        ContaFinanceira contaFinanceiraAntiga = transacaoExistente.getContaFinanceira();
        Marcador marcadorExistente = marcadorService.buscarMarcadorPorIdObject(dto.marcadorId());

        String emailUsuarioLogado = getEmailUsuarioLogado();
        if (!contaFinanceiraAntiga.getUsuario().getEmail().equals(emailUsuarioLogado)){
            throw new AccessDeniedException("Você não tem permissão para remover uma transação nessa conta financeira.");
        }

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
        transacaoExistente.setMarcador(marcadorExistente);

        // Busca a nova Categoria e a nova Conta Financeira (caso o usuario tenha trocado a conta da transação).
        transacaoExistente.setCategoria(tipoCategoriaService.buscarPorId(dto.idCategoria()));
        ContaFinanceira novaContaFinanceira = contaFinanceiraService.buscarContaPorIdObject(dto.idContaFinanceira());


        // Associa a transação à conta financeira específica
        transacaoExistente.setContaFinanceira(novaContaFinanceira);
        // VERIFICAÇÃO DE TIPO: O sistema precisa saber se tira ou coloca dinheiro
        if (transacaoExistente.getTipo() == TipoTransacao.DESPESA) {
            novaContaFinanceira.setSaldo(novaContaFinanceira.getSaldo().subtract(transacaoExistente.getValor()));
        } else {
            novaContaFinanceira.setSaldo(novaContaFinanceira.getSaldo().add(transacaoExistente.getValor()));
        }

        return transacaoRepository.save(transacaoExistente);
    }

    public void removerTransacao(Long id) throws AccessDeniedException {
        Transacao transacao = buscarPorId(id);
        ContaFinanceira conta = transacao.getContaFinanceira();

        String emailUsuarioLogado = getEmailUsuarioLogado();
        if (!conta.getUsuario().getEmail().equals(emailUsuarioLogado)){
            throw new AccessDeniedException("Você não tem permissão para remover uma transação nessa conta financeira.");
        }

        if (transacao.getTipo() == TipoTransacao.DESPESA){
            conta.setSaldo(conta.getSaldo().add(transacao.getValor()));
        } else {
            conta.setSaldo(conta.getSaldo().subtract(transacao.getValor()));
        }

        transacaoRepository.delete(transacao);
    }

}
