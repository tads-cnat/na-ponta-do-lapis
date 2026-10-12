package com.npl.na_ponta_do_lapis.dto;

import com.npl.na_ponta_do_lapis.entity.Familia;
import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.Papel;
import com.npl.na_ponta_do_lapis.web.dto.UsuarioResponseDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioResponseDTOTest {

	@Test
	void deveMapearDadosDoUsuarioSemFamilia() {
		Usuario usuario = criarUsuario();

		UsuarioResponseDTO dto = new UsuarioResponseDTO(usuario);

		assertAll(
				() -> assertEquals(1L, dto.id()),
				() -> assertEquals("Bruno Natanael", dto.nome()),
				() -> assertEquals("bruno@email.com", dto.email()),
				() -> assertEquals("brunonatanael", dto.username()),
				() -> assertEquals("foto.png", dto.fotoPerfil()),
				() -> assertEquals("USUARIO", dto.papel()),
				() -> assertNull(dto.idFamilia())
		);
	}

	@Test
	void deveMapearIdDaFamiliaDoUsuario() {
		Usuario usuario = criarUsuario();
		Familia familia = new Familia();
		familia.setId(5L);
		usuario.setFamilia(familia);

		UsuarioResponseDTO dto = new UsuarioResponseDTO(usuario);

		assertEquals(5L, dto.idFamilia());
	}

	private Usuario criarUsuario() {
		Usuario usuario = new Usuario();
		usuario.setId(1L);
		usuario.setNome("Bruno Natanael");
		usuario.setEmail("bruno@email.com");
		usuario.setUsername("brunonatanael");
		usuario.setFotoPerfil("foto.png");
		usuario.setPapel(Papel.USUARIO);
		return usuario;
	}
}