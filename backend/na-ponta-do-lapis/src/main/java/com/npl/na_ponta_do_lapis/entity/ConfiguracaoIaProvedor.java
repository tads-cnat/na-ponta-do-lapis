package com.npl.na_ponta_do_lapis.entity;

import com.npl.na_ponta_do_lapis.service.fatura.ia.ProvedorIa;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "configuracao_ia_provedor")
public class ConfiguracaoIaProvedor {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "provedor", length = 30, nullable = false)
    private ProvedorIa provedor;

    @Column(name = "modelo_ativo", length = 120, nullable = false)
    private String modeloAtivo;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @Column(name = "atualizado_por", length = 254)
    private String atualizadoPor;

    @Column(name = "alerta_modelo_descontinuado", nullable = false)
    private boolean alertaModeloDescontinuado = false;

    @Column(name = "ultimo_erro_modelo", length = 500)
    private String ultimoErroModelo;

    @Column(name = "ultimo_erro_em")
    private LocalDateTime ultimoErroEm;

    @Column(name = "ultimo_alerta_notificado_em")
    private LocalDateTime ultimoAlertaNotificadoEm;

    public ConfiguracaoIaProvedor() {
    }

    public ConfiguracaoIaProvedor(ProvedorIa provedor, String modeloAtivo) {
        this.provedor = provedor;
        this.modeloAtivo = modeloAtivo;
    }

    public ProvedorIa getProvedor() {
        return provedor;
    }

    public void setProvedor(ProvedorIa provedor) {
        this.provedor = provedor;
    }

    public String getModeloAtivo() {
        return modeloAtivo;
    }

    public void setModeloAtivo(String modeloAtivo) {
        this.modeloAtivo = modeloAtivo;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    public String getAtualizadoPor() {
        return atualizadoPor;
    }

    public void setAtualizadoPor(String atualizadoPor) {
        this.atualizadoPor = atualizadoPor;
    }

    public boolean isAlertaModeloDescontinuado() {
        return alertaModeloDescontinuado;
    }

    public void setAlertaModeloDescontinuado(boolean alertaModeloDescontinuado) {
        this.alertaModeloDescontinuado = alertaModeloDescontinuado;
    }

    public String getUltimoErroModelo() {
        return ultimoErroModelo;
    }

    public void setUltimoErroModelo(String ultimoErroModelo) {
        this.ultimoErroModelo = ultimoErroModelo;
    }

    public LocalDateTime getUltimoErroEm() {
        return ultimoErroEm;
    }

    public void setUltimoErroEm(LocalDateTime ultimoErroEm) {
        this.ultimoErroEm = ultimoErroEm;
    }

    public LocalDateTime getUltimoAlertaNotificadoEm() {
        return ultimoAlertaNotificadoEm;
    }

    public void setUltimoAlertaNotificadoEm(LocalDateTime ultimoAlertaNotificadoEm) {
        this.ultimoAlertaNotificadoEm = ultimoAlertaNotificadoEm;
    }
}

