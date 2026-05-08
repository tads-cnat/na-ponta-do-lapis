package com.npl.na_ponta_do_lapis.repository;

import com.npl.na_ponta_do_lapis.entity.Convite;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.StatusConvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConviteRepository extends JpaRepository<Convite,Long> {
    List<Convite> findByDestinatarioAndStatus(Usuario user, StatusConvite status);
}
