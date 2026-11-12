package com.npl.na_ponta_do_lapis.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "aporte_meta")
public class AporteMeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meta_id", nullable = false)
    private Meta meta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_origem_id")
    private ContaFinanceira contaOrigem;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(length = 255)
    private String observacao;

    @CreationTimestamp
    @Column(name = "data_aporte", nullable = false, updatable = false)
    private LocalDateTime dataAporte;

    public AporteMeta() {
    }

    public AporteMeta(Meta meta, Usuario usuario, ContaFinanceira contaOrigem, BigDecimal valor, String observacao) {
        this.meta = meta;
        this.usuario = usuario;
        this.contaOrigem = contaOrigem;
        this.valor = valor;
        this.observacao = observacao;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public ContaFinanceira getContaOrigem() {
        return contaOrigem;
    }

    public void setContaOrigem(ContaFinanceira contaOrigem) {
        this.contaOrigem = contaOrigem;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public LocalDateTime getDataAporte() {
        return dataAporte;
    }

    public void setDataAporte(LocalDateTime dataAporte) {
        this.dataAporte = dataAporte;
    }
}

