package com.npl.na_ponta_do_lapis.entity;

import com.npl.na_ponta_do_lapis.entity.enums.CategoriaConquista;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "conquista")
public class Conquista {

    @Id
    @Column(length = 50, nullable = false)
    private String id;

    @Column(nullable = false, length = 100)
    private String titulo;

    @Column(nullable = false, length = 255)
    private String descricao;

    @Column(nullable = false, length = 100)
    private String icone;

    @Column(name = "pontos_xp", nullable = false)
    private Integer pontosXp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CategoriaConquista categoria;

    public Conquista() {
    }

    public Conquista(String id, String titulo, String descricao, String icone, Integer pontosXp, CategoriaConquista categoria) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.icone = icone;
        this.pontosXp = pontosXp;
        this.categoria = categoria;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getIcone() {
        return icone;
    }

    public void setIcone(String icone) {
        this.icone = icone;
    }

    public Integer getPontosXp() {
        return pontosXp;
    }

    public void setPontosXp(Integer pontosXp) {
        this.pontosXp = pontosXp;
    }

    public CategoriaConquista getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaConquista categoria) {
        this.categoria = categoria;
    }
}

