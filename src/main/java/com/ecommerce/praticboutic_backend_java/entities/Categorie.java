package com.ecommerce.praticboutic_backend_java.entities;

import com.ecommerce.praticboutic_backend_java.BaseEntity;
import com.ecommerce.praticboutic_backend_java.DatabaseLink;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorie")
public class Categorie extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "catid")
    private Integer catid;

    @Column(name = "customid", nullable = false)
    private Integer customid;

    @Column(name = "nom", nullable = false, length = 150)
    private String nom;

    @Column(name = "visible", nullable = false, columnDefinition = "int DEFAULT 1")
    private Integer visible = 1;

    // Relation inverse avec Article - si vous souhaitez la maintenir
    @OneToMany(mappedBy = "categorie", fetch = FetchType.LAZY)
    private List<Article> articles;

    // Getters et Setters
    /**
     * Récupère l'identifiant de la catégorie
     * @return L'identifiant de la catégorie
     */
    public Integer getCatid() {
        return catid;
    }

    /**
     * Définit l'identifiant de la catégorie
     * @param catid L'identifiant de la catégorie à définir
     */
    public void setCatid(Integer catid) {
        this.catid = catid;
    }



    public Integer getCustomid() {
        return customid;
    }

    public void setCustomid(Integer customid) {
        this.customid = customid;
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

    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }

    /**
     * Méthode pour récupérer les relations (JOINS) associées à l'entité Categorie.
     * @return Liste d'objets `DatabaseLink` représentant les relations de l'entité Categorie.
     */
    public static List<DatabaseLink> getLinks() {
        List<DatabaseLink> links = new ArrayList<>();
        // Vous pouvez ajouter ici les relations avec d'autres tables si nécessaire
        return links;
    }

}