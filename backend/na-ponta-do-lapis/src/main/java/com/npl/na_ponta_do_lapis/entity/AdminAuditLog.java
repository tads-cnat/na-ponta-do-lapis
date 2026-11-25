package com.npl.na_ponta_do_lapis.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_audit_log", indexes = {
        @Index(name = "idx_admin_audit_criado_em", columnList = "criado_em"),
        @Index(name = "idx_admin_audit_acao", columnList = "acao")
})
public class AdminAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_usuario_id", nullable = false)
    private Long adminUsuarioId;

    @Column(name = "admin_email", nullable = false, length = 254)
    private String adminEmail;

    @Column(name = "acao", nullable = false, length = 60)
    private String acao;

    @Column(name = "entidade_alvo", nullable = false, length = 60)
    private String entidadeAlvo;

    @Column(name = "alvo_id", length = 100)
    private String alvoId;

    @Column(name = "alvo_descricao", length = 255)
    private String alvoDescricao;

    @Column(name = "motivo", length = 500)
    private String motivo;

    @Column(name = "detalhes_json", columnDefinition = "TEXT")
    private String detalhesJson;

    @Column(name = "ip_origem", length = 64)
    private String ipOrigem;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    public AdminAuditLog() {
    }

    public AdminAuditLog(
            Long adminUsuarioId,
            String adminEmail,
            String acao,
            String entidadeAlvo,
            String alvoId,
            String alvoDescricao,
            String motivo,
            String detalhesJson,
            String ipOrigem
    ) {
        this.adminUsuarioId = adminUsuarioId;
        this.adminEmail = adminEmail;
        this.acao = acao;
        this.entidadeAlvo = entidadeAlvo;
        this.alvoId = alvoId;
        this.alvoDescricao = alvoDescricao;
        this.motivo = motivo;
        this.detalhesJson = detalhesJson;
        this.ipOrigem = ipOrigem;
    }

    public Long getId() {
        return id;
    }

    public Long getAdminUsuarioId() {
        return adminUsuarioId;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public String getAcao() {
        return acao;
    }

    public String getEntidadeAlvo() {
        return entidadeAlvo;
    }

    public String getAlvoId() {
        return alvoId;
    }

    public String getAlvoDescricao() {
        return alvoDescricao;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getDetalhesJson() {
        return detalhesJson;
    }

    public String getIpOrigem() {
        return ipOrigem;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
