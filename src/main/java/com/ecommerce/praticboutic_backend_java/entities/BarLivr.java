package com.ecommerce.praticboutic_backend_java.entities;

import com.ecommerce.praticboutic_backend_java.models.BaseEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "barlivr")
public class BarLivr extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "barlivrid")
    private Integer barlivrId;

    @Column(name = "customid", nullable = false)
    private Integer customid;

    @Column(name = "valminin", nullable = false)
    private Float valMinIn;

    @Column(name = "valmaxex", nullable = false)
    private Float valMaxEx;

    @Column(name = "surcout", nullable = false, columnDefinition = "float DEFAULT 0")
    private Float surCout = 0.0f;

    @Column(name = "limitebasse", nullable = false, columnDefinition = "int unsigned DEFAULT 1")
    private Integer limiteBasse = 1;

    @Column(name = "limitehaute", nullable = false, columnDefinition = "int unsigned DEFAULT 1")
    private Integer limiteHaute = 1;

    @Column(name = "actif", nullable = false, columnDefinition = "int unsigned DEFAULT 1")
    private Integer actif = 1;

    // Getters et Setters
    public Integer getBarLivrId() {
        return barlivrId;
    }

    public void setBarLivrId(Integer barlivrId) {
        this.barlivrId = barlivrId;
    }

    public Integer getCustomId() {
        return customid;
    }

    public void setCustomId(Integer customid) {
        this.customid = customid;
    }

    public Float getValMinIn() {
        return valMinIn;
    }

    public void setValMinIn(Float valMinIn) {
        this.valMinIn = valMinIn;
    }

    public Float getValMaxEx() {
        return valMaxEx;
    }

    public void setValMaxEx(Float valMaxEx) {
        this.valMaxEx = valMaxEx;
    }

    public Float getSurCout() {
        return surCout;
    }

    public void setSurCout(Float surCout) {
        this.surCout = surCout;
    }

    public Integer getLimiteBasse() {
        return limiteBasse;
    }

    public void setLimiteBasse(Integer limiteBasse) {
        this.limiteBasse = limiteBasse;
    }

    public Integer getLimiteHaute() {
        return limiteHaute;
    }

    public void setLimiteHaute(Integer limiteHaute) {
        this.limiteHaute = limiteHaute;
    }

    public Integer getActif() {
        return actif;
    }

    public void setActif(Integer actif) {
        this.actif = actif;
    }

    public List<Object> getDisplayData()
    {
        List<Object> row = new ArrayList<>();
        row.add(getBarLivrId());
        row.add(getValMinIn());
        row.add(getValMaxEx());
        row.add(getSurCout());
        row.add(actif.toString());
        return row;
    }

}