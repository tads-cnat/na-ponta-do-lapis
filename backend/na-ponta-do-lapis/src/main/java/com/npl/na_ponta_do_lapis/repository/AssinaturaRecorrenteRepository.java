package com.npl.na_ponta_do_lapis.repository;

import com.npl.na_ponta_do_lapis.entity.AssinaturaRecorrente;
import com.npl.na_ponta_do_lapis.entity.enums.StatusRecorrencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AssinaturaRecorrenteRepository extends JpaRepository<AssinaturaRecorrente, Long> {

    @Query("""
        SELECT a FROM AssinaturaRecorrente a
        JOIN FETCH a.contaFinanceira
        JOIN FETCH a.categoria
        LEFT JOIN FETCH a.marcador
        WHERE a.usuario.email = :email
        ORDER BY a.status ASC, a.proximaCobranca ASC
    """)
    List<AssinaturaRecorrente> buscarPorUsuarioEmail(@Param("email") String email);

    @Query("""
        SELECT a FROM AssinaturaRecorrente a
        JOIN FETCH a.usuario
        JOIN FETCH a.contaFinanceira
        JOIN FETCH a.categoria
        LEFT JOIN FETCH a.marcador
        WHERE a.status = :status
        AND a.proximaCobranca <= :dataLimite
    """)
    List<AssinaturaRecorrente> buscarVencidas(@Param("status") StatusRecorrencia status,
                                              @Param("dataLimite") LocalDate dataLimite);
}
