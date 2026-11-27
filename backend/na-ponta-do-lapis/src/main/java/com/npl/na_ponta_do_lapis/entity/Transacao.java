package com.npl.na_ponta_do_lapis.entity;

import com.npl.na_ponta_do_lapis.entity.enums.EstadoTransacao;
import com.npl.na_ponta_do_lapis.entity.enums.TipoTransacao;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "transacao")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 254)
    private String descricao;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @ManyToOne // Relacionamento com a nova entidade TipoCategoria
    @JoinColumn(name = "id_categoria", nullable = false)
    private TipoCategoria categoria;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EstadoTransacao estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TipoTransacao tipo; // RECEITA ou DESPESA

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @ManyToOne
    @JoinColumn(name = "id_conta_financeira", nullable = false)
    private ContaFinanceira contaFinanceira;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marcador_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Marcador marcador;

    @Column(name = "grupo_parcelamento", length = 36)
    private String grupoParcelamento;

    @Column(name = "numero_parcela")
    private Integer numeroParcela;

    @Column(name = "total_parcelas")
    private Integer totalParcelas;

    @Column(name = "valor_total_compra", precision = 19, scale = 2)
    private BigDecimal valorTotalCompra;

    @Column(name = "impacta_saldo")
    private Boolean impactaSaldo = true;

    @Column(name = "pluggy_transaction_id", length = 100)
    private String pluggyTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "origem", length = 20)
    private com.npl.na_ponta_do_lapis.entity.enums.OrigemTransacao origem = com.npl.na_ponta_do_lapis.entity.enums.OrigemTransacao.MANUAL;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        if (this.criadoEm == null) {
            this.criadoEm = LocalDateTime.now();
        }
        if (this.origem == null) {
            this.origem = (this.pluggyTransactionId != null && !this.pluggyTransactionId.isBlank())
                    ? com.npl.na_ponta_do_lapis.entity.enums.OrigemTransacao.OPEN_FINANCE
                    : com.npl.na_ponta_do_lapis.entity.enums.OrigemTransacao.MANUAL;
        }
    }

    public boolean isImpactaSaldo() {
        return !Boolean.FALSE.equals(this.impactaSaldo);
    }

    public void setImpactaSaldo(Boolean impactaSaldo) {
        this.impactaSaldo = impactaSaldo;
    }

    public com.npl.na_ponta_do_lapis.entity.enums.OrigemTransacao getOrigem() {
        return origem != null ? origem : com.npl.na_ponta_do_lapis.entity.enums.OrigemTransacao.MANUAL;
    }

    public void setOrigem(com.npl.na_ponta_do_lapis.entity.enums.OrigemTransacao origem) {
        this.origem = origem;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }


    public Marcador getMarcador() {
        return marcador;
    }

    public void setMarcador(Marcador marcador) {
        this.marcador = marcador;
    }

    public Transacao() {
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

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

    public String getGrupoParcelamento() { return grupoParcelamento; }
    public void setGrupoParcelamento(String grupoParcelamento) { this.grupoParcelamento = grupoParcelamento; }

    public Integer getNumeroParcela() { return numeroParcela; }
    public void setNumeroParcela(Integer numeroParcela) { this.numeroParcela = numeroParcela; }

    public Integer getTotalParcelas() { return totalParcelas; }
    public void setTotalParcelas(Integer totalParcelas) { this.totalParcelas = totalParcelas; }

    public BigDecimal getValorTotalCompra() { return valorTotalCompra; }
    public void setValorTotalCompra(BigDecimal valorTotalCompra) { this.valorTotalCompra = valorTotalCompra; }

    public String getPluggyTransactionId() { return pluggyTransactionId; }
    public void setPluggyTransactionId(String pluggyTransactionId) { this.pluggyTransactionId = pluggyTransactionId; }
}
