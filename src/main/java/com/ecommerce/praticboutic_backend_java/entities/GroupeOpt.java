package com.ecommerce.praticboutic_backend_java.entities;

import com.ecommerce.praticboutic_backend_java.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "groupeopt")
public class GroupeOpt extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grpoptid")
    private Integer grpoptid;

    @Column(name = "artid")
    private Integer artid;

    @Column(name = "customid", nullable = false)
    private Integer customId;

    @Column(name = "nom", nullable = false, length = 150)
    private String nom;

    @Column(name = "visible", nullable = false, columnDefinition = "int DEFAULT 1")
    private Integer visible = 1;

    @Column(name = "multiple", nullable = false, columnDefinition = "int DEFAULT 0")
    private Integer multiple = 0;

    @Column(name = "obligatoire")
    private boolean obligatoire;

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

    public Integer getVisible() {
        return visible;
    }

    public void setVisible(Integer visible) {
        this.visible = visible;
    }

    public Integer getMultiple() {
        return multiple;
    }

    public void setMultiple(Integer multiple) {
        this.multiple = multiple;
    }

    /**
     * Méthode utilitaire pour vérifier si ce groupe d'options permet des sélections multiples
     * @return true si des sélections multiples sont autorisées, false sinon
     */
    public boolean isMultipleSelectionAllowed() {
        return multiple != null && multiple == 1;
    }

    /**
     * Méthode utilitaire pour vérifier si ce groupe d'options est visible
     * @return true si le groupe est visible, false sinon
     */
    public boolean isVisible() {
        return visible != null && visible == 1;
    }



}