package com.ecommerce.praticboutic_backend_java;

import com.ecommerce.praticboutic_backend_java.entities.Client;
import com.ecommerce.praticboutic_backend_java.entities.Article;
import com.ecommerce.praticboutic_backend_java.entities.*;
import jakarta.persistence.*;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;

import java.util.Set;
import java.lang.Class;

@MappedSuperclass
//@Access(AccessType.FIELD)
public abstract class BaseEntity {

    public static String capitalize(String word) {
        if (word == null || word.isEmpty()) {
            return word; // Gérer les cas où le mot est null ou vide
        }
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    public static String getPrimaryKeyName(EntityManager entityManager, String tableName) throws ClassNotFoundException {
        // Récupérer le type d'entité à partir du EntityManager et du nom de la table
        EntityType<?> entityType = entityManager.getMetamodel().entity(loadEntityClass(tableName));

        // Parcourir les attributs de l'entité
        for (Attribute<?, ?> attribute : entityType.getAttributes()) {
            if (attribute instanceof SingularAttribute<?, ?> singularAttribute) {
                // Identifier la clé primaire
                if (singularAttribute.isId()) {
                    return singularAttribute.getName(); // Retourner le nom de la clé primaire
                }
            }
        }
        // Retourner null si aucune clé primaire n'a été trouvée
        return null;
    }

    public static Class<?> loadEntityClass(String table) throws ClassNotFoundException {
        // Construction dynamique de la classe d'entité
        String entityName = "com.ecommerce.praticboutic_backend_java.entities." + capitalize(table); // Chemin du package des entités
        //Class<?> maclasse = Class.forName(entityName);
        try {
            return Class.forName(entityName);
        } catch (ClassNotFoundException ex) {
            throw new ClassNotFoundException("L'entité spécifiée n'existe pas : " + capitalize(table));
        }
    }


}