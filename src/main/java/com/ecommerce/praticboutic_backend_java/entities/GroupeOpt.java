package com.ecommerce.praticboutic_backend_java.entities;

import com.ecommerce.praticboutic_backend_java.models.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "groupeopt")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class GroupeOpt extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grpoptid")
    private Integer grpoptid;

    @Column(name = "customid", nullable = false)
    private Integer customid;

    @Column(name = "nom", unique = true, nullable = false, length = 150)
    private String nom;

    @Column(name = "visible", nullable = false, columnDefinition = "int DEFAULT 1")
    private Boolean visible = true;

    @Column(name = "multiple", nullable = false, columnDefinition = "int DEFAULT 0")
    private Boolean multiple = false;

    /**
     * Retourne l'identifiant du groupe d'options
     *
     * @return L'identifiant du groupe d'options
     */
    public Integer getGrpoptid() {
        return grpoptid;
    }

    /**
     * Définit l'identifiant du groupe d'options
     *
     * @param grpoptid L'identifiant du groupe d'options à définir
     */
    public void setGrpoptid(Integer grpoptid) {
        this.grpoptid = grpoptid;
    }

    // Getters et Setters (sans les accesseurs pour id qui sont dans BaseEntity)
    public Integer getCustomId() {
        return customid;
    }

    public void setCustomId(Integer customid) {
        this.customid = customid;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Boolean getMultiple() {
        return multiple;
    }

    public void setMultiple(Boolean multiple) {
        this.multiple = multiple;
    }

}