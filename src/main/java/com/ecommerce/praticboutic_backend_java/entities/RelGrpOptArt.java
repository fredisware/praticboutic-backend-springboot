package com.ecommerce.praticboutic_backend_java.entities;

import com.ecommerce.praticboutic_backend_java.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "relgrpoptart")
public class RelGrpOptArt extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "relgrpoartid")
    private Integer relgrpoartid;

    @Column(name = "customid", nullable = false)
    private Integer customid;

    @Column(name = "grpoptid", nullable = false, columnDefinition = "int DEFAULT 0")
    private Integer grpOptId = 0;

    @Column(name = "artid", nullable = false, columnDefinition = "int DEFAULT 0")
    private Integer artId = 0;

    @Column(name = "visible", nullable = false, columnDefinition = "int DEFAULT 1")
    private Integer visible = 1;

    // Relations (commentées, à activer si nécessaire)
    /*
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grpoptid", insertable = false, updatable = false)
    private GroupeOpt groupeOpt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artid", insertable = false, updatable = false)
    private Article article;
    */

    // Getters et Setters (sans les accesseurs pour id qui sont dans BaseEntity)
    public Integer getCustomId() {
        return customid;
    }

    public void setCustomId(Integer customid) {
        this.customid = customid;
    }

    public Integer getGrpOptId() {
        return grpOptId;
    }

    public void setGrpOptId(Integer grpOptId) {
        this.grpOptId = grpOptId;
    }

    public Integer getArtId() {
        return artId;
    }

    public void setArtId(Integer artId) {
        this.artId = artId;
    }

    public Integer getVisible() {
        return visible;
    }

    public void setVisible(Integer visible) {
        this.visible = visible;
    }

    /**
     * Méthode utilitaire pour vérifier si cette relation est visible
     * @return true si la relation est visible, false sinon
     */
    public boolean isVisible() {
        return visible != null && visible == 1;
    }
}