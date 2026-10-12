package com.npl.na_ponta_do_lapis.dto;

import com.npl.na_ponta_do_lapis.web.dto.UsuarioResumoDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UsuarioResumoDTOTest {

	@Test
	void deveExporIdENomeDoUsuario() {
		UsuarioResumoDTO dto = new UsuarioResumoDTO(7L, "Bruno Natanael");

		assertAll(
				() -> assertEquals(7L, dto.id()),
				() -> assertEquals("Bruno Natanael", dto.nome())
		);
	}

}
