package com.npl.na_ponta_do_lapis.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.Papel;
import com.npl.na_ponta_do_lapis.repository.ContaFinanceiraRepository;
import com.npl.na_ponta_do_lapis.security.jwt.JwtAuthFilter;
import com.npl.na_ponta_do_lapis.web.dto.ContaFinanceiraDTO;
import com.npl.na_ponta_do_lapis.web.dto.ContaFinanceiraPatchDTO;
import com.npl.na_ponta_do_lapis.web.dto.ContaFinanceiraResponseDTO;
import com.npl.na_ponta_do_lapis.web.exception.ContaIdNaoExisteException;

import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;


@Service
public class ContaFinanceiraService {

    private final ContaFinanceiraRepository contaFinanceiraRepository;
    private final UsuarioService usuarioService;

    public ContaFinanceiraService(ContaFinanceiraRepository contaFinanceiraRepository, UsuarioService usuarioService) {
        this.contaFinanceiraRepository = contaFinanceiraRepository;
        this.usuarioService = usuarioService;
    }

    @Transactional
    public ContaFinanceiraResponseDTO criarConta(ContaFinanceiraDTO contaDTO) {
        String email = JwtAuthFilter.getEmailUsuarioLogado();
        Usuario usuario = usuarioService.buscarUsuarioPorEmail(email);
        ContaFinanceira novaConta = contaDTO.toEntity(usuario);
        try {
            contaFinanceiraRepository.save(novaConta);  
        }
        catch (DataIntegrityViolationException e){
            if (e.getCause() instanceof ConstraintViolationException) {
                throw new IllegalArgumentException("Já existe uma conta com este nome para este usuário.");
            }
            throw e;
        }
        return new ContaFinanceiraResponseDTO(novaConta);
    }

    public List<ContaFinanceiraResponseDTO> listarContas() {
        return contaFinanceiraRepository.findAll().stream()
        .map(contaFinanceira -> new ContaFinanceiraResponseDTO(contaFinanceira))
        .toList();
    }

    @Transactional
    public ContaFinanceiraResponseDTO atualizarConta(Long id, ContaFinanceiraDTO contaDTO){

        ContaFinanceira conta = contaFinanceiraRepository.findById(id)
            .orElseThrow( () -> new ContaIdNaoExisteException("Conta de ID: " + id + " não existe"));
        validarDonoOuAdmin(conta);

        conta.setNome(contaDTO.nome());
        conta.setSaldo(contaDTO.saldo());
        conta.setCor(contaDTO.cor());
        conta.setMoeda(contaDTO.moeda());
        if (contaDTO.tipoConta() != null) {
            conta.setTipoConta(contaDTO.tipoConta());
        }
        if (contaDTO.limiteTotal() != null) {
            conta.setLimiteTotal(contaDTO.limiteTotal());
            if (conta.getTipoConta() == com.npl.na_ponta_do_lapis.entity.enums.TipoConta.CARTAO_CREDITO) {
                java.math.BigDecimal gasto = conta.getSaldo() != null ? conta.getSaldo().abs() : java.math.BigDecimal.ZERO;
                conta.setLimiteDisponivel(contaDTO.limiteTotal().subtract(gasto));
            }
        }

        try {
            contaFinanceiraRepository.save(conta);
        }
        catch (DataIntegrityViolationException e){
            if (e.getCause() instanceof ConstraintViolationException) {
                throw new IllegalArgumentException("Já existe uma conta com este nome para este usuário.");
            }
            throw e;
        }

        return new ContaFinanceiraResponseDTO(conta);
    }

    @Transactional
    public ContaFinanceiraResponseDTO atualizarContaParcial(Long id, ContaFinanceiraPatchDTO contaPatchDTO){

        ContaFinanceira conta = contaFinanceiraRepository.findById(id)
            .orElseThrow( () -> new ContaIdNaoExisteException("Conta de ID: " + id + " não existe"));
        validarDonoOuAdmin(conta);

        contaPatchDTO.nome().ifPresent(conta::setNome);
        contaPatchDTO.saldo().ifPresent(conta::setSaldo);
        contaPatchDTO.cor().ifPresent(conta::setCor);
        contaPatchDTO.moeda().ifPresent(conta::setMoeda);

        try {
            contaFinanceiraRepository.save(conta);
        }
        catch (DataIntegrityViolationException e){
            if (e.getCause() instanceof ConstraintViolationException) {
                throw new IllegalArgumentException("Já existe uma conta com este nome para este usuário.");
            }
            throw e;
        }

        return new ContaFinanceiraResponseDTO(conta);
    }

    @Transactional
    public void excluirConta(Long id){
        ContaFinanceira conta = contaFinanceiraRepository.findById(id)
                .orElseThrow(() -> new ContaIdNaoExisteException("Conta de ID: " + id + " não existe"));
        validarDonoOuAdmin(conta);
        contaFinanceiraRepository.delete(conta);
    }
    public List<ContaFinanceira> listarContaFinanceiraUsuarioLogado(){
        String email = JwtAuthFilter.getEmailUsuarioLogado();
        return contaFinanceiraRepository.buscarContaFinanceiraUsuarioLogado(email);
    }

    public ContaFinanceiraResponseDTO buscarContaPorId(Long id){
        ContaFinanceira conta = contaFinanceiraRepository.findById(id)
                .orElseThrow(() -> new ContaIdNaoExisteException("Conta de ID: " + id + " não existe"));
        validarDonoOuAdmin(conta);
        return new ContaFinanceiraResponseDTO(conta);
    }

    private void validarDonoOuAdmin(ContaFinanceira conta) {
        String email = JwtAuthFilter.getEmailUsuarioLogado();
        if (email == null || email.isBlank()) {
            return;
        }
        Usuario logado = usuarioService.buscarUsuarioPorEmail(email);
        if (logado == null) {
            return;
        }
        boolean ehAdmin = logado.getPapel() == Papel.ADMIN_SITE;
        boolean ehDono = conta.getUsuario() != null && java.util.Objects.equals(conta.getUsuario().getId(), logado.getId());
        if (!ehAdmin && !ehDono) {
            throw new AccessDeniedException("Você não tem permissão para acessar ou alterar esta conta.");
        }
    }

    // Método para o Service transacao utilizar pois o outro retorna "ContaFinanceiraResponseDTO" (Não deve ser exposto ao controller)
    public ContaFinanceira buscarContaPorIdObject(Long id) {
        if (org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()
                && !org.springframework.transaction.support.TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            return contaFinanceiraRepository.findByIdForUpdate(id)
                    .orElseThrow(() -> new ContaIdNaoExisteException("Conta de ID: " + id + " não existe"));
        }
        return contaFinanceiraRepository.findById(id)
                .orElseThrow(() -> new ContaIdNaoExisteException("Conta de ID: " + id + " não existe"));
    }

    /**
     * Aplica ou estorna o impacto financeiro de uma transação na conta.
     * Para CARTAO_CREDITO, saldo representa a fatura em aberto (positivo):
     * DESPESA aumenta a fatura e reduz limiteDisponivel; RECEITA reduz a fatura e libera limiteDisponivel.
     */
    public static void aplicarMovimentacaoSaldo(
            ContaFinanceira conta,
            com.npl.na_ponta_do_lapis.entity.enums.TipoTransacao tipo,
            java.math.BigDecimal valor,
            boolean estorno
    ) {
        if (conta == null || valor == null || tipo == null) {
            return;
        }
        java.math.BigDecimal saldoAtual = conta.getSaldo() != null ? conta.getSaldo() : java.math.BigDecimal.ZERO;
        boolean ehCartao = conta.getTipoConta() == com.npl.na_ponta_do_lapis.entity.enums.TipoConta.CARTAO_CREDITO;
        boolean somarNoSaldo = ehCartao
                ? (tipo == com.npl.na_ponta_do_lapis.entity.enums.TipoTransacao.DESPESA) != estorno
                : (tipo == com.npl.na_ponta_do_lapis.entity.enums.TipoTransacao.RECEITA) != estorno;

        java.math.BigDecimal novoSaldo = somarNoSaldo ? saldoAtual.add(valor) : saldoAtual.subtract(valor);
        conta.setSaldo(novoSaldo);

        if (ehCartao && conta.getLimiteTotal() != null) {
            java.math.BigDecimal faturaEmAberto = novoSaldo.compareTo(java.math.BigDecimal.ZERO) > 0
                    ? novoSaldo
                    : java.math.BigDecimal.ZERO;
            conta.setLimiteDisponivel(conta.getLimiteTotal().subtract(faturaEmAberto));
        }
    }

}
