package com.npl.na_ponta_do_lapis.entity;

import com.npl.na_ponta_do_lapis.service.fatura.ia.ProvedorIa;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "execucao_ia_log", indexes = {
        @Index(name = "idx_execucao_ia_criado_em", columnList = "criado_em"),
        @Index(name = "idx_execucao_ia_tipo_provedor", columnList = "tipo,provedor")
})
public class ExecucaoIaLog {

    public enum TipoExecucao {
        FATURA,
        GRAFITH
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 20, nullable = false)
    private TipoExecucao tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "provedor", length = 30, nullable = false)
    private ProvedorIa provedor;

    @Column(name = "modelo", length = 120)
    private String modelo;

    @Column(name = "sucesso", nullable = false)
    private boolean sucesso;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "codigo_erro", length = 80)
    private String codigoErro;

    @Column(name = "mensagem_erro", length = 400)
    private String mensagemErro;

    @Column(name = "duracao_ms")
    private Long duracaoMs;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    public ExecucaoIaLog() {
    }

    public ExecucaoIaLog(TipoExecucao tipo,
                         ProvedorIa provedor,
                         String modelo,
                         boolean sucesso,
                         Integer httpStatus,
                         String codigoErro,
                         String mensagemErro,
                         Long duracaoMs) {
        this.tipo = tipo;
        this.provedor = provedor;
        this.modelo = modelo;
        this.sucesso = sucesso;
        this.httpStatus = httpStatus;
        this.codigoErro = codigoErro;
        this.mensagemErro = mensagemErro;
        this.duracaoMs = duracaoMs;
    }

    public Long getId() {
        return id;
    }

    public TipoExecucao getTipo() {
        return tipo;
    }

    public ProvedorIa getProvedor() {
        return provedor;
    }

    public String getModelo() {
        return modelo;
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public String getCodigoErro() {
        return codigoErro;
    }

    public String getMensagemErro() {
        return mensagemErro;
    }

    public Long getDuracaoMs() {
        return duracaoMs;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}

