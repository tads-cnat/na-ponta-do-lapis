package com.npl.na_ponta_do_lapis.repository;

import com.npl.na_ponta_do_lapis.entity.ExecucaoIaLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ExecucaoIaLogRepository extends JpaRepository<ExecucaoIaLog, Long> {
    List<ExecucaoIaLog> findByCriadoEmAfter(LocalDateTime desde);
}

