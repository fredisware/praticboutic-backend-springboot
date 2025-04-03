package com.ecommerce.praticboutic_backend_java.entities;

import com.ecommerce.praticboutic_backend_java.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "cpzone")
public class CpZone extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cpzoneid")
    private Integer cpzoneId;

    @Column(name = "customid", nullable = false)
    private Integer customId;

    @Column(name = "codepostal", nullable = false, length = 5)
    private String codePostal;

    @Column(name = "ville", nullable = false, length = 45)
    private String ville;

    @Column(name = "actif", nullable = false, columnDefinition = "int DEFAULT 1")
    private Integer actif = 1;

    // Getters et Setters (sans les accesseurs pour id qui sont dans BaseEntity)
    public Integer getCustomId() {
        return customId;
    }

    public void setCustomId(Integer customId) {
        this.customId = customId;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public Integer getActif() {
        return actif;
    }

    public void setActif(Integer actif) {
        this.actif = actif;
    }
}