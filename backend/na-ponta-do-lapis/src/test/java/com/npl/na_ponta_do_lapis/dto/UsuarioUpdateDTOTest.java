package com.npl.na_ponta_do_lapis.dto;

import com.npl.na_ponta_do_lapis.web.dto.UsuarioUpdateDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioUpdateDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void camposOpcionaisNulosNaoDevemGerarViolacoes() {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO(null, null, null);

        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void dadosValidosNaoDevemGerarViolacoes() {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO("Bruno Natanael", "bruno@email.com", "senha123");

        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void emailComFormatoInvalidoDeveFalharValidacao() {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO(null, "email_invalido", null);
        Set<ConstraintViolation<UsuarioUpdateDTO>> violacoes = validator.validate(dto);

        assertTrue(violacoes.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void senhaComMenosDeSeisCaracteresDeveFalharValidacao() {
        UsuarioUpdateDTO dto = new UsuarioUpdateDTO(null, null, "12345");
        Set<ConstraintViolation<UsuarioUpdateDTO>> violacoes = validator.validate(dto);

        assertTrue(violacoes.stream().anyMatch(v -> v.getPropertyPath().toString().equals("senha")));
    }
}