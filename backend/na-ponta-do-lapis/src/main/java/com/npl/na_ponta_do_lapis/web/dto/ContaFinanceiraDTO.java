package com.npl.na_ponta_do_lapis.web.dto;

import java.math.BigDecimal;

import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.enums.TipoConta;
import com.npl.na_ponta_do_lapis.entity.Usuario;

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ContaFinanceiraDTO(
    
    @NotBlank(message = "O nome da conta não pode ser em branco")
    String nome,
    
    @NotNull(message = "O saldo é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "O saldo não pode ser negativo")
    BigDecimal saldo,
    
    @NotNull(message = "O tipo de conta é obrigatório")
    TipoConta tipo,
    
    @NotNull(message = "O usuário é obrigatório")
    Usuario usuario) {

    public ContaFinanceira toEntity(){
        ContaFinanceira conta = new ContaFinanceira();
        conta.setNome(this.nome);
        conta.setSaldo(this.saldo);
        conta.setTipo(this.tipo);
=======
=======
>>>>>>> ccf3bb4 (chore: aplica regras do gitigore)
=======
>>>>>>> ccf3bb4 (chore: aplica regras do gitigore)
public record ContaFinanceiraDTO(String nome, BigDecimal saldo, TipoConta tipo, Usuario usuario) {

    public ContaFinanceira toEntity(){
        ContaFinanceira conta = new ContaFinanceira();
        conta.setNome(nome);
        conta.setSaldo(saldo);
        conta.setTipo(tipo);
<<<<<<< HEAD
<<<<<<< HEAD
>>>>>>> ccf3bb4 (chore: aplica regras do gitigore)
=======
>>>>>>> ccf3bb4 (chore: aplica regras do gitigore)
=======
>>>>>>> ccf3bb4 (chore: aplica regras do gitigore)
        conta.setUsuario(usuario);
        return conta;
    }
}
