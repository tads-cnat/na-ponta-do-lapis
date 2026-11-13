package com.npl.na_ponta_do_lapis.security;

import com.npl.na_ponta_do_lapis.entity.Familia;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.Moeda;
import com.npl.na_ponta_do_lapis.entity.enums.Papel;

import java.io.Serializable;

/**
 * Representação serializável e imutável dos dados de autenticação do usuário para cache distribuído.
 * Evita problemas de serialização/proxies do Hibernate ao armazenar entidades JPA diretamente no Redis.
 */
public record UsuarioAuthCacheDTO(
        Long id,
        String nome,
        String email,
        String username,
        String senha,
        boolean senhaTemporaria,
        String telefone,
        String fotoPerfil,
        Papel papel,
        Moeda moedaPadrao,
        Long familiaId,
        String familiaNome,
        String familiaFoto
) implements Serializable {

    public static UsuarioAuthCacheDTO from(Usuario usuario) {
        Familia familia = usuario.getFamilia();
        return new UsuarioAuthCacheDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getUsername(),
                usuario.getPassword(),
                usuario.isSenhaTemporaria(),
                usuario.getTelefone(),
                usuario.getFotoPerfil(),
                usuario.getPapel(),
                usuario.getMoedaPadrao(),
                familia != null ? familia.getId() : null,
                familia != null ? familia.getNome() : null,
                familia != null ? familia.getFotoFamilia() : null
        );
    }

    public Usuario toUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setUsername(username);
        usuario.setSenha(senha);
        usuario.setSenhaTemporaria(senhaTemporaria);
        usuario.setTelefone(telefone);
        usuario.setFotoPerfil(fotoPerfil);
        usuario.setPapel(papel);
        usuario.setMoedaPadrao(moedaPadrao);

        if (familiaId != null) {
            Familia familia = new Familia();
            familia.setId(familiaId);
            familia.setNome(familiaNome);
            familia.setFotoFamilia(familiaFoto);
            usuario.setFamilia(familia);
        }

        return usuario;
    }
}
