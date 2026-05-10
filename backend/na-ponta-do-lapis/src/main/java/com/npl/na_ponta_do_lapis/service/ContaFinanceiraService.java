package com.npl.na_ponta_do_lapis.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.repository.ContaFinanceiraRepository;
import com.npl.na_ponta_do_lapis.security.jwt.JwtAuthFilter;
import com.npl.na_ponta_do_lapis.web.dto.ContaFinanceiraDTO;
import com.npl.na_ponta_do_lapis.web.dto.ContaFinanceiraPatchDTO;
import com.npl.na_ponta_do_lapis.web.dto.ContaFinanceiraResponseDTO;
import com.npl.na_ponta_do_lapis.web.exception.ContaIdNaoExisteException;

import jakarta.transaction.Transactional;


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
        List<ContaFinanceira> contas = listarContaFinanceiraUsuarioLogado();
        for (ContaFinanceira conta : contas){
            if (conta.getNome() == contaDTO.nome()) {
                throw new IllegalArgumentException("Não é possível criar contas com o mesmo nome.");
            }
        }
        ContaFinanceira novaConta = contaDTO.toEntity(usuario);
        contaFinanceiraRepository.save(novaConta);
        return new ContaFinanceiraResponseDTO(novaConta);
    }

    public List<ContaFinanceiraResponseDTO> listarContas() {
        return contaFinanceiraRepository.findAll().stream()
        .map(contaFinanceira -> new ContaFinanceiraResponseDTO(contaFinanceira))
        .toList();
    }

    public List<ContaFinanceira> listarContaFinanceiraUsuarioLogado(){
        String email = JwtAuthFilter.getEmailUsuarioLogado();
        return contaFinanceiraRepository.buscarContaFinanceiraUsuarioLogado(email);
    }

    public ContaFinanceiraResponseDTO buscarContaPorId(Long id){
        return contaFinanceiraRepository.findById(id)
        .map(contaFinanceira -> new ContaFinanceiraResponseDTO(contaFinanceira))
        .orElseThrow(
        () -> new ContaIdNaoExisteException("Conta de ID: " + id + " não existe")
        );
    }

    // Método para o Service transacao utilizar pois o outro retorna "ContaFinanceiraResponseDTO" (Não deve ser exposto ao controller)
    public ContaFinanceira buscarContaPorIdObject(Long id) {
        return contaFinanceiraRepository.findById(id)
                .orElseThrow(() -> new ContaIdNaoExisteException("Conta de ID: " + id + " não existe"));
    }

    @Transactional
    public ContaFinanceiraResponseDTO atualizarConta(Long id, ContaFinanceiraDTO contaDTO){

        ContaFinanceira conta = contaFinanceiraRepository.findById(id)
            .orElseThrow(() ->
                new ContaIdNaoExisteException("Conta de ID: " + id + " não existe")
            );

        conta.setNome(contaDTO.nome());
        conta.setSaldo(contaDTO.saldo());
        conta.setCor(contaDTO.cor());

        contaFinanceiraRepository.save(conta);

        return new ContaFinanceiraResponseDTO(conta);
    }

    @Transactional
    public ContaFinanceiraResponseDTO atualizarContaParcial(
            Long id,
            ContaFinanceiraPatchDTO contaPatchDTO
    ){

        ContaFinanceira conta = contaFinanceiraRepository.findById(id)
            .orElseThrow(() ->
                new ContaIdNaoExisteException(
                    "Conta de ID: " + id + " não existe"
                )
            );

        contaPatchDTO.nome().ifPresent(conta::setNome);
        contaPatchDTO.saldo().ifPresent(conta::setSaldo);
        contaPatchDTO.cor().ifPresent(conta::setCor);

        contaFinanceiraRepository.save(conta);

        return new ContaFinanceiraResponseDTO(conta);
    }

    @Transactional
    public void excluirConta(Long id){
        if(!contaFinanceiraRepository.existsById(id)){
            throw new ContaIdNaoExisteException("Conta de ID: " + id + " não existe");
        }
        contaFinanceiraRepository.deleteById(id);
    }
}
