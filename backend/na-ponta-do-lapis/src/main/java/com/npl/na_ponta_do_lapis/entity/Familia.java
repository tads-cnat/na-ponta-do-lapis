package com.npl.na_ponta_do_lapis.entity;

import jakarta.persistence.*;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "familia")
public class Familia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", length = 100, nullable = false)
    private String nome;

//    @OneToMany(mappedBy = "familia", cascade = CascadeType.ALL)
//    private List<Usuario> membros;

    @Column(name = "fotoFamilia", columnDefinition = "TEXT", nullable = true)
    private String fotoFamilia;

//    public List<Usuario> getMembros() {
//        return membros;
//    }
//
//    public void setMembros(List<Usuario> membros) {
//        this.membros = membros;
//    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Familia familia = (Familia) o;
        return id.equals(familia.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFotoFamilia() {
        return fotoFamilia;
    }

    public void setFotoFamilia(String fotoFamilia) {
        this.fotoFamilia = fotoFamilia;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

}
