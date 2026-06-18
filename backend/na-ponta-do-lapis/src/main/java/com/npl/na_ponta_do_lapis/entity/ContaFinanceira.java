package com.npl.na_ponta_do_lapis.entity;

import java.math.BigDecimal;
import java.util.Objects;

import com.npl.na_ponta_do_lapis.entity.enums.Moeda;

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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "conta_financeira", uniqueConstraints = @UniqueConstraint(
    name = "nome_conta_unico",
    columnNames = {"nome", "usuario_id"}
))
public class ContaFinanceira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O nome é obrigatório")
    @NotBlank(message = "O nome não pode ser vazio")
    @Column(name = "nome", length = 100, nullable = false)
    @Size(min = 3)
    private String nome;

    @NotNull(message = "O saldo é obrigatório")
    @Column(name = "saldo", precision = 19, scale = 2, nullable = false)
    private BigDecimal saldo;

    @NotNull(message = "A cor é obrigatória")
    @Column(name = "cor", length = 7, nullable = false)
    @NotBlank(message = "A cor não pode ser vazia")
    @Size(max = 7)
    private String cor;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "A moeda é obrigatória")
    @Column(name = "moeda", length = 3, nullable = false)
    @NotBlank(message = "A moeda não pode ser vazia")
    @Size(max = 3)
    private Moeda moeda;

    @NotNull(message = "O usuário é obrigatório")
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
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

    public String getCor() {
        return this.cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public void setMoeda(Moeda moeda){
        this.moeda = moeda;
    }

    public Moeda getMoeda(){
        return this.moeda;
    }

    public Usuario getUsuario() {
        return this.usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public String toString() {
        return "ContaFinanceira{" +
                "nome='" + this.nome + '\n' +
                ", saldo='" + this.saldo + '\n' +
                ", cor='" + this.cor + '\n' +
                ", moeda='" + this.moeda + '\n' +
                ", usuario= " + this.usuario.getId() + " - " + this.usuario.getNome() + '\n' +
                "}";
    }

}
