package com.ecommerce.praticboutic_backend_java;

public class Lien {
    private String nom;      // Nom de la colonne ou de l'association
    private String srctbl;   // Table source à laquelle la colonne ou la relation est associée

    // Constructeur
    public Lien(String nom, String srctbl) {
        this.nom = nom;
        this.srctbl = srctbl;
    }

    // Getter pour 'nom'
    public String getNom() {
        return nom;
    }

    // Setter pour 'nom'
    public void setNom(String nom) {
        this.nom = nom;
    }

    // Getter pour 'srctbl'
    public String getSrctbl() {
        return srctbl;
    }

    // Setter pour 'srctbl'
    public void setSrctbl(String srctbl) {
        this.srctbl = srctbl;
    }

    // Méthode toString (optionnelle, utile pour le debugging)
    @Override
    public String toString() {
        return "Lien{" +
                "nom='" + nom + '\'' +
                ", srctbl='" + srctbl + '\'' +
                '}';
    }
}