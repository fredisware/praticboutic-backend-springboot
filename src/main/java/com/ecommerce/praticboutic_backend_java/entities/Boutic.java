package com.ecommerce.praticboutic_backend_java.entities;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 * Entité représentant une boutique dans l'application
 */
@Entity
@Table(name = "boutic")
public class Boutic implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean ouvert;

    @Column(nullable = false)
    private boolean livraison;

    @Column(nullable = false)
    private boolean emporter;

    @Column(nullable = false)
    private boolean surplace;

    @Column(name = "delai_livraison")
    private Integer delaiLivraison;

    @Column(name = "delai_emporter")
    private Integer delaiEmporter;

    @Column(name = "code_postal")
    private String codePostal;

    @Column
    private String adresse;

    @Column
    private String ville;

    @Column
    private String telephone;

    @Column
    private String email;

    // Constructeurs
    public Boutic() {
    }

    public Boutic(String nom, String description, boolean ouvert) {
        this.nom = nom;
        this.description = description;
        this.ouvert = ouvert;
    }

    // Getters et setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isOuvert() {
        return ouvert;
    }

    public void setOuvert(boolean ouvert) {
        this.ouvert = ouvert;
    }

    public boolean isLivraison() {
        return livraison;
    }

    public void setLivraison(boolean livraison) {
        this.livraison = livraison;
    }

    public boolean isEmporter() {
        return emporter;
    }

    public void setEmporter(boolean emporter) {
        this.emporter = emporter;
    }

    public boolean isSurplace() {
        return surplace;
    }

    public void setSurplace(boolean surplace) {
        this.surplace = surplace;
    }

    public Integer getDelaiLivraison() {
        return delaiLivraison;
    }

    public void setDelaiLivraison(Integer delaiLivraison) {
        this.delaiLivraison = delaiLivraison;
    }

    public Integer getDelaiEmporter() {
        return delaiEmporter;
    }

    public void setDelaiEmporter(Integer delaiEmporter) {
        this.delaiEmporter = delaiEmporter;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Boutic{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", ouvert=" + ouvert +
                '}';
    }
}