package com.npl.na_ponta_do_lapis.entity;

import java.math.BigDecimal;
import java.util.Objects;

import com.npl.na_ponta_do_lapis.entity.enums.TipoConta;

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
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "conta_financeira")
public class ContaFinanceira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O nome é obrigatório")
    @Column(name = "nome", length = 100, nullable = false)
    private String nome;

    @NotNull(message = "O saldo é obrigatório")
    @Column(name = "saldo", precision = 19, scale = 2, nullable = false, columnDefinition = "DEFAULT 0.0")
    private BigDecimal saldo;

    @NotNull(message = "O tipo é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoConta tipo;

    @NotNull(message = "O usuário é obrigatório")
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ContaFinanceira conta_financeira = (ContaFinanceira) o;
        return Objects.equals(id, conta_financeira.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return this.nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getSaldo() {
        return this.saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public TipoConta getTipo() {
        return this.tipo;
    }

    public void setTipo(TipoConta tipo) {
        this.tipo = tipo;
    }

    public Usuario getUsuario() {
        return this.usuario;
    }

    public void setUsuario(Usuario usuario_id) {
        this.usuario = usuario_id;
    }

    @Override
    public String toString() {
        return "ContaFinanceira{" +
                "nome='" + this.nome + '\n' +
                ", saldo='" + this.saldo + '\n' +
                ", tipo='" + this.tipo + '\n' +
                ", usuario=" + this.usuario + '\n' +
                "}";
    }

}
