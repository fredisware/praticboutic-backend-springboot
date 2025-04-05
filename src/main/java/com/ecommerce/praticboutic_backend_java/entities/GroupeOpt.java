package com.ecommerce.praticboutic_backend_java.entities;

import com.ecommerce.praticboutic_backend_java.BaseEntity;
import jakarta.persistence.*;
import org.hibernate.SessionFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "groupeopt")
public class GroupeOpt extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grpoptid")
    private Integer grpoptid;

    @Column(name = "customid", nullable = false)
    private Integer customId;

    @Column(name = "nom", nullable = false, length = 150)
    private String nom;

    @Column(name = "visible", nullable = false, columnDefinition = "int DEFAULT 1")
    private Boolean visible = true;

    @Column(name = "multiple", nullable = false, columnDefinition = "int DEFAULT 0")
    private Boolean multiple = false;

    /**
     * Retourne l'identifiant du groupe d'options
     *
     * @return L'identifiant du groupe d'options
     */
    public Integer getGrpoptid() {
        return grpoptid;
    }

    /**
     * Définit l'identifiant du groupe d'options
     *
     * @param grpoptid L'identifiant du groupe d'options à définir
     */
    public void setGrpoptid(Integer grpoptid) {
        this.grpoptid = grpoptid;
    }

    // Getters et Setters (sans les accesseurs pour id qui sont dans BaseEntity)
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

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Boolean getMultiple() {
        return multiple;
    }

    public void setMultiple(Boolean multiple) {
        this.multiple = multiple;
    }

    public Map<String, String> getDisplayData()
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put("grpopptid" , getGrpoptid().toString());
        map.put("nom" , getNom());
        map.put("visible" , getVisible() ? "1" : "0");
        map.put("multiple" , getMultiple() ? "1" : "0");
        return map;
    }

    public static ArrayList<?> displayData(SessionFactory sessionFactory, EntityManager entityManager, String table, Integer bouticid, Integer limit, Integer offset, String selcol, Integer selid) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        return BaseEntity.displayData(sessionFactory, entityManager, table, bouticid, limit, offset, selcol, selid);
    }


}