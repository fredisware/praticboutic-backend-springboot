package com.ecommerce.praticboutic_backend_java.entities;

import com.ecommerce.praticboutic_backend_java.BaseEntity;
import com.ecommerce.praticboutic_backend_java.DatabaseLink;
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
    private Integer customId;

    @Column(name = "nom", nullable = false, length = 150)
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

    @Column(name = "obligatoire", nullable = false, columnDefinition = "int DEFAULT 0")
    private Integer obligatoire = 0;

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
        return customId;
    }

    public void setCustomId(Integer customId) {
        this.customId = customId;
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

    public Integer getObligatoire() {
        return obligatoire;
    }

    public void setObligatoire(Integer obligatoire) {
        this.obligatoire = obligatoire;
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

    /**
     * Méthode pour récupérer les relations (JOINS) associées à l'entité Article.
     * @return Liste d'objets `DatabaseLink` représentant les relations de l'entité Article.
     */
    public static List<DatabaseLink> getLinks() {
        List<DatabaseLink> links = new ArrayList<>();

        // Relation avec Categorie (LEFT JOIN pour illustrer)
        links.add(new DatabaseLink(
                "article",      // Table source
                "catid",        // Champ source (catid dans l'entité Article)
                "categorie",    // Table destination (entité Categorie)
                "catid",           // Champ destination (clé primaire dans la table Categorie)
                "lj",           // Type de jointure (LEFT JOIN pour cet exemple)
                0               // Indice unique
        ));

        return links;
    }

    public Map<String, String> getDisplayData()
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put("artid" , getArtId().toString());
        map.put("nom" , getNom());
        map.put("prix" , getPrix().toString());
        map.put("description" , getDescription());
        map.put("visible" , getVisible() ? "1" : "0");
        map.put("categorie", getCategorie().getNom());
        map.put("unite", getUnite());
        return map;
    }

    public static ArrayList<?> displayData(SessionFactory sessionFactory, EntityManager entityManager, String table, Integer bouticid, Integer limit, Integer offset, String selcol, Integer selid) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        return BaseEntity.displayData(sessionFactory, entityManager, table, bouticid, limit, offset, selcol, selid);
    }
}