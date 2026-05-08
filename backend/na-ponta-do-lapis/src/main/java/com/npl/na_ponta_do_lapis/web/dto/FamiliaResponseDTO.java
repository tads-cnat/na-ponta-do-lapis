package com.npl.na_ponta_do_lapis.web.dto;

import com.npl.na_ponta_do_lapis.entity.Familia;
import com.npl.na_ponta_do_lapis.entity.Usuario;

import java.util.List;

public record FamiliaResponseDTO(Long id, String nome, String foto, List<UsuarioResponseDTO> membros) {

    public FamiliaResponseDTO(Familia familia) {
        this(
                familia.getId(),
                familia.getNome(),
                familia.getFotoFamilia(),
                familia.getMembros() == null
                        ? List.of()
                        : familia.getMembros().stream().map(UsuarioResponseDTO::new).toList()
        );
    }
}
