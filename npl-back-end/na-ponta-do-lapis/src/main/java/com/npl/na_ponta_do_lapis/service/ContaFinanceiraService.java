package com.npl.na_ponta_do_lapis.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.repository.ContaFinanceiraRepository;
import com.npl.na_ponta_do_lapis.web.Controller.Exception.ContaIdNaoExisteException;
import com.npl.na_ponta_do_lapis.web.Controller.dto.ContaFinanceiraDTO;
import com.npl.na_ponta_do_lapis.web.Controller.dto.ContaFinanceiraResponseDTO;

import jakarta.transaction.Transactional;


@Service
public class ContaFinanceiraService {

    private final ContaFinanceiraRepository contaFinanceiraRepository;

    public ContaFinanceiraService(ContaFinanceiraRepository contaFinanceiraRepository) {
        this.contaFinanceiraRepository = contaFinanceiraRepository;
    }

    @Transactional
    public ContaFinanceiraResponseDTO criarConta(ContaFinanceiraDTO contaDTO) {
        ContaFinanceira novaConta = contaDTO.toEntity();
        contaFinanceiraRepository.save(novaConta);
        return new ContaFinanceiraResponseDTO(novaConta);
    }

    public List<ContaFinanceiraResponseDTO> listarContas() {
        return contaFinanceiraRepository.findAll().stream()
        .map(contaFinanceira -> new ContaFinanceiraResponseDTO(contaFinanceira))
        .toList();
    }

    public ContaFinanceiraResponseDTO buscarContaPorId(Long id){
        return contaFinanceiraRepository.findById(id)
        .map(contaFinanceira -> new ContaFinanceiraResponseDTO(contaFinanceira))
        .orElseThrow(
        () -> new ContaIdNaoExisteException("Conta de ID: " + id + " não existe")
        );
    }

    @Transactional
    public ContaFinanceiraResponseDTO atualizarConta(Long id, ContaFinanceiraDTO contaDTO){
        ContaFinanceira conta = contaFinanceiraRepository.findById(id)
            .orElseThrow(() -> new ContaIdNaoExisteException("Conta de ID: " + id + " não existe"));
        
        conta.setNome(contaDTO.nome());
        conta.setSaldo(contaDTO.saldo());
        conta.setTipo(contaDTO.tipo());
        conta.setUsuario(contaDTO.usuario());
        
        contaFinanceiraRepository.save(conta);
        return new ContaFinanceiraResponseDTO(conta);
    }

    @Transactional
    public void excluirConta(Long id){
        contaFinanceiraRepository.deleteById(id);
    }
}