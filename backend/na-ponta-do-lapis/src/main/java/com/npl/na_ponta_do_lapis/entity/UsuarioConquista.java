package com.npl.na_ponta_do_lapis.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_conquista", uniqueConstraints = {
    @UniqueConstraint(name = "uk_usuario_conquista", columnNames = {"usuario_id", "conquista_id"})
})
public class UsuarioConquista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "conquista_id", nullable = false)
    private Conquista conquista;

    @Column(name = "data_desbloqueio", nullable = false)
    private LocalDateTime dataDesbloqueio;

    public UsuarioConquista() {
    }

    public UsuarioConquista(Usuario usuario, Conquista conquista, LocalDateTime dataDesbloqueio) {
        this.usuario = usuario;
        this.conquista = conquista;
        this.dataDesbloqueio = dataDesbloqueio;
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

    public Conquista getConquista() {
        return conquista;
    }

    public void setConquista(Conquista conquista) {
        this.conquista = conquista;
    }

    public LocalDateTime getDataDesbloqueio() {
        return dataDesbloqueio;
    }

    public void setDataDesbloqueio(LocalDateTime dataDesbloqueio) {
        this.dataDesbloqueio = dataDesbloqueio;
    }
}

