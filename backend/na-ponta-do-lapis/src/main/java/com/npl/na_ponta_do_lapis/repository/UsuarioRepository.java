package com.npl.na_ponta_do_lapis.repository;


import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.Papel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByUsername(String name);
    List<Usuario> findByPapelIn(List<Papel> papels);

    Optional<Usuario> findByEmail(String mail);
}
