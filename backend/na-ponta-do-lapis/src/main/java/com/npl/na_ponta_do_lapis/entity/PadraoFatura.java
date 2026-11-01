package com.npl.na_ponta_do_lapis.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * Padrão de leitura de fatura por emissor.
 *
 * O regex de transação deve usar os grupos nomeados:
 *   (?<descricao>...) (?<data>...) (?<valor>...)
 *
 * O regex de total deve capturar o valor no grupo 1.
 */
@Entity
@Table(name = "padrao_fatura")
public class PadraoFatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "emissor", nullable = false, length = 100)
    private String emissor;

    @Column(name = "regex_identificacao", nullable = false, length = 500)
    private String regexIdentificacao;

    @Column(name = "regex_transacao", nullable = false, length = 1000)
    private String regexTransacao;

    @Column(name = "regex_total", length = 500)
    private String regexTotal;

    @Column(name = "formato_data", nullable = false, length = 30)
    private String formatoData;

    @Column(name = "versao", nullable = false)
    private Integer versao = 1;

    @Column(name = "origem", nullable = false, length = 20)
    private String origem = "SEED";

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @PrePersist
    void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
        if (versao == null) {
            versao = 1;
        }
        if (ativo == null) {
            ativo = true;
        }
        if (origem == null) {
            origem = "SEED";
        }
    }

    @PreUpdate
    void preUpdate() {
        atualizadoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmissor() {
        return emissor;
    }

    public void setEmissor(String emissor) {
        this.emissor = emissor;
    }

    public String getRegexIdentificacao() {
        return regexIdentificacao;
    }

    public void setRegexIdentificacao(String regexIdentificacao) {
        this.regexIdentificacao = regexIdentificacao;
    }

    public String getRegexTransacao() {
        return regexTransacao;
    }

    public void setRegexTransacao(String regexTransacao) {
        this.regexTransacao = regexTransacao;
    }

    public String getRegexTotal() {
        return regexTotal;
    }

    public void setRegexTotal(String regexTotal) {
        this.regexTotal = regexTotal;
    }

    public String getFormatoData() {
        return formatoData;
    }

    public void setFormatoData(String formatoData) {
        this.formatoData = formatoData;
    }

    public Integer getVersao() {
        return versao;
    }

    public void setVersao(Integer versao) {
        this.versao = versao;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}
