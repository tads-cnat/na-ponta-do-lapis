package com.npl.na_ponta_do_lapis.entity;

import com.npl.na_ponta_do_lapis.entity.enums.Papel;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    private Validator validator;
    private Usuario usuarioValido;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        usuarioValido = new Usuario();
        usuarioValido.setId(1L);
        usuarioValido.setNome("Bruno Natanael");
        usuarioValido.setEmail("bruno@email.com");
        usuarioValido.setUsername("brunonatanael");
        usuarioValido.setSenha("senhaSegura123");
        usuarioValido.setPapel(Papel.values()[0]);
    }

    @Test
    void usuarioValidoNaoDeveGerarViolacoes() {
        Set<ConstraintViolation<Usuario>> violacoes = validator.validate(usuarioValido);
        assertTrue(violacoes.isEmpty());
    }

    @Test
    void nomeComMenosDeTresLetrasDeveFalharValidacao() {
        usuarioValido.setNome("AB");
        Set<ConstraintViolation<Usuario>> violacoes = validator.validate(usuarioValido);
        assertTrue(violacoes.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nome")));
    }

    @Test
    void nomeNuloDeveFalharValidacao() {
        usuarioValido.setNome(null);
        Set<ConstraintViolation<Usuario>> violacoes = validator.validate(usuarioValido);
        assertTrue(violacoes.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nome")));
    }

    @Test
    void emailComFormatoInvalidoDeveFalharValidacao() {
        usuarioValido.setEmail("email_sem_arroba.com");
        Set<ConstraintViolation<Usuario>> violacoes = validator.validate(usuarioValido);
        assertTrue(violacoes.stream().anyMatch(v -> v.getMessage().equals("Cadastre um tipo e-mail válido")));
    }

    @Test
    void emailVazioDeveFalharValidacao() {
        usuarioValido.setEmail("");
        Set<ConstraintViolation<Usuario>> violacoes = validator.validate(usuarioValido);
        assertTrue(violacoes.stream().anyMatch(v -> v.getMessage().equals("O Campo não pode estar vazio")));
    }

    @Test
    void emailNuloDeveFalharValidacao() {
        usuarioValido.setEmail(null);
        Set<ConstraintViolation<Usuario>> violacoes = validator.validate(usuarioValido);
        assertTrue(violacoes.stream().anyMatch(v -> v.getMessage().equals("Nome não pode ser nulo")));
    }

    @Test
    void authorityListNaoDeveSerNula() {
        assertNotNull(usuarioValido.getAuthorities());
    }

    @Test
    void authorityListDeveConterExatamenteUmElemento() {
        assertEquals(1, usuarioValido.getAuthorities().size());
    }

    @Test
    void authorityListDeveConterOPapelDoUsuario() {
        assertTrue(usuarioValido.getAuthorities().contains(usuarioValido.getPapel()));
    }

    @Test
    void isAccountNonExpiredDeveRetornarTrue() {
        assertTrue(usuarioValido.isAccountNonExpired());
    }

    @Test
    void isAccountNonLockedDeveRetornarTrue() {
        assertTrue(usuarioValido.isAccountNonLocked());
    }

    @Test
    void isCredentialsNonExpiredDeveRetornarTrue() {
        assertTrue(usuarioValido.isCredentialsNonExpired());
    }

    @Test
    void isEnabledDeveRetornarTrue() {
        assertTrue(usuarioValido.isEnabled());
    }

    @Test
    void equalsDeveRetornarTrueParaUsuariosComMesmoId() {
        Usuario usuario1 = new Usuario();
        usuario1.setId(99L);
        Usuario usuario2 = new Usuario();
        usuario2.setId(99L);

        assertEquals(usuario1, usuario2);
    }

    @Test
    void equalsDeveRetornarFalseParaUsuariosComIdsDiferentes() {
        Usuario usuario1 = new Usuario();
        usuario1.setId(1L);
        Usuario usuario2 = new Usuario();
        usuario2.setId(2L);

        assertNotEquals(usuario1, usuario2);
    }

    @Test
    void equalsDeveRetornarFalseQuandoComparadoComNull() {
        Usuario usuario1 = new Usuario();
        usuario1.setId(1L);

        assertNotEquals(null, usuario1);
    }

    @Test
    void equalsDeveRetornarFalseQuandoComparadoComOutroTipoDeObjeto() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        String outroObjeto = "Não sou um usuário";

        assertNotEquals(usuario, outroObjeto);
    }

    @Test
    void hashCodeDeveSerIgualParaUsuariosComMesmoId() {
        Usuario usuario1 = new Usuario();
        usuario1.setId(99L);
        Usuario usuario2 = new Usuario();
        usuario2.setId(99L);

        assertEquals(usuario1.hashCode(), usuario2.hashCode());
    }
}