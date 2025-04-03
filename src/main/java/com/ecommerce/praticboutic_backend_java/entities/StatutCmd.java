package com.ecommerce.praticboutic_backend_java.entities;

import com.ecommerce.praticboutic_backend_java.BaseEntity;
import jakarta.persistence.*;

/**
 * Entité StatutCmd
 */
@Entity
@Table(name = "statutcmd")
public class StatutCmd extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "statid")
    private Integer statId;

    private Integer customid;
    private String etat;
    private String couleur;
    private String message;
    private Boolean defaut;
    private Boolean actif;

    public StatutCmd() {}

    public StatutCmd(Integer customid, String etat, String couleur, String message, Boolean defaut, Boolean actif) {
        this.customid = customid;
        this.etat = etat;
        this.couleur = couleur;
        this.message = message;
        this.defaut = defaut;
        this.actif = actif;
    }

    public Integer getCustomid() {
        return customid;
    }

    public void setCustomid(Integer customid) {
        this.customid = customid;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public String getCouleur() {
        return couleur;
    }

    public void setCouleur(String couleur) {
        this.couleur = couleur;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isDefaut() {
        return defaut;
    }

    public void setDefaut(boolean defaut) {
        this.defaut = defaut;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

}