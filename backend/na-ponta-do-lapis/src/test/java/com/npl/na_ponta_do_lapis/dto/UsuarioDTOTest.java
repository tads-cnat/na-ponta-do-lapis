package com.npl.na_ponta_do_lapis.dto;

import com.npl.na_ponta_do_lapis.entity.Usuario;
import com.npl.na_ponta_do_lapis.entity.enums.Papel;
import com.npl.na_ponta_do_lapis.web.dto.UsuarioDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioDTOTest {

	private Validator validator;

	@BeforeEach
	void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void usuarioValidoNaoDeveGerarViolacoes() {
		UsuarioDTO dto = new UsuarioDTO("Bruno Natanael", "bruno@email.com", "brunonatanael", "senha123");

		assertTrue(validator.validate(dto).isEmpty());
	}

	@Test
	void nomeNuloDeveFalharValidacao() {
		UsuarioDTO dto = new UsuarioDTO(null, "bruno@email.com", "brunonatanael", "senha123");
		Set<ConstraintViolation<UsuarioDTO>> violacoes = validator.validate(dto);

		assertTrue(violacoes.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nome")));
	}

	@Test
	void nomeVazioDeveFalharValidacao() {
		UsuarioDTO dto = new UsuarioDTO("  ", "bruno@email.com", "brunonatanael", "senha123");
		Set<ConstraintViolation<UsuarioDTO>> violacoes = validator.validate(dto);

		assertTrue(violacoes.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nome")));
	}

	@Test
	void emailComFormatoInvalidoDeveFalharValidacao() {
		UsuarioDTO dto = new UsuarioDTO("Bruno Natanael", "email_invalido", "brunonatanael", "senha123");
		Set<ConstraintViolation<UsuarioDTO>> violacoes = validator.validate(dto);

		assertTrue(violacoes.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
	}

	@Test
	void senhaNulaDeveFalharValidacao() {
		UsuarioDTO dto = new UsuarioDTO("Bruno Natanael", "bruno@email.com", "brunonatanael", null);
		Set<ConstraintViolation<UsuarioDTO>> violacoes = validator.validate(dto);

		assertTrue(violacoes.stream().anyMatch(v -> v.getPropertyPath().toString().equals("senha")));
	}

	@Test
	void senhaComMenosDeSeisCaracteresDeveFalharValidacao() {
		UsuarioDTO dto = new UsuarioDTO("Bruno Natanael", "bruno@email.com", "brunonatanael", "12345");
		Set<ConstraintViolation<UsuarioDTO>> violacoes = validator.validate(dto);

		assertTrue(violacoes.stream().anyMatch(v -> v.getPropertyPath().toString().equals("senha")));
	}

	@Test
	void toEntityDeveCopiarDadosEDefinirPapelPadrao() {
		UsuarioDTO dto = new UsuarioDTO("Bruno Natanael", "bruno@email.com", "brunonatanael", "senha123");

		Usuario usuario = dto.toEntity();

		assertAll(
				() -> assertEquals("Bruno Natanael", usuario.getNome()),
				() -> assertEquals("bruno@email.com", usuario.getEmail()),
				() -> assertEquals("brunonatanael", usuario.getUsername()),
				() -> assertNull(usuario.getFotoPerfil()),
				() -> assertEquals(Papel.USUARIO, usuario.getPapel())
		);
	}

}
