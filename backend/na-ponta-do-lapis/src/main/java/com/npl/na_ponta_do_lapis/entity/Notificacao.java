package com.npl.na_ponta_do_lapis.entity;

import com.npl.na_ponta_do_lapis.entity.enums.TipoNotificacao;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacao")
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(length = 120, nullable = false)
    private String titulo;

    @Column(length = 500, nullable = false)
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 50, nullable = false)
    private TipoNotificacao tipo = TipoNotificacao.SISTEMA;

    @Column(nullable = false)
    private boolean lida = false;

    @Column(name = "link_redirecionamento", length = 200)
    private String linkRedirecionamento;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    public Notificacao() {
    }

    public Notificacao(Usuario usuario, String titulo, String mensagem, TipoNotificacao tipo, String linkRedirecionamento) {
        this.usuario = usuario;
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.tipo = tipo;
        this.linkRedirecionamento = linkRedirecionamento;
        this.lida = false;
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

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public TipoNotificacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoNotificacao tipo) {
        this.tipo = tipo;
    }

    public boolean isLida() {
        return lida;
    }

    public void setLida(boolean lida) {
        this.lida = lida;
    }

    public String getLinkRedirecionamento() {
        return linkRedirecionamento;
    }

    public void setLinkRedirecionamento(String linkRedirecionamento) {
        this.linkRedirecionamento = linkRedirecionamento;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}

