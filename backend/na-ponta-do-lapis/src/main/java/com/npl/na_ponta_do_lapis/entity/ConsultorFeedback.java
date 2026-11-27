package com.npl.na_ponta_do_lapis.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultor_feedback")
public class ConsultorFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private boolean util;

    @Column(length = 40)
    private String provedor;

    @Column(name = "pergunta_resumo", length = 250)
    private String perguntaResumo;

    @Column(name = "resposta_resumo", length = 400)
    private String respostaResumo;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        if (this.criadoEm == null) {
            this.criadoEm = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public boolean isUtil() { return util; }
    public void setUtil(boolean util) { this.util = util; }

    public String getProvedor() { return provedor; }
    public void setProvedor(String provedor) { this.provedor = provedor; }

    public String getPerguntaResumo() { return perguntaResumo; }
    public void setPerguntaResumo(String perguntaResumo) { this.perguntaResumo = perguntaResumo; }

    public String getRespostaResumo() { return respostaResumo; }
    public void setRespostaResumo(String respostaResumo) { this.respostaResumo = respostaResumo; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}
