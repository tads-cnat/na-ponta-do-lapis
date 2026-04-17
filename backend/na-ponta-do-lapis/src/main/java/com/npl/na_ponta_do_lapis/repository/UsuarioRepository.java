package com.npl.na_ponta_do_lapis.repository;


import com.npl.na_ponta_do_lapis.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
