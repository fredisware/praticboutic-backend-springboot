package com.ecommerce.praticboutic_backend_java.entities;

import com.ecommerce.praticboutic_backend_java.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.SessionFactory;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Entité représentant une option dans l'application
 */
@Entity
@Table(name = "\"option\"") // Le mot "option" étant un mot réservé en SQL, on utilise des backticks
public class Option implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "optid")
    private Integer optid;

    @Column(name = "customid", nullable = false)
    private Integer customId;

    @Column(name = "nom", nullable = false, length = 150)
    private String nom;

    @Column(name = "surcout", nullable = false)
    private Double surcout = 0.0;

    @Column(name = "grpoptid", nullable = false)
    private Integer grpoptid;

    @Column(name = "visible", nullable = false)
    private Boolean visible = true;

    // Relation avec Categorie (si vous souhaitez conserver la relation JPA)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grpoptid", insertable = false, updatable = false) // Utilisez insertable=false, updatable=false pour éviter les conflits
    private GroupeOpt groupeopt;

    // Constructeurs
    public Option() {
    }

    public Option(Integer customId, String nom) {
        this.customId = customId;
        this.nom = nom;
    }

    public Option(Integer customId, String nom, Double surcout, Integer groupeOptionId) {
        this.customId = customId;
        this.nom = nom;
        this.surcout = surcout;
    }

    // Getters et setters
    public Integer getOptId() {
        return optid;
    }

    public void setOptId(Integer id) {
        this.optid = id;
    }

    public Integer getCustomId() {
        return customId;
    }

    public void setCustomId(Integer customId) {
        this.customId = customId;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Double getSurcout() {
        return surcout;
    }

    public void setSurcout() {
        this.surcout = surcout;
    }

    public Integer getGroupeOptionId() {
        return grpoptid;
    }

    public void setGroupeOptionId(Boolean groupeOptionId) {
        this.grpoptid = grpoptid;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public GroupeOpt getGroupeOption() {
        return groupeopt;
    }

    public void setGroupeOption(GroupeOpt groupeopt) {
        this.groupeopt= groupeopt;
    }

    /**
     * Vérifie si l'option est visible
     * @return true si l'option est visible, false sinon
     */
    @Transient
    public boolean isVisible() {
        return visible;
    }

    @Override
    public String toString() {
        return "Option{" +
                "opid=" + optid +
                ", customId=" + customId +
                ", nom='" + nom + '\'' +
                ", surcout=" + surcout +
                ", groupeOptionId=" + grpoptid +
                ", visible=" + visible +
                '}';
    }

    public Map<String, String> getDisplayData()
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put("opptid" , getOptId().toString());
        map.put("nom" , getNom());
        map.put("surcout" , getSurcout().toString());
        map.put("groupeption" , getGroupeOption().getNom());
        map.put("visible" , getVisible() ? "1" : "0");
        return map;
    }

    public static ArrayList<?> displayData(SessionFactory sessionFactory, EntityManager entityManager, String table, Integer bouticid, Integer limit, Integer offset, String selcol, Integer selid) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        return BaseEntity.displayData(sessionFactory, entityManager, table, bouticid, limit, offset, selcol, selid);
    }

}