package com.npl.na_ponta_do_lapis.web.dto;

import java.math.BigDecimal;

import com.npl.na_ponta_do_lapis.entity.ContaFinanceira;
import com.npl.na_ponta_do_lapis.entity.enums.TipoConta;
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 76e0bd3 (corrigindo imports e pacotes)
=======
>>>>>>> cb70e71 (chore: aplica regras do gitigore)
=======
>>>>>>> 76e0bd3 (corrigindo imports e pacotes)
<<<<<<< HEAD:backend/na-ponta-do-lapis/src/main/java/com/npl/na_ponta_do_lapis/web/dto/ContaFinanceiraResponseDTO.java
<<<<<<< HEAD:backend/na-ponta-do-lapis/src/main/java/com/npl/na_ponta_do_lapis/web/dto/ContaFinanceiraResponseDTO.java
=======
=======
>>>>>>> 0e87127 (corrigindo imports e pacotes):npl-back-end/na-ponta-do-lapis/src/main/java/com/npl/na_ponta_do_lapis/web/dto/ContaFinanceiraResponseDTO.java
import com.npl.na_ponta_do_lapis.entity.Usuario;
>>>>>>> 0e87127 (corrigindo imports e pacotes):npl-back-end/na-ponta-do-lapis/src/main/java/com/npl/na_ponta_do_lapis/web/dto/ContaFinanceiraResponseDTO.java

public record ContaFinanceiraResponseDTO(Long id, String nome, BigDecimal saldo, TipoConta tipo, Long usuarioId) {

    public ContaFinanceiraResponseDTO(ContaFinanceira contaFinanceira){
        this(contaFinanceira.getId(), contaFinanceira.getNome(), contaFinanceira.getSaldo(), contaFinanceira.getTipo(), contaFinanceira.getUsuarioId());
=======
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> ccf3bb4 (chore: aplica regras do gitigore)
=======
>>>>>>> 76e0bd3 (corrigindo imports e pacotes)
=======
=======
>>>>>>> ccf3bb4 (chore: aplica regras do gitigore)
>>>>>>> cb70e71 (chore: aplica regras do gitigore)
=======
>>>>>>> ccf3bb4 (chore: aplica regras do gitigore)
=======
>>>>>>> 76e0bd3 (corrigindo imports e pacotes)
import com.npl.na_ponta_do_lapis.entity.Usuario;

public record ContaFinanceiraResponseDTO(Long id, String nome, BigDecimal saldo, TipoConta tipo, Usuario usuario) {

    public ContaFinanceiraResponseDTO(ContaFinanceira contaFinanceira){
        this(contaFinanceira.getId(), contaFinanceira.getNome(), contaFinanceira.getSaldo(), contaFinanceira.getTipo(), contaFinanceira.getUsuario());
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
>>>>>>> ccf3bb4 (chore: aplica regras do gitigore)
=======
=======
>>>>>>> 76e0bd3 (corrigindo imports e pacotes)
=======
>>>>>>> ccf3bb4 (chore: aplica regras do gitigore)
=======
>>>>>>> cb70e71 (chore: aplica regras do gitigore)
>>>>>>> ccf3bb4 (chore: aplica regras do gitigore)
=======
=======
>>>>>>> 76e0bd3 (corrigindo imports e pacotes)
>>>>>>> ccf3bb4 (chore: aplica regras do gitigore)
    }
}
