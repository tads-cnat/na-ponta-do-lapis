package com.npl.na_ponta_do_lapis.repository;

import com.npl.na_ponta_do_lapis.entity.ConsultorFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultorFeedbackRepository extends JpaRepository<ConsultorFeedback, Long> {
    long countByUtilTrue();
    long countByUtilFalse();
}
