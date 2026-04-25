package com.npl.na_ponta_do_lapis.entity;

import com.fasterxml.jackson.databind.ser.std.StdKeySerializers;
import com.npl.na_ponta_do_lapis.entity.enums.StatusConvite;
import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
public class Convite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Usuario destinatario;

    @ManyToOne(optional = false)
    private Familia familia;

    @Enumerated(EnumType.STRING)
    private StatusConvite status;

    @CreationTimestamp
    private LocalDateTime dataCriacao;

    public Usuario getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(Usuario destinatario) {
        this.destinatario = destinatario;
    }

    public Familia getFamilia() {
        return familia;
    }

    public void setFamilia(Familia familia) {
        this.familia = familia;
    }

    public StatusConvite getStatus() {
        return status;
    }

    public void setStatus(StatusConvite status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
