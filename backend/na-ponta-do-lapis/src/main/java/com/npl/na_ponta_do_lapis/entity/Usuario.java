package com.npl.na_ponta_do_lapis.entity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.time.Instant;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.npl.na_ponta_do_lapis.entity.enums.Moeda;
import com.npl.na_ponta_do_lapis.entity.enums.Papel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "usuario")
public class Usuario implements UserDetails {

    public static final String VERSAO_TERMOS_ATUAL = "2.0";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", length = 100, nullable = false)
    @NotBlank
    @NotNull
    @Size(min = 3)
    private String nome;

    @Column(name = "email", length = 254, nullable = false)
    @NotNull(message = "Nome não pode ser nulo")
    @NotBlank(message = "O Campo não pode estar vazio")
    @Email(message = "Cadastre um tipo e-mail válido")
    private String email;

    @Column(name =  "username", nullable = false, length = 100, unique = true)
    private String username;

    @Column(name = "senha", nullable = false, length = 254)
    private String senha;

    @Column(name = "recuperacao_token_hash", length = 64)
    private String recuperacaoTokenHash;

    @Column(name = "recuperacao_token_expira_em")
    private Instant recuperacaoTokenExpiraEm;

    @Column(name = "email_verificado")
    private Boolean emailVerificado;

    @Column(name = "email_verificacao_token_hash", length = 64)
    private String emailVerificacaoTokenHash;

    @Column(name = "email_verificacao_expira_em")
    private Instant emailVerificacaoExpiraEm;

    @Column(name = "telefone", nullable = true, length = 20)
    private String telefone;

    @Column(name = "foto_perfil")
    private String fotoPerfil;

    @ManyToOne
    @JoinColumn(name = "familia_id")
    private Familia familia;

    @Enumerated(EnumType.STRING)
    @Column(name = "papel")
    private Papel papel;

    @Enumerated(EnumType.STRING)
    @Column(name = "moeda_padrao", length = 3, nullable = false, columnDefinition = "varchar(3) default 'BRL'")
    private Moeda moedaPadrao = Moeda.BRL;

    @Column(name = "versao_termos", length = 20)
    private String versaoTermos;

    @Column(name = "termos_aceitos_em")
    private LocalDateTime termosAceitosEm;

    public String getVersaoTermos() {
        return versaoTermos;
    }

    public void setVersaoTermos(String versaoTermos) {
        this.versaoTermos = versaoTermos;
    }

    public LocalDateTime getTermosAceitosEm() {
        return termosAceitosEm;
    }

    public void setTermosAceitosEm(LocalDateTime termosAceitosEm) {
        this.termosAceitosEm = termosAceitosEm;
    }

    public Moeda getMoedaPadrao() {
        return moedaPadrao != null ? moedaPadrao : Moeda.BRL;
    }

    public void setMoedaPadrao(Moeda moedaPadrao) {
        this.moedaPadrao = moedaPadrao != null ? moedaPadrao : Moeda.BRL;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(papel);
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getRecuperacaoTokenHash() {
        return recuperacaoTokenHash;
    }

    public void setRecuperacaoTokenHash(String recuperacaoTokenHash) {
        this.recuperacaoTokenHash = recuperacaoTokenHash;
    }

    public Instant getRecuperacaoTokenExpiraEm() {
        return recuperacaoTokenExpiraEm;
    }

    public void setRecuperacaoTokenExpiraEm(Instant recuperacaoTokenExpiraEm) {
        this.recuperacaoTokenExpiraEm = recuperacaoTokenExpiraEm;
    }

    public Boolean getEmailVerificado() {
        return emailVerificado;
    }

    public void setEmailVerificado(Boolean emailVerificado) {
        this.emailVerificado = emailVerificado;
    }

    public boolean isEmailVerificado() {
        return !Boolean.FALSE.equals(emailVerificado);
    }

    public String getEmailVerificacaoTokenHash() {
        return emailVerificacaoTokenHash;
    }

    public void setEmailVerificacaoTokenHash(String emailVerificacaoTokenHash) {
        this.emailVerificacaoTokenHash = emailVerificacaoTokenHash;
    }

    public Instant getEmailVerificacaoExpiraEm() {
        return emailVerificacaoExpiraEm;
    }

    public void setEmailVerificacaoExpiraEm(Instant emailVerificacaoExpiraEm) {
        this.emailVerificacaoExpiraEm = emailVerificacaoExpiraEm;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public Papel getPapel() {
        return papel;
    }

    public void setPapel(Papel papel) {
        this.papel = papel;
    }

    public Familia getFamilia() {
        return familia;
    }

    public void setFamilia(Familia familia) {
        this.familia = familia;
    }
}
