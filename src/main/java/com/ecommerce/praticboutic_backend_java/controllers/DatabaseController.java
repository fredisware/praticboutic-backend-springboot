package com.ecommerce.praticboutic_backend_java.controllers;

import org.hibernate.Session;
import com.ecommerce.praticboutic_backend_java.*;
import com.ecommerce.praticboutic_backend_java.configurations.StripeConfig;
import com.ecommerce.praticboutic_backend_java.entities.*;
import com.ecommerce.praticboutic_backend_java.repositories.*;
import com.ecommerce.praticboutic_backend_java.requests.*;

import java.util.*;
import java.util.Locale;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.stream.Collectors;


import com.stripe.Stripe;
import com.stripe.model.Subscription;
import com.stripe.param.SubscriptionUpdateParams;
import jakarta.servlet.http.HttpSession;

import org.hibernate.SessionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.*;
import jakarta.persistence.metamodel.*;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import static com.ecommerce.praticboutic_backend_java.BaseEntity.capitalize;
import static com.ecommerce.praticboutic_backend_java.BaseEntity.loadEntityClass;

import org.hibernate.cfg.Configuration;

@RestController
@RequestMapping("/api")
public class DatabaseController {
    // Déclarez le logger en tant que champ statique en haut de votre classe
    private static final Logger logger = LoggerFactory.getLogger(DatabaseController.class);

    private final StripeConfig stripeConfig;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AbonnementRepository abonnementRepository;

    @Autowired
    private ParametreRepository parametreRepository;

    @Autowired
    private StatutCmdRepository statutCmdRepository;

    private static SessionFactory sessionFactory = null;

    public DatabaseController(StripeConfig stripeConfig) {
        this.stripeConfig = stripeConfig;
    }


    @PostMapping("/count-elements")
    public Map<String, Object> countElementsInTable(@RequestBody VueTableRequest input) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Vérification si le nom de la table est fourni
            if (input.getTable() == null || input.getTable().isEmpty()) {
                response.put("error", "Le nom de la table est vide.");
                return response;
            }

            Class<?> entityClass = loadEntityClass(input.getTable());

            // Création de la requête avec le EntityManager
            StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(*) FROM ");
            queryBuilder.append(input.getTable());
            queryBuilder.append(" e WHERE e.customid = :bouticid");

            // Ajout de conditions supplémentaires
            if (input.getSelcol() != null && !input.getSelcol().isEmpty() && input.getSelid() != null && input.getSelid() > 0) {
                queryBuilder.append(" AND e.").append(input.getSelcol()).append(" = :selid");
            }

            // Création de la requête dynamique
            Query query = entityManager.createNativeQuery(queryBuilder.toString());

            // Paramètres de la requête
            query.setParameter("bouticid", input.getBouticid());

            if (input.getSelcol() != null && !input.getSelcol().isEmpty() && input.getSelid() != null && input.getSelid() > 0) {
                query.setParameter("selid", input.getSelid());
            }

            // Exécution de la requête
            Long count = (Long) query.getSingleResult();

            response.put("count", count);
            response.put("entity", input.getTable());
        } catch (Exception e) {
            response.put("error", "Erreur lors de l'exécution : " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/vue-table")
    public Map<String, Object> vueTable(@RequestBody VueTableRequest input) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Variables d'entrée
            String tableName = input.getTable();
            Integer bouticid = input.getBouticid();
            String selcol = input.getSelcol();
            Integer selid = input.getSelid();
            Integer limit = input.getLimit();
            Integer offset = input.getOffset();

            // Validation des données d'entrée
            if (tableName == null || tableName.isEmpty()) {
                throw new IllegalArgumentException("Le nom de la table est vide.");
            }

            Class[] cArg = new Class[8];
            cArg[0] = SessionFactory.class;
            cArg[1] = EntityManager.class; cArg[2] = String.class; cArg[3] = Integer.class;
            cArg[4] = Integer.class; cArg[5] = Integer.class; cArg[6] = String.class;
            cArg[7] = Integer.class;

                try {
                    sessionFactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
                } catch (Throwable ex) {
                    throw new ExceptionInInitializerError(ex);
                }

            Class<?> entityClass = BaseEntity.getEntityClassFromTableName(sessionFactory, tableName);
            Method method = entityClass.getDeclaredMethod("displayData", cArg);
            Object entityInstance = entityClass.getDeclaredConstructor().newInstance();
            ArrayList<?> data = (ArrayList<?>)method.invoke( entityInstance, sessionFactory, entityManager, tableName, bouticid, limit, offset, selcol, selid);

            // Construction de la réponse
            response.put("data", data);
            response.put("count", data.size());
            response.put("status", "success");
        }
        catch(Exception e2)
        {
            response.put("error", "Erreur lors de l'exécution : " + e2.getMessage());
        }

        return response;
    }

    @PostMapping("/remplirOption")
    public Map<String, Object> remplirOption(@RequestBody RemplirOptionTableRequest input) throws ClassNotFoundException {
        Map<String, Object> response = new HashMap<>();

        String strClePrimaire = null;

        // Variables d'entrée
        String tableName = input.getTable();
        Long idBoutic = input.getBouticid();
        String strColSel = input.getSelcol();
        Long idSel = input.getSelid();
        Integer iLimit = input.getLimit();
        Integer iOffset = input.getOffset();

        // Validation des données d'entrée
        if (tableName == null || tableName.isEmpty()) {
            response.put("error", "Le nom de la table est vide.");
            return response;
        }

        strClePrimaire = BaseEntity.getPrimaryKeyName(sessionFactory, entityManager, tableName);

        if (strClePrimaire == null) {
            throw new IllegalArgumentException("Aucune clé primaire trouvée pour cette table");
        }

        // Création de la requête SQL
        StringBuilder query = new StringBuilder("SELECT ")
                .append(strClePrimaire).append(", ")
                .append(input.getSelcol())
                .append(" FROM `").append(tableName).append("`")
                .append(" WHERE customid = ").append(idBoutic)
                .append(" OR ").append(strClePrimaire).append(" = 0");

        if ("statutcmd".equals(tableName)) {
            query.append(" AND actif = 1");
        }

        TypedQuery<Object[]> typedQuery = entityManager.createQuery(query.toString(), Object[].class);
        List<Object[]> results = typedQuery.getResultList();
        response.put("results", results);

        return response;
    }

    /**
     * Insère une nouvelle ligne dans une table spécifiée
     *
     * @param input La requête contenant les informations d'insertion
     * @return Une Map contenant le résultat de l'opération
     */
    @PostMapping("/insert-row")
    public Map<String, Object> insertRow(@RequestBody InsertRowRequest input) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validation de l'entrée
            if (input.getTable() == null || input.getTable().isEmpty()) {
                response.put("error", "Le nom de la table est requis");
                return response;
            }

            if (input.getBouticid() == null) {
                response.put("error", "L'ID boutic est requis");
                return response;
            }

            if (input.getRow() == null || input.getRow().isEmpty()) {
                response.put("error", "Les données à insérer sont requises");
                return response;
            }

            // Construction dynamique de la classe d'entité
            Class<?> entityClass;
            try {
                String entityName = "com.ecommerce.praticboutic_backend_java.entities." + input.getTable();
                entityClass = Class.forName(entityName);
            } catch (ClassNotFoundException ex) {
                response.put("error", "L'entité spécifiée n'existe pas : " + input.getTable());
                return response;
            }

            // Création d'une nouvelle instance de l'entité
            Object entity;
            try {
                entity = entityClass.getDeclaredConstructor().newInstance();

                // Définir customid/bouticid
                Method setCustomIdMethod = entityClass.getMethod("setCustomid", Long.class);
                setCustomIdMethod.invoke(entity, input.getBouticid());

                // Parcourir les champs à insérer
                for (ColumnData column : input.getRow()) {
                    // Vérifier les contraintes d'unicité pour les champs de type "ref", "codepromo" ou "email"
                    if ("ref".equals(column.getType()) || "codepromo".equals(column.getType())) {
                        String jpql = "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e " +
                                "WHERE e.customid = :customid AND e." + column.getNom() + " = :valeur";

                        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
                        query.setParameter("customid", input.getBouticid());
                        query.setParameter("valeur", column.getValeur());

                        if (query.getSingleResult() > 0) {
                            throw new IllegalArgumentException("Impossible d'avoir plusieurs fois la valeur '" +
                                    column.getValeur() + "' dans la colonne '" +
                                    column.getDesc() + "'");
                        }
                    }

                    if ("email".equals(column.getType())) {
                        String jpql = "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e " +
                                "WHERE e." + column.getNom() + " = :valeur";

                        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
                        query.setParameter("valeur", column.getValeur());

                        if (query.getSingleResult() > 0) {
                            throw new IllegalArgumentException("Le courriel '" + column.getValeur() +
                                    "' existe déjà dans la base de données");
                        }
                    }

                    // Définir la valeur de la colonne sur l'entité
                    String setterName = "set" + column.getNom().substring(0, 1).toUpperCase() +
                            column.getNom().substring(1);

                    // Trouver la méthode appropriée
                    Method[] methods = entityClass.getMethods();
                    Method setterMethod = null;

                    for (Method method : methods) {
                        if (method.getName().equals(setterName) && method.getParameterCount() == 1) {
                            setterMethod = method;
                            break;
                        }
                    }

                    if (setterMethod == null) {
                        throw new NoSuchMethodException("Méthode " + setterName + " non trouvée");
                    }

                    // Convertir et assigner la valeur selon le type
                    Class<?> paramType = setterMethod.getParameterTypes()[0];
                    Object value = column.getValeur();

                    // Traiter les cas spéciaux comme les mots de passe
                    if ("pass".equals(column.getType())) {
                        // Utiliser BCrypt pour hacher le mot de passe
                        value = new BCryptPasswordEncoder().encode(column.getValeur());
                    }

                    // Convertir la valeur au type approprié
                    if (paramType == Long.class || paramType == long.class) {
                        value = Long.parseLong(column.getValeur().toString());
                    } else if (paramType == Integer.class || paramType == int.class) {
                        value = Integer.parseInt(column.getValeur().toString());
                    } else if (paramType == Double.class || paramType == double.class) {
                        value = Double.parseDouble(column.getValeur().toString());
                    } else if (paramType == Boolean.class || paramType == boolean.class) {
                        value = Boolean.parseBoolean(column.getValeur().toString());
                    } else if (paramType == Date.class) {
                        // Adapter selon votre format de date
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        value = format.parse(column.getValeur().toString());
                    }

                    // Appliquer la valeur
                    setterMethod.invoke(entity, value);
                }

                // Persister l'entité
                entityManager.persist(entity);

                // Récupérer l'ID généré
                Method getIdMethod = entityClass.getMethod("getId");
                Object id = getIdMethod.invoke(entity);

                // Répondre avec l'ID inséré
                response.put("id", id);
                response.put("success", true);

            } catch (Exception e) {
                response.put("error", e.getMessage());
                e.printStackTrace();
            }

        } catch (Exception e) {
            response.put("error", e.getMessage());
            e.printStackTrace();
        }

        return response;
    }

    /**
     * Met à jour une ligne existante dans une table spécifiée
     *
     * @param input La requête contenant les informations de mise à jour
     * @return Une Map contenant le résultat de l'opération
     */
    @PostMapping("/update-row")
    public Map<String, Object> updateRow(@RequestBody UpdateRowRequest input) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validation de l'entrée
            if (input.getTable() == null || input.getTable().isEmpty()) {
                response.put("error", "Le nom de la table est requis");
                return response;
            }

            if (input.getBouticid() == null) {
                response.put("error", "L'ID boutic est requis");
                return response;
            }

            if (input.getRow() == null || input.getRow().isEmpty()) {
                response.put("error", "Les données à mettre à jour sont requises");
                return response;
            }

            if (input.getIdtoup() == null) {
                response.put("error", "L'ID de l'élément à mettre à jour est requis");
                return response;
            }

            if (input.getColonne() == null || input.getColonne().isEmpty()) {
                response.put("error", "Le nom de la colonne d'ID est requis");
                return response;
            }

            // Construction dynamique de la classe d'entité
            Class<?> entityClass;
            try {
                String entityName = "com.ecommerce.praticboutic_backend_java.entities." + input.getTable();
                entityClass = Class.forName(entityName);
            } catch (ClassNotFoundException ex) {
                response.put("error", "L'entité spécifiée n'existe pas : " + input.getTable());
                return response;
            }

            // Récupérer l'entité à mettre à jour
            String jpqlFind = "SELECT e FROM " + entityClass.getSimpleName() + " e " +
                    "WHERE e." + input.getColonne() + " = :id AND e.customid = :customid";

            Query queryFind = entityManager.createQuery(jpqlFind);
            queryFind.setParameter("id", input.getIdtoup());
            queryFind.setParameter("customid", input.getBouticid());

            Object entity;
            try {
                entity = queryFind.getSingleResult();
            } catch (Exception e) {
                response.put("error", "Entité non trouvée avec ID: " + input.getIdtoup());
                return response;
            }

            // Parcourir les champs à mettre à jour
            for (ColumnData column : input.getRow()) {
                // Vérifier les contraintes d'unicité pour les champs de type "ref", "codepromo" ou "email"
                if ("ref".equals(column.getType()) || "codepromo".equals(column.getType())) {
                    String jpql = "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e " +
                            "WHERE e.customid = :customid AND e." + column.getNom() + " = :valeur " +
                            "AND e." + input.getColonne() + " != :id";

                    TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
                    query.setParameter("customid", input.getBouticid());
                    query.setParameter("valeur", column.getValeur());
                    query.setParameter("id", input.getIdtoup());

                    if (query.getSingleResult() > 0) {
                        throw new IllegalArgumentException("Impossible d'avoir plusieurs fois la valeur '" +
                                column.getValeur() + "' dans la colonne '" +
                                column.getDesc() + "'");
                    }
                }

                if ("email".equals(column.getType())) {
                    String jpql = "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e " +
                            "WHERE e." + column.getNom() + " = :valeur " +
                            "AND e." + input.getColonne() + " != :id";

                    TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
                    query.setParameter("valeur", column.getValeur());
                    query.setParameter("id", input.getIdtoup());

                    if (query.getSingleResult() > 0) {
                        throw new IllegalArgumentException("Le courriel '" + column.getValeur() +
                                "' existe déjà dans la base de données");
                    }
                }

                // Traiter le cas spécial des mots de passe vides (à ne pas mettre à jour)
                if ("pass".equals(column.getType()) && (column.getValeur() == null ||
                        column.getValeur().toString().isEmpty())) {
                    continue; // Sauter ce champ
                }

                // Définir la valeur de la colonne sur l'entité
                String setterName = "set" + column.getNom().substring(0, 1).toUpperCase() +
                        column.getNom().substring(1);

                // Trouver la méthode appropriée
                Method[] methods = entityClass.getMethods();
                Method setterMethod = null;

                for (Method method : methods) {
                    if (method.getName().equals(setterName) && method.getParameterCount() == 1) {
                        setterMethod = method;
                        break;
                    }
                }

                if (setterMethod == null) {
                    throw new NoSuchMethodException("Méthode " + setterName + " non trouvée");
                }

                // Convertir et assigner la valeur selon le type
                Class<?> paramType = setterMethod.getParameterTypes()[0];
                Object value = column.getValeur();

                // Traiter les cas spéciaux comme les mots de passe
                if ("pass".equals(column.getType())) {
                    // Utiliser BCrypt pour hacher le mot de passe
                    value = new BCryptPasswordEncoder().encode(column.getValeur().toString());
                }

                // Convertir la valeur au type approprié
                if (paramType == Long.class || paramType == long.class) {
                    value = Long.parseLong(column.getValeur().toString());
                } else if (paramType == Integer.class || paramType == int.class) {
                    value = Integer.parseInt(column.getValeur().toString());
                } else if (paramType == Double.class || paramType == double.class) {
                    value = Double.parseDouble(column.getValeur().toString());
                } else if (paramType == Boolean.class || paramType == boolean.class) {
                    value = Boolean.parseBoolean(column.getValeur().toString());
                } else if (paramType == Date.class) {
                    // Adapter selon votre format de date
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    value = format.parse(column.getValeur().toString());
                }

                // Appliquer la valeur
                setterMethod.invoke(entity, value);
            }

            // Persister les modifications
            entityManager.merge(entity);

            response.put("success", true);

        } catch (Exception e) {
            response.put("error", e.getMessage());
            e.printStackTrace();
        }

        return response;
    }

    /**
     * Récupère les valeurs d'une ligne spécifique dans une table
     *
     * @param input La requête contenant les informations pour récupérer les valeurs
     * @return Une Map contenant le résultat de l'opération
     */
    @PostMapping("/get-values")
    public Map<String, Object> getValues(@RequestBody GetValuesRequest input) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validation de l'entrée
            if (input.getTable() == null || input.getTable().isEmpty()) {
                response.put("error", "Le nom de la table est requis");
                return response;
            }

            if (input.getBouticid() == null) {
                response.put("error", "L'ID boutic est requis");
                return response;
            }

            if (input.getIdtoup() == null) {
                response.put("error", "L'ID de l'élément est requis");
                return response;
            }

            // Construction dynamique de la classe d'entité
            Class<?> entityClass;
            try {
                String entityName = "com.ecommerce.praticboutic_backend_java.entities." + input.getTable();
                entityClass = Class.forName(entityName);
            } catch (ClassNotFoundException ex) {
                response.put("error", "L'entité spécifiée n'existe pas : " + input.getTable());
                return response;
            }

            // Trouver la clé primaire (chercher l'annotation @Id)
            String primaryKeyField = null;
            Field[] fields = entityClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Id.class)) {
                    primaryKeyField = field.getName();
                    break;
                }
            }

            if (primaryKeyField == null) {
                response.put("error", "Impossible de déterminer la clé primaire pour l'entité " + input.getTable());
                return response;
            }

            // Créer la requête JPQL pour récupérer l'entité
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e " +
                    "WHERE e." + primaryKeyField + " = :id AND e.customid = :customid";

            Query query = entityManager.createQuery(jpql);
            query.setParameter("id", input.getIdtoup());
            query.setParameter("customid", input.getBouticid());

            Object entity;
            try {
                entity = query.getSingleResult();
            } catch (NoResultException e) {
                response.put("error", "Aucun enregistrement trouvé avec l'ID " + input.getIdtoup());
                return response;
            }

            // Extraire toutes les valeurs des champs
            List<Object> values = new ArrayList<>();
            for (Field field : fields) {
                // Rendre le champ accessible s'il est privé
                field.setAccessible(true);

                // Récupérer la valeur
                Object value = field.get(entity);

                // Ajouter la valeur à la liste (gérer les cas null)
                values.add(value != null ? value.toString() : "");
            }

            // Ajouter les valeurs à la réponse
            response.put("values", values);
            response.put("success", true);

        } catch (Exception e) {
            response.put("error", e.getMessage());
            e.printStackTrace();
        }

        return response;
    }

    /**
     * Récupère les couleurs associées aux commandes
     *
     * @param input La requête contenant les paramètres de pagination et l'ID boutic
     * @return Une Map contenant le résultat de l'opération avec les couleurs
     */
    @PostMapping("/color-row")
    public Map<String, Object> getOrderColors(@RequestBody ColorRowRequest input) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validation de l'entrée
            if (input.getBouticid() == null) {
                response.put("error", "L'ID boutic est requis");
                return response;
            }

            if (input.getLimite() == null || input.getLimite() <= 0) {
                response.put("error", "La limite est requise et doit être positive");
                return response;
            }

            if (input.getOffset() == null || input.getOffset() < 0) {
                response.put("error", "L'offset est requis et doit être non négatif");
                return response;
            }

            // Créer la requête JPQL
            String jpql = "SELECT s.couleur FROM Commande c " +
                    "INNER JOIN StatutCmd s ON c.statid = s.statid " +
                    "WHERE c.customid = :customid " +
                    "ORDER BY c.cmdid";

            Query query = entityManager.createQuery(jpql);
            query.setParameter("customid", input.getBouticid());
            query.setFirstResult(input.getOffset());
            query.setMaxResults(input.getLimite());

            // Exécuter la requête et récupérer les résultats
            List<String> colors = query.getResultList();

            // Transformer les résultats pour correspondre au format attendu
            List<List<String>> formattedResults = new ArrayList<>();
            for (String color : colors) {
                List<String> colorWrapper = new ArrayList<>();
                colorWrapper.add(color);
                formattedResults.add(colorWrapper);
            }

            response.put("colors", formattedResults);
            response.put("success", true);

        } catch (Exception e) {
            response.put("error", e.getMessage());
            e.printStackTrace();
        }

        return response;
    }

    /**
     * Récupère les données d'une commande et formate un message avec ces données
     *
     * @param input La requête contenant l'ID de la commande et l'ID boutic
     * @return Une Map contenant le résultat de l'opération
     */
    @PostMapping("/get-com-data")
    public Map<String, Object> getOrderData(@RequestBody GetComDataRequest input) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validation de l'entrée
            if (input.getCmdid() == null) {
                response.put("error", "L'ID de la commande est requis");
                return response;
            }

            if (input.getBouticid() == null) {
                response.put("error", "L'ID boutic est requis");
                return response;
            }

            // Créer la requête JPQL pour récupérer les données
            String jpql = "SELECT c.telephone, s.message, c.numref, c.nom, c.prenom, " +
                    "c.adresse1, c.adresse2, c.codepostal, c.ville, c.vente, " +
                    "c.paiement, c.sstotal, c.fraislivraison, c.total, c.commentaire, " +
                    "s.etat, cust.nom " +
                    "FROM Commande c " +
                    "INNER JOIN StatutCmd s ON c.statid = s.statid " +
                    "INNER JOIN Customer cust ON c.customid = cust.customid " +
                    "WHERE c.cmdid = :cmdid AND c.customid = :customid " +
                    "AND s.customid = :customid AND cust.customid = :customid " +
                    "ORDER BY c.cmdid";

            Query query = entityManager.createQuery(jpql);
            query.setParameter("cmdid", input.getCmdid());
            query.setParameter("customid", input.getBouticid());

            // Exécuter la requête et récupérer le résultat
            Object[] row;
            try {
                row = (Object[]) query.getSingleResult();
            } catch (NoResultException e) {
                response.put("error", "Aucune commande trouvée avec l'ID " + input.getCmdid());
                return response;
            }

            // Formater le message en remplaçant les variables
            String content = (String) row[1]; // message

            // Remplacer toutes les variables par leurs valeurs
            content = content.replace("%boutic%", (String) row[16]); // nom du customer
            content = content.replace("%telephone%", (String) row[0]); // téléphone
            content = content.replace("%numref%", (String) row[2]); // numref
            content = content.replace("%nom%", (String) row[3]); // nom
            content = content.replace("%prenom%", (String) row[4]); // prenom
            content = content.replace("%adresse1%", (String) row[5]); // adresse1
            content = content.replace("%adresse2%", (String) row[6] != null ? (String) row[6] : ""); // adresse2
            content = content.replace("%codepostal%", (String) row[7]); // codepostal
            content = content.replace("%ville%", (String) row[8]); // ville
            content = content.replace("%vente%", (String) row[9]); // vente
            content = content.replace("%paiement%", (String) row[10]); // paiement

            // Formater les montants (nombre avec 2 décimales, virgule comme séparateur décimal et espace pour les milliers)
            NumberFormat formatter = NumberFormat.getNumberInstance(Locale.FRANCE);
            formatter.setMinimumFractionDigits(2);
            formatter.setMaximumFractionDigits(2);

            BigDecimal sstotal = (BigDecimal) row[11];
            BigDecimal fraislivraison = (BigDecimal) row[12];
            BigDecimal total = (BigDecimal) row[13];

            content = content.replace("%sstotal%", formatter.format(sstotal)); // sstotal
            content = content.replace("%fraislivraison%", formatter.format(fraislivraison)); // fraislivraison
            content = content.replace("%total%", formatter.format(total)); // total

            content = content.replace("%commentaire%", row[14] != null ? (String) row[14] : ""); // commentaire
            content = content.replace("%etat%", (String) row[15]); // etat

            String message = content;

            // Créer le tableau de réponse
            List<String> result = new ArrayList<>();
            result.add((String) row[0]); // téléphone
            result.add(message); // message formaté

            response.put("data", result);
            response.put("success", true);

        } catch (Exception e) {
            response.put("error", e.getMessage());
            e.printStackTrace();
        }

        return response;
    }

    /**
     * Récupère une propriété spécifique d'un customer
     */
    @PostMapping("/get-custom-prop")
    public ResponseEntity<Map<String, Object>> getCustomProperty(@RequestBody CustomPropertyRequest input) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validation des paramètres
            if (input.getBouticid() == null) {
                response.put("error", "L'ID boutic est requis");
                return ResponseEntity.badRequest().body(response);
            }

            if (input.getProp() == null || input.getProp().isEmpty()) {
                response.put("error", "La propriété à récupérer est requise");
                return ResponseEntity.badRequest().body(response);
            }

            // Vérification pour éviter l'injection SQL
            validatePropertyName(input.getProp());

            // Création de la requête JPQL
            String jpql = "SELECT c." + input.getProp() + " FROM Customer c " +
                    "WHERE c.customid = :customid";

            Query query = entityManager.createQuery(jpql);
            query.setParameter("customid", input.getBouticid());
            query.setMaxResults(1);

            // Exécution de la requête
            Object result;
            try {
                result = query.getSingleResult();
            } catch (NoResultException e) {
                response.put("error", "Aucun customer trouvé avec l'ID " + input.getBouticid());
                return ResponseEntity.ok(response);
            } catch (IllegalArgumentException e) {
                response.put("error", "Propriété invalide: " + input.getProp());
                return ResponseEntity.badRequest().body(response);
            }

            // Construction de la réponse
            List<Object> values = new ArrayList<>();
            values.add(result);

            response.put("values", values);
            response.put("success", true);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Une erreur est survenue: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Met à jour une propriété spécifique d'un customer
     */
    @PostMapping("/set-custom-prop")
    public ResponseEntity<Map<String, Object>> setCustomProperty(@RequestBody CustomPropertyUpdateRequest input) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validation des paramètres
            if (input.getBouticid() == null) {
                response.put("error", "L'ID boutic est requis");
                return ResponseEntity.badRequest().body(response);
            }

            if (input.getProp() == null || input.getProp().isEmpty()) {
                response.put("error", "La propriété à mettre à jour est requise");
                return ResponseEntity.badRequest().body(response);
            }

            if (input.getValeur() == null) {
                response.put("error", "La valeur de la propriété est requise");
                return ResponseEntity.badRequest().body(response);
            }

            // Vérification pour éviter l'injection SQL
            validatePropertyName(input.getProp());

            // Vérifier si la valeur existe déjà pour d'autres customers (pour le champ 'customer')
            if ("customer".equals(input.getProp())) {
                String checkJpql = "SELECT COUNT(c) FROM Customer c " +
                        "WHERE c." + input.getProp() + " = :valeur " +
                        "AND c.customid != :customid";

                Query checkQuery = entityManager.createQuery(checkJpql);
                checkQuery.setParameter("valeur", input.getValeur());
                checkQuery.setParameter("customid", input.getBouticid());

                Long count = (Long) checkQuery.getSingleResult();

                if (count >= 1) {
                    response.put("result", "KO");
                    return ResponseEntity.ok(response);
                }
            }

            // Mise à jour de la propriété
            String updateJpql = "UPDATE Customer c SET c." + input.getProp() + " = :valeur " +
                    "WHERE c.customid = :customid";

            Query updateQuery = entityManager.createQuery(updateJpql);
            updateQuery.setParameter("valeur", input.getValeur());
            updateQuery.setParameter("customid", input.getBouticid());

            int updatedRows = updateQuery.executeUpdate();

            if (updatedRows > 0) {
                response.put("result", "OK");
            } else {
                response.put("result", "KO");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("result", "KO");
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Récupère une propriété spécifique d'un client associé à un customer
     */
    @PostMapping("/get-client-prop")
    public ResponseEntity<Map<String, Object>> getClientProperty(@RequestBody ClientPropertyRequest input) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validation des paramètres
            if (input.getBouticid() == null) {
                response.put("error", "L'ID boutic est requis");
                return ResponseEntity.badRequest().body(response);
            }

            if (input.getProp() == null || input.getProp().isEmpty()) {
                response.put("error", "La propriété à récupérer est requise");
                return ResponseEntity.badRequest().body(response);
            }

            // Vérification pour éviter l'injection SQL
            validatePropertyName(input.getProp());

            // Création de la requête JPQL
            String jpql = "SELECT cl." + input.getProp() + " FROM Customer c " +
                    "JOIN Client cl ON c.cltid = cl.cltid " +
                    "WHERE c.customid = :customid";

            Query query = entityManager.createQuery(jpql);
            query.setParameter("customid", input.getBouticid());
            query.setMaxResults(1);

            // Exécution de la requête
            Object result;
            try {
                result = query.getSingleResult();
            } catch (NoResultException e) {
                response.put("error", "Aucun client trouvé pour le customer avec l'ID " + input.getBouticid());
                return ResponseEntity.ok(response);
            } catch (IllegalArgumentException e) {
                response.put("error", "Propriété invalide: " + input.getProp());
                return ResponseEntity.badRequest().body(response);
            }

            // Construction de la réponse
            List<Object> values = new ArrayList<>();
            values.add(result);

            response.put("values", values);
            response.put("success", true);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            response.put("error", "Une erreur est survenue: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Met à jour une propriété spécifique d'un client associé à un customer
     */
    @PostMapping("/set-client-prop")
    public ResponseEntity<Map<String, Object>> setClientProperty(@RequestBody ClientPropertyUpdateRequest input) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validation des paramètres
            if (input.getBouticid() == null) {
                response.put("error", "L'ID boutic est requis");
                return ResponseEntity.badRequest().body(response);
            }

            if (input.getProp() == null || input.getProp().isEmpty()) {
                response.put("error", "La propriété à mettre à jour est requise");
                return ResponseEntity.badRequest().body(response);
            }

            if (input.getValeur() == null) {
                response.put("error", "La valeur de la propriété est requise");
                return ResponseEntity.badRequest().body(response);
            }

            // Vérification pour éviter l'injection SQL
            validatePropertyName(input.getProp());

            // Récupérer l'ID du client associé au customer
            String cltIdQuery = "SELECT c.cltid FROM Customer c WHERE c.customid = :customid";
            Query query = entityManager.createQuery(cltIdQuery);
            query.setParameter("customid", input.getBouticid());

            Long cltid;
            try {
                cltid = (Long) query.getSingleResult();
            } catch (NoResultException e) {
                response.put("error", "Aucun client trouvé pour le customer avec l'ID " + input.getBouticid());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Mise à jour de la propriété
            String updateJpql;
            if ("pass".equals(input.getProp()) && !input.getValeur().isEmpty()) {
                // Cas spécial pour le mot de passe : hachage avant stockage
                String hashedPassword = BCrypt.hashpw(input.getValeur(), BCrypt.gensalt());
                updateJpql = "UPDATE Client c SET c." + input.getProp() + " = :valeur " +
                        "WHERE c.cltid = :cltid";

                Query updateQuery = entityManager.createQuery(updateJpql);
                updateQuery.setParameter("valeur", hashedPassword);
                updateQuery.setParameter("cltid", cltid);
                updateQuery.executeUpdate();
            } else {
                // Cas général
                updateJpql = "UPDATE Client c SET c." + input.getProp() + " = :valeur " +
                        "WHERE c.cltid = :cltid";

                Query updateQuery = entityManager.createQuery(updateJpql);
                updateQuery.setParameter("valeur", input.getValeur());
                updateQuery.setParameter("cltid", cltid);
                updateQuery.executeUpdate();
            }

            response.put("result", "OK");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Une erreur est survenue: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Méthode utilitaire pour valider les noms de propriété afin d'éviter l'injection SQL
     */
    private void validatePropertyName(String propertyName) {
        // Vérifier que le nom de propriété ne contient que des caractères alphanumériques et des underscores
        if (!propertyName.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Nom de propriété invalide: " + propertyName);
        }
    }

    /**
     * Méthode pour créer une nouvelle boutique et configurer tous ses paramètres associés
     */
    @PostMapping("/build-boutic")
    public ResponseEntity<Map<String, Object>> buildBoutic(@RequestBody BuildBouticRequest input, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Vérification de la présence des données de session nécessaires
            validateSessionDataForBuildBoutic(session);

            // Début de la transaction
            CustomTransactionManager transactionManager = new CustomTransactionManager();
            transactionManager.executeWithTransaction(status -> {
                // Vérification de l'unicité de l'email
                String verifyEmail = (String) session.getAttribute("verify_email");
                if (verifyEmail == null) {
                    throw new IllegalArgumentException("L'email ne peut pas être null");
                }

                Long existingClientCount = clientRepository.countByEmail(verifyEmail);

                if (existingClientCount > 0) {
                    throw new IllegalStateException("Impossible d'avoir plusieurs fois le même courriel " + verifyEmail);
                }

                // Création du client
                String hashedPassword = BCrypt.hashpw((String) session.getAttribute("registration_pass"), BCrypt.gensalt());

                Client client = new Client();
                client.setEmail(verifyEmail);
                client.setPass(hashedPassword);
                client.setQualite((String) session.getAttribute("registration_qualite"));
                client.setNom((String) session.getAttribute("registration_nom"));
                client.setPrenom((String) session.getAttribute("registration_prenom"));
                client.setAdr1((String) session.getAttribute("registration_adr1"));
                client.setAdr2((String) session.getAttribute("registration_adr2"));
                client.setCp((String) session.getAttribute("registration_cp"));
                client.setVille((String) session.getAttribute("registration_ville"));
                client.setTel((String) session.getAttribute("registration_tel"));
                client.setStripeCustomerId((String) session.getAttribute("registration_stripe_customer_id"));
                client.setActif(true);
                client.setDeviceId(input.getDeviceId());
                client.setDeviceType(input.getDeviceType());

                clientRepository.save(client);

                // Validation de l'alias de la boutique
                String aliasBoutic = (String) session.getAttribute("initboutic_aliasboutic");
                if (StringUtils.isEmpty(aliasBoutic)) {
                    throw new IllegalArgumentException("Identifiant vide");
                }

                // Vérification des identifiants interdits
                List<String> forbiddenIds = Arrays.asList("admin", "common", "upload", "vendor");
                if (forbiddenIds.contains(aliasBoutic)) {
                    throw new IllegalArgumentException("Identifiant interdit");
                }

                // Création de la boutique (customer)
                Customer customer = new Customer();
                customer.setCltid(client.getCltId());
                customer.setCustomer(aliasBoutic);
                customer.setNom((String) session.getAttribute("initboutic_nom"));
                customer.setLogo((String) session.getAttribute("initboutic_logo"));
                customer.setCourriel((String) session.getAttribute("initboutic_email"));

                customerRepository.save(customer);

                // Création de l'abonnement
                Abonnement abonnement = new Abonnement();
                abonnement.setCltId(client.getCltId());
                abonnement.setCreationBoutic(false);
                abonnement.setBouticId(customer.getCustomId());
                abonnement.setStripeSubscriptionId((String) session.getAttribute("creationabonnement_stripe_subscription_id"));
                abonnement.setActif(1);

                abonnementRepository.save(abonnement);

                // Mise à jour des métadonnées Stripe
                updateStripeSubscriptionMetadata(
                        (String) session.getAttribute("creationabonnement_stripe_subscription_id"),
                        abonnement.getAboId()
                );

                // Création des paramètres par défaut
                createDefaultParameters(customer.getCustomId(), session);

                // Création des statuts de commande par défaut
                createDefaultOrderStatuses(customer.getCustomId());

                // Mise à jour de la session
                session.setAttribute("bo_stripe_customer_id", session.getAttribute("registration_stripe_customer_id"));
                session.setAttribute("bo_id", customer.getCustomId());
                session.setAttribute("bo_email", session.getAttribute("verify_email"));
                session.setAttribute("bo_auth", "oui");
                session.setAttribute("bo_init", "oui");

                return null;
            });

            response.put("success", true);
            response.put("message", "Boutique créée avec succès");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException | IllegalStateException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("error", "Une erreur est survenue: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Méthode pour mettre à jour l'adresse email d'une boutique
     */
    @PostMapping("/radress-boutic")
    public ResponseEntity<Map<String, Object>> updateBouticEmail(@RequestBody UpdateEmailRequest input, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Vérification de la session
            if (session.getAttribute("bo_id") == null || session.getAttribute("bo_email") == null) {
                throw new IllegalStateException("Session invalide ou expirée");
            }

            Integer bouticId = Integer.parseInt(session.getAttribute("bo_id").toString());
            String currentEmail = (String) session.getAttribute("bo_email");

            // Vérification de l'unicité de l'email
            Long emailCount = clientRepository.countByEmail(currentEmail);
            if (emailCount > 1) {
                throw new IllegalStateException("Impossible d'avoir plusieurs fois le même courriel " + currentEmail);
            }

            // Récupération de l'ID client associé à la boutique

            Customer customer = customerRepository.findByCustomid(bouticId)
                    .orElseThrow(() -> new IllegalStateException("Boutique introuvable"));

            // Mise à jour de l'email du client
            clientRepository.updateEmailById(input.getEmail(), customer.getCltid());

            // Mise à jour de la session
            session.setAttribute("bo_email", input.getEmail());

            response.put("success", true);
            response.put("message", "Adresse email mise à jour avec succès");
            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            logger.error("Erreur de validation lors de la mise à jour de l'email", e);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour de l'email", e);
            response.put("error", "Une erreur est survenue: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Méthode pour vérifier la présence des données de session nécessaires à la création d'une boutique
     */
    private void validateSessionDataForBuildBoutic(HttpSession session) {
        Map<String, String> requiredSessionAttributes = new HashMap<>();
        requiredSessionAttributes.put("verify_email", "Email de vérification");
        requiredSessionAttributes.put("registration_pass", "Mot de passe");
        requiredSessionAttributes.put("registration_qualite", "Qualité");
        requiredSessionAttributes.put("registration_nom", "Nom");
        requiredSessionAttributes.put("registration_prenom", "Prénom");
        requiredSessionAttributes.put("initboutic_aliasboutic", "Alias de la boutique");
        requiredSessionAttributes.put("initboutic_nom", "Nom de la boutique");
        requiredSessionAttributes.put("initboutic_logo", "Logo");
        requiredSessionAttributes.put("initboutic_email", "Email de la boutique");
        requiredSessionAttributes.put("creationabonnement_stripe_subscription_id", "ID d'abonnement Stripe");
        requiredSessionAttributes.put("confboutic_validsms", "Validation par SMS");
        requiredSessionAttributes.put("confboutic_chxpaie", "Choix de paiement");
        requiredSessionAttributes.put("confboutic_chxmethode", "Méthode de livraison");
        requiredSessionAttributes.put("confboutic_mntmincmd", "Montant minimum de commande");

        List<String> missingAttributes = requiredSessionAttributes.entrySet().stream()
                .filter(entry -> session.getAttribute(entry.getKey()) == null)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        if (!missingAttributes.isEmpty()) {
            throw new IllegalStateException("Données de session manquantes: " + String.join(", ", missingAttributes));
        }
    }

    /**
     * Méthode pour mettre à jour les métadonnées de l'abonnement Stripe
     */
    private void updateStripeSubscriptionMetadata(String subscriptionId, Integer abonnementId) {
        try {
            Stripe.apiKey = stripeConfig.getSecretKey();
            Stripe.setAppInfo(
                    "pratic-boutic/registration",
                    "0.0.2",
                    "https://praticboutic.fr"
            );

            Map<String, String> metadata = new HashMap<>();
            metadata.put("pbabonumref", "ABOPB" + StringUtils.leftPad(abonnementId.toString(), 10, "0"));

            SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                    .setMetadata(metadata)
                    .build();

            Subscription subscription = Subscription.retrieve(subscriptionId);
            subscription.update(params);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la mise à jour des métadonnées Stripe", e);
        }
    }

    /**
     * Méthode pour créer les paramètres par défaut d'une boutique
     */
    private void createDefaultParameters(Integer customId, HttpSession session) {
        List<Parametre> parametres = Arrays.asList(
                new Parametre(customId, "isHTML_mail", "1", "HTML activé pour l'envoi de mail"),
                new Parametre(customId, "Subject_mail", "Commande Praticboutic", "Sujet du courriel pour l'envoi de mail"),
                new Parametre(customId, "VALIDATION_SMS", (String) session.getAttribute("confboutic_validsms"), "Commande validée par sms ?"),
                new Parametre(customId, "VerifCP", "0", "Activation de la verification des codes postaux"),
                new Parametre(customId, "Choix_Paiement", (String) session.getAttribute("confboutic_chxpaie"), "COMPTANT ou LIVRAISON ou TOUS"),
                new Parametre(customId, "MP_Comptant", "Par carte bancaire", "Texte du paiement comptant"),
                new Parametre(customId, "MP_Livraison", "Moyens conventionnels", "Texte du paiement à la livraison"),
                new Parametre(customId, "Choix_Method", (String) session.getAttribute("confboutic_chxmethode"), "TOUS ou EMPORTER ou LIVRER"),
                new Parametre(customId, "CM_Livrer", "Vente avec livraison", "Texte de la vente à la livraison"),
                new Parametre(customId, "CM_Emporter", "Vente avec passage à la caisse", "Texte de la vente à emporter"),
                new Parametre(customId, "MntCmdMini", (String) session.getAttribute("confboutic_mntmincmd"), "Montant commande minimal"),
                new Parametre(customId, "SIZE_IMG", "smallimg", "bigimg ou smallimg"),
                new Parametre(customId, "CMPT_CMD", "0", "Compteur des références des commandes"),
                new Parametre(customId, "MONEY_SYSTEM", "STRIPE MARKETPLACE", ""),
                new Parametre(customId, "STRIPE_ACCOUNT_ID", "", "ID Compte connecté Stripe"),
                new Parametre(customId, "NEW_ORDER", "0", "Nombre de nouvelle(s) commande(s)")
        );

        parametreRepository.saveAll(parametres);
    }

    /**
     * Méthode pour créer les statuts de commande par défaut
     */
    private void createDefaultOrderStatuses(Integer customId) {
        List<StatutCmd> statuts = Arrays.asList(
                new StatutCmd(customId, "Commande à faire", "#E2001A",
                        "Bonjour, votre commande à été transmise. %boutic% vous remercie et vous tiendra informé de son avancé. ", true, true),
                new StatutCmd(customId, "En cours de préparation", "#EB690B",
                        "Votre commande est en cours de préparation. ", false, true),
                new StatutCmd(customId, "En cours de livraison", "#E2007A",
                        "Votre commande est en cours de livraison, ", false, true),
                new StatutCmd(customId, "Commande à disposition", "#009EE0",
                        "Votre commande est à disposition", false, true),
                new StatutCmd(customId, "Commande terminée", "#009036",
                        "%boutic% vous remercie pour votre commande. À très bientôt. ", false, true),
                new StatutCmd(customId, "Commande anulée", "#1A171B",
                        "Nous ne pouvons donner suite à votre commande. Pour plus d'informations, merci de nous contacter. ", false, true)
        );

        statutCmdRepository.saveAll(statuts);
    }

}
