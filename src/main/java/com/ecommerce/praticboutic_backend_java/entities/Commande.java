package com.ecommerce.praticboutic_backend_java.entities;

import com.ecommerce.praticboutic_backend_java.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "commande", uniqueConstraints = @UniqueConstraint(name = "numref_UNIQUE", columnNames = {"customid", "numref"}))
public class Commande extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cmdid")
    private Integer cmdId;

    @Column(name = "customid", nullable = false)
    private Integer customId;

    @Column(name = "numref", nullable = false, length = 60)
    private String numRef;

    @Column(name = "nom", nullable = false, length = 60)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 60)
    private String prenom;

    @Column(name = "telephone", nullable = false, length = 60)
    private String telephone;

    @Column(name = "adresse1", nullable = false, length = 150)
    private String adresse1;

    @Column(name = "adresse2", nullable = false, length = 150)
    private String adresse2;

    @Column(name = "codepostal", nullable = false, length = 5)
    private String codePostal;

    @Column(name = "ville", nullable = false, length = 50)
    private String ville;

    @Column(name = "vente", nullable = false, length = 45)
    private String vente;

    @Column(name = "paiement", nullable = false, length = 45)
    private String paiement;

    @Column(name = "sstotal", nullable = false, columnDefinition = "double DEFAULT 0")
    private Double ssTotal = 0.0;

    @Column(name = "remise", nullable = false, columnDefinition = "double DEFAULT 0")
    private Double remise = 0.0;

    @Column(name = "fraislivraison", nullable = false, columnDefinition = "double DEFAULT 0")
    private Double fraisLivraison = 0.0;

    @Column(name = "total", nullable = false, columnDefinition = "double DEFAULT 0")
    private Double total = 0.0;

    @Column(name = "commentaire", nullable = false, length = 300)
    private String commentaire;

    @Column(name = "method", nullable = false, length = 45)
    private String method;

    @Column(name = "table", nullable = false)
    private Integer table;

    @Column(name = "datecreation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "statid", nullable = false)
    private Integer statId;

    // Relation optionnelle avec CommandeDetail si vous souhaitez la définir
    @OneToMany(mappedBy = "commande", fetch = FetchType.LAZY)
    private List<CommandeDetail> commandeDetails = new ArrayList<>();

    // Getters et Setters (sans les accesseurs pour id qui sont dans BaseEntity)

    public Integer getCustomId() {
        return customId;
    }

    public void setCustomId(Integer customId) {
        this.customId = customId;
    }

    public String getNumRef() {
        return numRef;
    }

    public void setNumRef(String numRef) {
        this.numRef = numRef;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse1() {
        return adresse1;
    }

    public void setAdresse1(String adresse1) {
        this.adresse1 = adresse1;
    }

    public String getAdresse2() {
        return adresse2;
    }

    public void setAdresse2(String adresse2) {
        this.adresse2 = adresse2;
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

    public String getVente() {
        return vente;
    }

    public void setVente(String vente) {
        this.vente = vente;
    }

    public String getPaiement() {
        return paiement;
    }

    public void setPaiement(String paiement) {
        this.paiement = paiement;
    }

    public Double getSsTotal() {
        return ssTotal;
    }

    public void setSsTotal(Double ssTotal) {
        this.ssTotal = ssTotal;
    }

    public Double getRemise() {
        return remise;
    }

    public void setRemise(Double remise) {
        this.remise = remise;
    }

    public Double getFraisLivraison() {
        return fraisLivraison;
    }

    public void setFraisLivraison(Double fraisLivraison) {
        this.fraisLivraison = fraisLivraison;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Integer getTable() {
        return table;
    }

    public void setTable(Integer table) {
        this.table = table;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Integer getStatId() {
        return statId;
    }

    public void setStatId(Integer statId) {
        this.statId = statId;
    }

    public List<CommandeDetail> getCommandeDetails() {
        return commandeDetails;
    }

    public void setCommandeDetails(List<CommandeDetail> commandeDetails) {
        this.commandeDetails = commandeDetails;
    }

    // Méthode pratique pour ajouter un détail de commande
    public void addCommandeDetail(CommandeDetail commandeDetail) {
        commandeDetails.add(commandeDetail);
        commandeDetail.setCommande(this);
    }

    // Méthode pratique pour supprimer un détail de commande
    public void removeCommandeDetail(CommandeDetail commandeDetail) {
        commandeDetails.remove(commandeDetail);
        commandeDetail.setCommande(null);
    }
}