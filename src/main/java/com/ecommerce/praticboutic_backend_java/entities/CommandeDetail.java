package com.ecommerce.praticboutic_backend_java.entities;

import com.ecommerce.praticboutic_backend_java.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "commandedetail")
public class CommandeDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cmddetid")
    private Integer cmddetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cmdid", nullable = false)
    private Commande commande;

    @Column(name = "customid", nullable = false)
    private Integer customId;

    @Column(name = "artid", nullable = false)
    private Integer articleId;
    
    @Column(name = "quantite", nullable = false)
    private Integer quantite;

    @Column(name = "prix_unitaire", nullable = false)
    private Double prixUnitaire;

    @Column(name = "total", nullable = false)
    private Double total;

    @Column(name = "options", length = 300)
    private String options;

    // Getters et Setters
    public Commande getCommande() {
        return commande;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
    }

    public Integer getCustomId() {
        return customId;
    }

    public void setCustomId(Integer customId) {
        this.customId = customId;
    }

    public Integer getArticleId() {
        return articleId;
    }

    public void setArticleId(Integer articleId) {
        this.articleId = articleId;
    }

    public Integer getQuantite() {
        return quantite;
    }

    public void setQuantite(Integer quantite) {
        this.quantite = quantite;
    }

    public Double getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(Double prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }
    
    /**
     * Méthode pour calculer le total en fonction de la quantité et du prix unitaire
     */
    public void calculerTotal() {
        if (this.quantite != null && this.prixUnitaire != null) {
            this.total = this.quantite * this.prixUnitaire;
        }
    }
}