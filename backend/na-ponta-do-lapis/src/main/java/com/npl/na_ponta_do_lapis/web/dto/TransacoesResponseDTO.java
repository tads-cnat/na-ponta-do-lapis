package com.npl.na_ponta_do_lapis.web.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
<<<<<<< HEAD
<<<<<<< HEAD
import com.npl.na_ponta_do_lapis.entity.Transacao;
import com.npl.na_ponta_do_lapis.entity.enums.EstadoTransacao;
=======
>>>>>>> e4f88b5 (refactor:corrigindo POST criaTransacoes e implementando regra de negorcio ao criar transacao)
=======
import com.npl.na_ponta_do_lapis.entity.Transacao;
import com.npl.na_ponta_do_lapis.entity.enums.EstadoTransacao;
>>>>>>> f2c067a (refactor(Transacoes ): corrigindo endpoint transacao, add regra de negocio no atualizar Transacao e melhorando resposta dos endpoits transacoes)
import com.npl.na_ponta_do_lapis.entity.enums.TipoTransacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
import java.util.List;
=======
>>>>>>> 23bb351 (fix: retirando imports desnecesários)

public record TransacoesResponseDTO(
        Long id,
        String descricao,
        BigDecimal valor,
        TipoTransacao tipo,
        EstadoTransacao estado,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime dataHora,
        CategoriaResumoDTO categoria,
        ContaResumoDTO conta
) {
       public TransacoesResponseDTO(Transacao transacao){
               this(
                       transacao.getId(),
                       transacao.getDescricao(),
                       transacao.getValor(),
                       transacao.getTipo(),
                       transacao.getEstado(),
                       transacao.getDataHora(),
                       new CategoriaResumoDTO(
                               transacao.getCategoria().getId(),
                               transacao.getCategoria().getNome()
                       ),
                       new ContaResumoDTO(
                               transacao.getContaFinanceira().getId(),
                               transacao.getContaFinanceira().getNome()
                       )
               );
       };
=======
=======
import java.util.List;
>>>>>>> f2c067a (refactor(Transacoes ): corrigindo endpoint transacao, add regra de negocio no atualizar Transacao e melhorando resposta dos endpoits transacoes)

public record TransacoesResponseDTO(
        Long id,
        String descricao,
        BigDecimal valor,
        TipoTransacao tipo,
        EstadoTransacao estado,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime dataHora,
        CategoriaResumoDTO categoria,
        ContaResumoDTO conta
) {
<<<<<<< HEAD
>>>>>>> e4f88b5 (refactor:corrigindo POST criaTransacoes e implementando regra de negorcio ao criar transacao)
=======
       public TransacoesResponseDTO(Transacao transacao){
               this(
                       transacao.getId(),
                       transacao.getDescricao(),
                       transacao.getValor(),
                       transacao.getTipo(),
                       transacao.getEstado(),
                       transacao.getDataHora(),
                       new CategoriaResumoDTO(
                               transacao.getCategoria().getId(),
                               transacao.getCategoria().getNome()
                       ),
                       new ContaResumoDTO(
                               transacao.getContaFinanceira().getId(),
                               transacao.getContaFinanceira().getNome()
                       )
               );
       };
>>>>>>> f2c067a (refactor(Transacoes ): corrigindo endpoint transacao, add regra de negocio no atualizar Transacao e melhorando resposta dos endpoits transacoes)
}

