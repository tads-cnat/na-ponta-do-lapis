package com.npl.na_ponta_do_lapis.entity;

import com.npl.na_ponta_do_lapis.service.fatura.ia.ProvedorIa;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chave_ia_usuario")
public class ChaveIaUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "apelido", length = 100, nullable = false)
    @NotBlank
    private String apelido;

    @Enumerated(EnumType.STRING)
    @Column(name = "provedor", length = 50, nullable = false)
    @NotNull
    private ProvedorIa provedor;

    @Column(name = "chave_criptografada", columnDefinition = "TEXT", nullable = false)
    @NotBlank
    private String chaveCriptografada;

    @Column(name = "padrao", nullable = false)
    private boolean padrao = false;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    public ChaveIaUsuario() {
    }

    public ChaveIaUsuario(Usuario usuario, String apelido, ProvedorIa provedor, String chaveCriptografada, boolean padrao) {
        this.usuario = usuario;
        this.apelido = apelido;
        this.provedor = provedor;
        this.chaveCriptografada = chaveCriptografada;
        this.padrao = padrao;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getApelido() {
        return apelido;
    }

    public void setApelido(String apelido) {
        this.apelido = apelido;
    }

    public ProvedorIa getProvedor() {
        return provedor;
    }

    public void setProvedor(ProvedorIa provedor) {
        this.provedor = provedor;
    }

    public String getChaveCriptografada() {
        return chaveCriptografada;
    }

    public void setChaveCriptografada(String chaveCriptografada) {
        this.chaveCriptografada = chaveCriptografada;
    }

    public boolean isPadrao() {
        return padrao;
    }

    public void setPadrao(boolean padrao) {
        this.padrao = padrao;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}

