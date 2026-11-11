package com.npl.na_ponta_do_lapis.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import com.npl.na_ponta_do_lapis.entity.enums.TipoMeta;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;

import com.npl.na_ponta_do_lapis.entity.enums.TipoMeta;

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

@Entity
@Table(name = "meta")
public class Meta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "valor_atual", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorAtual = BigDecimal.ZERO;

    @Column(name = "foto_url")
    private String fotoUrl;

    @CreationTimestamp
    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_limite")
    private LocalDate dataLimite;

    @Column(name = "data_encerramento")
    private LocalDate dataEncerramento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_meta", nullable = false)
    private TipoMeta tipoMeta;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private com.npl.na_ponta_do_lapis.entity.enums.StatusMeta status = com.npl.na_ponta_do_lapis.entity.enums.StatusMeta.EM_ANDAMENTO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_financeira_id")
    private ContaFinanceira conta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private TipoCategoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "familia_id")
    private Familia familia;

    public Meta() {
    }

    public Meta(BigDecimal valor, String nome, TipoMeta tipoMeta, Usuario usuario) {
        this.valor = valor;
        this.nome = nome;
        this.tipoMeta = tipoMeta;
        this.usuario = usuario;
        this.valorAtual = BigDecimal.ZERO;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
    
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public String getNome() {
        return nome;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
    
    public BigDecimal getValor() {
        return valor;
    }

    public void setValorAtual(BigDecimal valorAtual) {
        this.valorAtual = valorAtual;
    }
    
    public BigDecimal getValorAtual() {
        return valorAtual;
    }
    
    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataLimite(LocalDate dataLimite) {
        this.dataLimite = dataLimite;
    }

    public LocalDate getDataLimite() {
        return dataLimite;
    }

    public void setDataEncerramento(LocalDate dataEncerramento) {
        this.dataEncerramento = dataEncerramento;
    }

    public LocalDate getDataEncerramento() {
        return dataEncerramento;
    }

    public void setTipoMeta(TipoMeta tipoMeta) {
        this.tipoMeta = tipoMeta;
    }

    public TipoMeta getTipoMeta() {
        return tipoMeta;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setConta(ContaFinanceira conta) {
        this.conta = conta;
    }
    
    public ContaFinanceira getConta() {
        return conta;
    }

    public com.npl.na_ponta_do_lapis.entity.enums.StatusMeta getStatus() {
        return status;
    }

    public void setStatus(com.npl.na_ponta_do_lapis.entity.enums.StatusMeta status) {
        this.status = status;
    }

    public TipoCategoria getCategoria() {
        return categoria;
    }

    public void setCategoria(TipoCategoria categoria) {
        this.categoria = categoria;
    }

    public Familia getFamilia() {
        return familia;
    }

    public void setFamilia(Familia familia) {
        this.familia = familia;
    }
}