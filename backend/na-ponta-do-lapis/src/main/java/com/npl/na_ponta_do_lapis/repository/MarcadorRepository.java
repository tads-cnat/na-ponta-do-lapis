package com.npl.na_ponta_do_lapis.repository;

import com.npl.na_ponta_do_lapis.entity.Marcador;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarcadorRepository extends JpaRepository<Marcador, Long> {
    List<Marcador> findByUsuario(Usuario usuario);
}