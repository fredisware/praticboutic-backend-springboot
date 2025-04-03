package com.ecommerce.praticboutic_backend_java.entities;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 * Entité représentant une option dans l'application
 */
@Entity
@Table(name = "`option`") // Le mot "option" étant un mot réservé en SQL, on utilise des backticks
public class Option implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "optid")
    private Integer id;

    @Column(name = "customid", nullable = false)
    private Integer customId;

    @Column(name = "nom", nullable = false, length = 150)
    private String nom;

    @Column(name = "surcout", nullable = false)
    private Double surcout = 0.0;

    @Column(name = "grpoptid", nullable = false)
    private Integer grpoptid;

    @Column(name = "visible", nullable = false)
    private Integer visible = 1;

    // Constructeurs
    public Option() {
    }

    public Option(Integer customId, String nom) {
        this.customId = customId;
        this.nom = nom;
    }

    public Option(Integer customId, String nom, Double surcout, Integer groupeOptionId) {
        this.customId = customId;
        this.nom = nom;
        this.surcout = surcout;
        this.grpoptid = grpoptid;
    }

    // Getters et setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCustomId() {
        return customId;
    }

    public void setCustomId(Integer customId) {
        this.customId = customId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Double getSurcout() {
        return surcout;
    }

    public void setSurcout() {
        this.surcout = surcout;
    }

    public Integer getGroupeOptionId() {
        return grpoptid;
    }

    public void setGroupeOptionId(Integer groupeOptionId) {
        this.grpoptid = grpoptid;
    }

    public Integer getVisible() {
        return visible;
    }

    public void setVisible(Integer visible) {
        this.visible = visible;
    }

    /**
     * Vérifie si l'option est visible
     * @return true si l'option est visible, false sinon
     */
    @Transient
    public boolean isVisible() {
        return visible == 1;
    }

    /**
     * Définit la visibilité de l'option
     * @param visible true pour rendre l'option visible, false pour la masquer
     */
    public void setVisibility(boolean visible) {
        this.visible = visible ? 1 : 0;
    }

    @Override
    public String toString() {
        return "Option{" +
                "id=" + id +
                ", customId=" + customId +
                ", nom='" + nom + '\'' +
                ", surcout=" + surcout +
                ", groupeOptionId=" + grpoptid +
                ", visible=" + visible +
                '}';
    }
}