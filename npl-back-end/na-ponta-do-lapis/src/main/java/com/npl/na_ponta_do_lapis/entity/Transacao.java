package com.npl.na_ponta_do_lapis.entity;

import com.npl.na_ponta_do_lapis.entity.Enums.EstadoTransacao;
import com.npl.na_ponta_do_lapis.entity.TipoCategoria;
import com.npl.na_ponta_do_lapis.entity.Enums.TipoTransacao;
import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "transacao")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 254)
    private String descricao;

    @Column(nullable = false)
    private Double valor;

    @ManyToOne // Relacionamento com a nova entidade TipoCategoria
    @JoinColumn(name = "id_categoria", nullable = false)
    private TipoCategoria categoria;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EstadoTransacao estado = EstadoTransacao.REALIZADA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoTransacao tipo; // RECEITA ou DESPESA

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @ManyToOne
    @JoinColumn(name = "id_conta_financeira", nullable = false)
    private ContaFinanceira contaFinanceira;

    public Transacao() {
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }

    public TipoCategoria getCategoria() { return categoria; }
    public void setCategoria(TipoCategoria categoria) { this.categoria = categoria; }

    public EstadoTransacao getEstado() { return estado; }
    public void setEstado(EstadoTransacao estado) { this.estado = estado; }

    public TipoTransacao getTipo() { return tipo; }
    public void setTipo(TipoTransacao tipo) { this.tipo = tipo; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public ContaFinanceira getContaFinanceira() { return contaFinanceira; }
    public void setContaFinanceira(ContaFinanceira contaFinanceira) { this.contaFinanceira = contaFinanceira; }
}
