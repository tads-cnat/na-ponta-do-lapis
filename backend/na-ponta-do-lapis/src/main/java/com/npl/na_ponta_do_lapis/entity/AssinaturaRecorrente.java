package com.npl.na_ponta_do_lapis.entity;

import com.npl.na_ponta_do_lapis.entity.enums.EstadoTransacao;
import com.npl.na_ponta_do_lapis.entity.enums.FrequenciaRecorrencia;
import com.npl.na_ponta_do_lapis.entity.enums.StatusRecorrencia;
import com.npl.na_ponta_do_lapis.entity.enums.TipoTransacao;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "assinatura_recorrente")
public class AssinaturaRecorrente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 254)
    private String descricao;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoTransacao tipo = TipoTransacao.DESPESA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FrequenciaRecorrencia frequencia = FrequenciaRecorrencia.MENSAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusRecorrencia status = StatusRecorrencia.ATIVA;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_geracao", nullable = false, length = 30)
    private EstadoTransacao estadoGeracao = EstadoTransacao.PENDENTE;

    @Column(name = "dia_cobranca", nullable = false)
    private Integer diaCobranca;

    @Column(name = "proxima_cobranca", nullable = false)
    private LocalDate proximaCobranca;

    @Column(name = "ultima_cobranca")
    private LocalDate ultimaCobranca;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_financeira_id", nullable = false)
    private ContaFinanceira contaFinanceira;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private TipoCategoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marcador_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Marcador marcador;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        if (this.criadoEm == null) {
            this.criadoEm = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = StatusRecorrencia.ATIVA;
        }
        if (this.estadoGeracao == null) {
            this.estadoGeracao = EstadoTransacao.PENDENTE;
        }
        if (this.tipo == null) {
            this.tipo = TipoTransacao.DESPESA;
        }
        if (this.diaCobranca == null && this.proximaCobranca != null) {
            this.diaCobranca = this.proximaCobranca.getDayOfMonth();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public TipoTransacao getTipo() { return tipo; }
    public void setTipo(TipoTransacao tipo) { this.tipo = tipo; }

    public FrequenciaRecorrencia getFrequencia() { return frequencia; }
    public void setFrequencia(FrequenciaRecorrencia frequencia) { this.frequencia = frequencia; }

    public StatusRecorrencia getStatus() { return status; }
    public void setStatus(StatusRecorrencia status) { this.status = status; }

    public EstadoTransacao getEstadoGeracao() { return estadoGeracao; }
    public void setEstadoGeracao(EstadoTransacao estadoGeracao) { this.estadoGeracao = estadoGeracao; }

    public Integer getDiaCobranca() { return diaCobranca; }
    public void setDiaCobranca(Integer diaCobranca) { this.diaCobranca = diaCobranca; }

    public LocalDate getProximaCobranca() { return proximaCobranca; }
    public void setProximaCobranca(LocalDate proximaCobranca) { this.proximaCobranca = proximaCobranca; }

    public LocalDate getUltimaCobranca() { return ultimaCobranca; }
    public void setUltimaCobranca(LocalDate ultimaCobranca) { this.ultimaCobranca = ultimaCobranca; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public ContaFinanceira getContaFinanceira() { return contaFinanceira; }
    public void setContaFinanceira(ContaFinanceira contaFinanceira) { this.contaFinanceira = contaFinanceira; }

    public TipoCategoria getCategoria() { return categoria; }
    public void setCategoria(TipoCategoria categoria) { this.categoria = categoria; }

    public Marcador getMarcador() { return marcador; }
    public void setMarcador(Marcador marcador) { this.marcador = marcador; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}
