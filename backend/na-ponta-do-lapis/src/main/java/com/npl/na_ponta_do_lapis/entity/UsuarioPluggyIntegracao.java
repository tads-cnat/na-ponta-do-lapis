package com.npl.na_ponta_do_lapis.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuario_pluggy_integracao")
public class UsuarioPluggyIntegracao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "client_id_criptografado", columnDefinition = "TEXT", nullable = false)
    @NotBlank
    private String clientIdCriptografado;

    @Column(name = "client_secret_criptografado", columnDefinition = "TEXT", nullable = false)
    @NotBlank
    private String clientSecretCriptografado;

    @Column(name = "ultima_sincronizacao")
    private LocalDateTime ultimaSincronizacao;

    @Column(name = "sincronizacoes_manuais_hoje", nullable = false)
    private int sincronizacoesManuaisHoje = 0;

    @Column(name = "data_contador_manual")
    private LocalDate dataContadorManual;

    @Column(name = "item_ids_json", columnDefinition = "TEXT")
    private String itemIdsJson;

    public UsuarioPluggyIntegracao() {
    }

    public UsuarioPluggyIntegracao(Usuario usuario, String clientIdCriptografado, String clientSecretCriptografado) {
        this.usuario = usuario;
        this.clientIdCriptografado = clientIdCriptografado;
        this.clientSecretCriptografado = clientSecretCriptografado;
        this.dataContadorManual = LocalDate.now();
        this.sincronizacoesManuaisHoje = 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getClientIdCriptografado() {
        return clientIdCriptografado;
    }

    public void setClientIdCriptografado(String clientIdCriptografado) {
        this.clientIdCriptografado = clientIdCriptografado;
    }

    public String getClientSecretCriptografado() {
        return clientSecretCriptografado;
    }

    public void setClientSecretCriptografado(String clientSecretCriptografado) {
        this.clientSecretCriptografado = clientSecretCriptografado;
    }

    public LocalDateTime getUltimaSincronizacao() {
        return ultimaSincronizacao;
    }

    public void setUltimaSincronizacao(LocalDateTime ultimaSincronizacao) {
        this.ultimaSincronizacao = ultimaSincronizacao;
    }

    public int getSincronizacoesManuaisHoje() {
        return sincronizacoesManuaisHoje;
    }

    public void setSincronizacoesManuaisHoje(int sincronizacoesManuaisHoje) {
        this.sincronizacoesManuaisHoje = sincronizacoesManuaisHoje;
    }

    public LocalDate getDataContadorManual() {
        return dataContadorManual;
    }

    public void setDataContadorManual(LocalDate dataContadorManual) {
        this.dataContadorManual = dataContadorManual;
    }

    public String getItemIdsJson() {
        return itemIdsJson;
    }

    public void setItemIdsJson(String itemIdsJson) {
        this.itemIdsJson = itemIdsJson;
    }

    public Set<String> getItemIds() {
        if (itemIdsJson == null || itemIdsJson.isBlank()) {
            return new HashSet<>();
        }
        try {
            return new ObjectMapper().readValue(itemIdsJson, new TypeReference<Set<String>>() {});
        } catch (Exception e) {
            return new HashSet<>();
        }
    }

    public void setItemIds(Set<String> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            this.itemIdsJson = "[]";
            return;
        }
        try {
            this.itemIdsJson = new ObjectMapper().writeValueAsString(itemIds);
        } catch (Exception e) {
            this.itemIdsJson = "[]";
        }
    }

    public void adicionarItemId(String itemId) {
        if (itemId == null || itemId.isBlank()) return;
        Set<String> ids = getItemIds();
        ids.add(itemId.trim());
        setItemIds(ids);
    }

    public void removerItemId(String itemId) {
        if (itemId == null || itemId.isBlank()) return;
        Set<String> ids = getItemIds();
        ids.remove(itemId.trim());
        setItemIds(ids);
    }
}

