package com.npl.na_ponta_do_lapis.entity;

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

    @Column(name = "senha_temporaria", nullable = false, columnDefinition = "boolean default false")
    private boolean senhaTemporaria;

    @Column(name = "senha_temporaria_expira_em")
    private Instant senhaTemporariaExpiraEm;

    @Column(name = "pergunta_seguranca", length = 200)
    private String perguntaSeguranca;

    @Column(name = "resposta_seguranca_hash", length = 254)
    private String respostaSegurancaHash;

    @Column(name = "tentativas_recuperacao", nullable = false, columnDefinition = "integer default 0")
    private int tentativasRecuperacao;

    @Column(name = "recuperacao_bloqueada_ate")
    private Instant recuperacaoBloqueadaAte;

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
        return !senhaTemporaria || senhaTemporariaExpiraEm != null
            && senhaTemporariaExpiraEm.isAfter(Instant.now());
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

    public boolean isSenhaTemporaria() {
        return senhaTemporaria;
    }

    public void setSenhaTemporaria(boolean senhaTemporaria) {
        this.senhaTemporaria = senhaTemporaria;
    }

    public Instant getSenhaTemporariaExpiraEm() {
        return senhaTemporariaExpiraEm;
    }

    public void setSenhaTemporariaExpiraEm(Instant senhaTemporariaExpiraEm) {
        this.senhaTemporariaExpiraEm = senhaTemporariaExpiraEm;
    }

    public String getPerguntaSeguranca() {
        return perguntaSeguranca;
    }

    public void setPerguntaSeguranca(String perguntaSeguranca) {
        this.perguntaSeguranca = perguntaSeguranca;
    }

    @JsonIgnore
    public String getRespostaSegurancaHash() {
        return respostaSegurancaHash;
    }

    public void setRespostaSegurancaHash(String respostaSegurancaHash) {
        this.respostaSegurancaHash = respostaSegurancaHash;
    }

    public int getTentativasRecuperacao() {
        return tentativasRecuperacao;
    }

    public void setTentativasRecuperacao(int tentativasRecuperacao) {
        this.tentativasRecuperacao = tentativasRecuperacao;
    }

    public Instant getRecuperacaoBloqueadaAte() {
        return recuperacaoBloqueadaAte;
    }

    public void setRecuperacaoBloqueadaAte(Instant recuperacaoBloqueadaAte) {
        this.recuperacaoBloqueadaAte = recuperacaoBloqueadaAte;
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
