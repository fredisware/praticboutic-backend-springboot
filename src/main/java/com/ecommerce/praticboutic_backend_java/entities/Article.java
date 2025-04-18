package com.ecommerce.praticboutic_backend_java.entities;

import com.ecommerce.praticboutic_backend_java.models.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.SessionFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "article")
public class Article extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "artid")
    private Integer artId;

    @Column(name = "customid", nullable = false)
    private Integer customid;

    @Column(name = "nom", unique = true, nullable = false, length = 150)
    private String nom;

    @Column(name = "prix", nullable = false)
    private Double prix;

    @Column(name = "description", length = 350)
    private String description;

    @Column(name = "visible", nullable = false, columnDefinition = "int DEFAULT 1")
    private Boolean visible = true;

    @Column(name = "catid", nullable = false, columnDefinition = "int DEFAULT 0")
    private Integer catId = 0;

    @Column(name = "unite", nullable = false, length = 150)
    private String unite;

    @Column(name = "image", length = 255)
    private String image;

    @Column(name = "imgvisible", nullable = false, columnDefinition = "int DEFAULT 0")
    private Integer imgVisible = 0;

    // Relation avec Categorie (si vous souhaitez conserver la relation JPA)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catid", insertable = false, updatable = false) // Utilisez insertable=false, updatable=false pour éviter les conflits
    private Categorie categorie;

    public Integer getArtId() {
        return artId;
    }

    public void setArtId(Integer artId) {
        this.artId = artId;
    }

    public Integer getCustomId() {
        return customid;
    }

    public void setCustomId(Integer customid) {
        this.customid = customid;
    }

    public Integer getCatId() {
        return catId;
    }

    public void setCatId(Integer catId) {
        this.catId = catId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getImgVisible() {
        return imgVisible;
    }

    public void setImgVisible(Integer imgVisible) {
        this.imgVisible = imgVisible;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Double getPrix() {
        return prix;
    }

    public void setPrix(Double prix) {
        this.prix = prix;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getVisible() { // renommer "isVisible" en "getVisible"
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getUnite() {
        return unite;
    }

    public void setUnite(String unite) {
        this.unite = unite;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    public List<Object> getDisplayData()
    {
        List<Object> row = new ArrayList<>();
        row.add(getArtId());
        row.add(getNom());
        row.add(getPrix());
        row.add(getDescription());
        row.add(getVisible() ? "1" : "0");
        row.add(getCategorie() != null ? getCategorie().getNom() : "");
        row.add(getUnite());
        return row;
    }

    //public static ArrayList<?> displayData(SessionFactory sessionFactory, EntityManager entityManager, String table, Integer bouticid, Integer limit, Integer offset, String selcol, Integer selid) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
    //    return BaseEntity.displayData(sessionFactory, entityManager, table, bouticid, limit, offset, selcol, selid);
    //}



}