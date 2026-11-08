package com.npl.na_ponta_do_lapis.service;

import com.npl.na_ponta_do_lapis.entity.ChaveIaUsuario;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.web.dto.ChaveIaRequestDTO;
import com.npl.na_ponta_do_lapis.web.dto.ChaveIaResponseDTO;

import java.util.List;
import java.util.Optional;

public interface ChaveIaService {

    List<ChaveIaResponseDTO> listarChaves(Usuario usuario);

    ChaveIaResponseDTO cadastrarChave(Usuario usuario, ChaveIaRequestDTO dto);

    void definirComoPadrao(Usuario usuario, Long chaveId);

    void deletarChave(Usuario usuario, Long chaveId);

    String obterChaveDecifrada(Usuario usuario, Long chaveId);

    ChaveIaUsuario obterEntidade(Usuario usuario, Long chaveId);

    Optional<ChaveIaUsuario> obterChavePadrao(Usuario usuario);
}
