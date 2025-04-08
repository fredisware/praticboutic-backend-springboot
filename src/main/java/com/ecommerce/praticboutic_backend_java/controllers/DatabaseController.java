package com.ecommerce.praticboutic_backend_java.controllers;

import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.transaction.Transactional;
import org.hibernate.Session;
import com.ecommerce.praticboutic_backend_java.*;
import com.ecommerce.praticboutic_backend_java.configurations.StripeConfig;
import com.ecommerce.praticboutic_backend_java.entities.*;
import com.ecommerce.praticboutic_backend_java.repositories.*;
import com.ecommerce.praticboutic_backend_java.requests.*;

import java.io.File;
import java.io.StringReader;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.Locale;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.stream.Collectors;
import java.io.FileReader;

import jakarta.servlet.http.HttpSession;
import jakarta.persistence.*;
import jakarta.json.Json;

import com.stripe.Stripe;
import com.stripe.model.Subscription;
import com.stripe.param.SubscriptionUpdateParams;

import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.jdbc.Work;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.*;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.spring6.expression.Fields;

@RestController
@RequestMapping("/api")
public class DatabaseController {
    // Déclarez le logger en tant que champ statique en haut de votre classe
    private static final Logger logger = LoggerFactory.getLogger(DatabaseController.class);

    private final StripeConfig stripeConfig;

    private static SessionFactory sessionFactory = null;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

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

    public DatabaseController(StripeConfig stripeConfig) {
        this.stripeConfig = stripeConfig;
    }

    @GetMapping("/count-elements")
    public Map<String, Object> countElementsInTable(@RequestBody VueTableRequest input) {
        Map<String, Object> response = new HashMap<>();
        try {
            String strTable = input.getTable(); Integer iBouticid = input.getBouticid();
            String strSelcol = input.getSelcol(); Integer iSelid = input.getSelid();
            Integer iLimit = input.getLimit(); Integer iOffset = input.getOffset();
            // Vérification si le nom de la table est fourni
            if (strTable == null || strTable.isEmpty()) {
                response.put("error", "Le nom de la table est vide.");
                return response;
            }
            Class<?> entityClass;
            try {
                sessionFactory = entityManager.getEntityManagerFactory()
                        .unwrap(SessionFactory.class);
                entityClass = BaseEntity.getEntityClassFromTableName(sessionFactory, input.getTable());

            } catch (Throwable ex) {
                throw new ExceptionInInitializerError(ex);
            }
            // Création de la requête avec le EntityManager
            StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(*) FROM ");
            queryBuilder.append(strTable);
            queryBuilder.append(" e WHERE e.customid = :bouticid");
            // Ajout de conditions supplémentaires
            boolean bSel = (strSelcol != null && !strSelcol.isEmpty() && iSelid != null && iSelid > 0);
            if (bSel) queryBuilder.append(" AND e.").append(strSelcol).append(" = :selid");
            // Création de la requête dynamique
            Query query = entityManager.createNativeQuery(queryBuilder.toString());
            // Paramètres de la requête
            query.setParameter("bouticid", iBouticid);
            if (bSel) query.setParameter("selid", iSelid);
            // Exécution de la requête
            Long count = (Long) query.getSingleResult();
            response.put("count", count);
            response.put("entity", strTable);
        } catch (Exception e) {
            response.put("error", "Erreur lors de l'exécution : " + e.getMessage());
        }
        return response;
    }

    @GetMapping("/vue-table")
    public Map<String, Object> vueTable(@RequestBody VueTableRequest input) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Variables d'entrée
            String strTable = input.getTable(); Integer iBouticid = input.getBouticid();
            String strSelcol = input.getSelcol(); Integer iSelid = input.getSelid();
            Integer iLimit = input.getLimit(); Integer iOffset = input.getOffset();
            // Validation des données d'entrée
            if (strTable == null || strTable.isEmpty()) {
                throw new IllegalArgumentException("Le nom de la table est vide.");
            }
            Class<?> entityClass;
            try {
                sessionFactory = entityManager.getEntityManagerFactory()
                        .unwrap(SessionFactory.class);
                entityClass = BaseEntity.getEntityClassFromTableName(sessionFactory, input.getTable());

            } catch (Throwable ex) {
                throw new ExceptionInInitializerError(ex);
            }
            ArrayList<Object> data = new ArrayList<>();
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT ").append(BaseEntity.getPrimaryKeyName(sessionFactory, entityManager, strTable))
                    .append(" FROM `").append(strTable)
                    .append("` WHERE customid = ").append(iBouticid)
                    .append(" LIMIT ").append(iLimit).append(" OFFSET ").append(iOffset);

            if (strSelcol != null && !strSelcol.isEmpty() && iSelid != null) {
                queryBuilder.append(" WHERE ").append(strSelcol).append(" = ").append(iSelid);
            }
            Query query = entityManager.createNativeQuery(queryBuilder.toString());
            for(Object primaryKey : query.getResultList())
            {
                Object entityInstance = entityManager.find(entityClass, primaryKey);
                Object ret = entityClass.getDeclaredMethod("getDisplayData").invoke(entityInstance);
                data.add(ret);
            }
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

    @GetMapping("/fill-option")
    public Map<String, Object> remplirOption(@RequestBody RemplirOptionTableRequest input)
    {
        Map<String, Object> response = new HashMap<>();
        try {
            String strClePrimaire = null;
            // Variables d'entrée
            String tableName = input.getTable(); Long idBoutic = input.getBouticid(); String strColonne = input.getColonne();
            // Validation des données d'entrée
            if (tableName == null || tableName.isEmpty()) {
                response.put("error", "Le nom de la table est vide.");
                return response;
            }
            Class<?> entityClass;
            try {
                sessionFactory = entityManager.getEntityManagerFactory()
                        .unwrap(SessionFactory.class);
                entityClass = BaseEntity.getEntityClassFromTableName(sessionFactory, input.getTable());

            } catch (Throwable ex) {
                throw new ExceptionInInitializerError(ex);
            }
            strClePrimaire = BaseEntity.getPrimaryKeyName(sessionFactory, entityManager, tableName);
            if (strClePrimaire == null) {
                throw new IllegalArgumentException("Aucune clé primaire trouvée pour cette table");
            }
            // Création de la requête SQL
            StringBuilder sbQueryRemplirOption = new StringBuilder("SELECT ")
                    .append(strClePrimaire).append(", ")
                    .append(input.getColonne())
                    .append(" FROM `").append(tableName).append("`")
                    .append(" WHERE customid = ").append(idBoutic)
                    .append(" OR ").append(strClePrimaire).append(" = 0");
            if ("statutcmd".equals(tableName)) {
                sbQueryRemplirOption.append(" AND actif = 1");
            }
            Query queryRemplirOption = entityManager.createNativeQuery(sbQueryRemplirOption.toString(), Object[].class);
            List<?> results = queryRemplirOption.getResultList();
            response.put("results", results);
        }
        catch(Exception e2)
        {
            response.put("error", "Erreur lors de l'exécution : " + e2.getMessage());
        }

        return response;
    }

    /**
     * Insère une nouvelle ligne dans une table spécifiée
     *
     * @param input La requête contenant les informations d'insertion
     * @return Une Map contenant le résultat de l'opération
     */
    @PutMapping("/insert-row")
    @Transactional
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
                sessionFactory = entityManager.getEntityManagerFactory()
                        .unwrap(SessionFactory.class);
                entityClass = BaseEntity.getEntityClassFromTableName(sessionFactory, input.getTable());
            } catch (ClassNotFoundException ex) {
                response.put("error", "L'entité spécifiée n'existe pas : " + input.getTable());
                return response;
            }

            // Vérifier les contraintes d'unicité
            for (ColumnData column : input.getRow()) {
                try {
                    Field field = entityClass.getDeclaredField(column.getNom());
                    Column columnAnnotation = field.getAnnotation(Column.class);
                    if (columnAnnotation != null && columnAnnotation.unique()) {
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
                } catch (NoSuchFieldException e) {
                    // Ignorer les champs qui n'existent pas dans l'entité
                    continue;
                }
            }

            // Préparation des données pour l'insertion
            Map<String, Object> columnValues = new HashMap<>();
            columnValues.put("customid", input.getBouticid());

            for (ColumnData column : input.getRow()) {
                Object value = column.getValeur();
                if ("pass".equals(column.getType())) {
                    value = new BCryptPasswordEncoder().encode(column.getValeur());
                }
                columnValues.put(column.getNom(), value);
            }

            // Utiliser une requête native pour l'insertion
            StringBuilder columns = new StringBuilder("customid");
            StringBuilder placeholders = new StringBuilder("?");
            List<Object> values = new ArrayList<>();
            values.add(input.getBouticid());

            for (ColumnData column : input.getRow()) {
                columns.append(", ").append(column.getNom());
                placeholders.append(", ?");

                Object value = column.getValeur();
                if ("pass".equals(column.getType())) {
                    value = new BCryptPasswordEncoder().encode(column.getValeur());
                }
                values.add(value);
            }

            String insertSql = "INSERT INTO " + input.getTable() + " (" + columns + ") VALUES (" + placeholders + ")";

            Query insertQuery = entityManager.createNativeQuery(insertSql);
            for (int i = 0; i < values.size(); i++) {
                insertQuery.setParameter(i + 1, values.get(i));
            }

            int result = insertQuery.executeUpdate();

            // Récupérer l'ID généré
            Query idQuery = entityManager.createNativeQuery("SELECT LAST_INSERT_ID()");
            Long lastId = ((Number) idQuery.getSingleResult()).longValue();

            response.put("id", lastId);
            response.put("success", result > 0);

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
    @PatchMapping("/update-row")
    @Transactional
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
                sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class);
                entityClass = BaseEntity.getEntityClassFromTableName(sessionFactory, input.getTable());
            } catch (ClassNotFoundException ex) {
                response.put("error", "L'entité spécifiée n'existe pas : " + input.getTable());
                return response;
            }

            // Vérifier les contraintes d'unicité
            for (ColumnData column : input.getRow()) {
                Field field = entityClass.getDeclaredField(column.getNom());
                Column columnAnnotation = field.getAnnotation(Column.class);
                if (columnAnnotation != null && columnAnnotation.unique()) {
                    String jpql = "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e " +
                            "WHERE e.customid = :customid AND e." + column.getNom() +
                            " = :valeur AND e." + input.getColonne() + " != :idtoup";

                    TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
                    query.setParameter("customid", input.getBouticid());
                    query.setParameter("valeur", column.getValeur());
                    query.setParameter("idtoup", input.getIdtoup());

                    Long lCount = query.getSingleResult();
                    if (lCount > 0) {
                        throw new IllegalArgumentException("Impossible d'avoir plusieurs fois la valeur '" +
                                column.getValeur() + "' dans la colonne '" +
                                column.getDesc() + "'");
                    }
                }
            }

            // Préparer et exécuter la mise à jour
            StringBuilder jpql = new StringBuilder("UPDATE " + entityClass.getSimpleName() + " e SET ");

            for (int i = 0; i < input.getRow().size(); i++) {
                ColumnData column = input.getRow().get(i);
                jpql.append("e.").append(column.getNom()).append(" = :").append(column.getNom());

                if (i < input.getRow().size() - 1) {
                    jpql.append(", ");
                }
            }

            jpql.append(" WHERE e.customid = :customid AND e.").append(input.getColonne()).append(" = :idtoup");

            Query updateQuery = entityManager.createQuery(jpql.toString());

            // Set parameters
            for (ColumnData column : input.getRow()) {
                Object value = column.getValeur();
                if ("pass".equals(column.getType())) {
                    value = new BCryptPasswordEncoder().encode(column.getValeur());
                }
                if ("bool".equals(column.getType())) {
                    value = (Boolean) column.getValeur().equals("1");
                }
                updateQuery.setParameter(column.getNom(), value);

            }

            updateQuery.setParameter("customid", input.getBouticid());
            updateQuery.setParameter("idtoup", input.getIdtoup());

            int updatedCount = updateQuery.executeUpdate();
            response.put("success", updatedCount > 0);

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
    @GetMapping("/get-values")
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
            try {
                sessionFactory = new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
            } catch (Throwable ex) {
                throw new ExceptionInInitializerError(ex);
            }
            // Construction dynamique de la classe d'entité
            Class<?> entityClass;
            try {
                entityClass = BaseEntity.getEntityClassFromTableName(sessionFactory, input.getTable());
            } catch (ClassNotFoundException ex) {
                response.put("error", "L'entité spécifiée n'existe pas : " + input.getTable());
                return response;
            }
            String strClePrimaire = BaseEntity.getPrimaryKeyName(sessionFactory, entityManager, input.getTable());
            if (strClePrimaire == null) {
                throw new IllegalArgumentException("Aucune clé primaire trouvée pour cette table");
            }
            StringBuilder sbQuerySelect = new StringBuilder("SELECT ");
            for (Field field : entityClass.getDeclaredFields() ) {
                if ((field.getAnnotation(Column.class) != null) && (!field.getName().equals("customid"))) {
                    if (!field.equals(entityClass.getDeclaredFields()[0])) sbQuerySelect.append(", ");
                    sbQuerySelect.append(field.getName());
                }
            }
            sbQuerySelect.append(" FROM ").append(input.getTable()).append(" WHERE ");
            sbQuerySelect.append(strClePrimaire).append(" = ").append(input.getIdtoup());
            sbQuerySelect.append(" AND ").append("customid = ").append(input.getBouticid());
            Query qSelect = entityManager.createNativeQuery(sbQuerySelect.toString());
            Object result = qSelect.getSingleResult();
            response.put("values", result);
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
    @GetMapping("/color-row")
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

            Query query = entityManager.createQuery(jpql, String.class);
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
    @GetMapping("/get-com-data")
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

            Double sstotal = (Double) row[11];
            Double fraislivraison = (Double) row[12];
            Double total = (Double) row[13];

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
    @GetMapping("/get-custom-prop")
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

    @PostMapping("/set-custom-prop")
    @Transactional
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
                String checkJpql = "SELECT COUNT(c) FROM Customer c WHERE c." + input.getProp() + " = :valeur AND c.customid != :customid";

                Query checkQuery = entityManager.createQuery(checkJpql);
                checkQuery.setParameter("valeur", input.getValeur());
                checkQuery.setParameter("customid", input.getBouticid());

                Long count = (Long) checkQuery.getSingleResult();

                if (count >= 1) {
                    response.put("result", "KO");
                    return ResponseEntity.ok(response);
                }
            }

            // Mise à jour de la propriété - use parameterized query instead of string concatenation
            String updateJpql = "UPDATE Customer c SET c." + input.getProp() + " = :valeur WHERE c.customid = :customid";

            Query updateQuery = entityManager.createQuery(updateJpql);
            updateQuery.setParameter("valeur", input.getValeur());
            updateQuery.setParameter("customid", input.getBouticid());

            int updatedCount = updateQuery.executeUpdate();

            if (updatedCount > 0) {
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
    @GetMapping("/get-client-prop")
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
    @Transactional
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
            String cltIdQuery = "SELECT c.client.cltid FROM Customer c WHERE c.customid = :customid";
            Query query = entityManager.createQuery(cltIdQuery);
            query.setParameter("customid", input.getBouticid());

            Integer cltid;
            try {
                cltid = (Integer) query.getSingleResult();
            } catch (NoResultException e) {
                response.put("error", "Aucun client trouvé pour le customer avec l'ID " + input.getBouticid());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Mise à jour de la propriété
            String updateJpql = "UPDATE Client c SET c." + input.getProp() + " = :valeur WHERE c.cltid = :cltid";

            Query updateQuery = entityManager.createQuery(updateJpql);

            if ("pass".equals(input.getProp()) && !input.getValeur().isEmpty()) {
                // Cas spécial pour le mot de passe : hachage avant stockage
                String hashedPassword = BCrypt.hashpw(input.getValeur(), BCrypt.gensalt());
                updateQuery.setParameter("valeur", hashedPassword);
            } else {
                // Cas général
                updateQuery.setParameter("valeur", input.getValeur());
            }

            updateQuery.setParameter("cltid", cltid);
            int updatedCount = updateQuery.executeUpdate();

            if (updatedCount > 0) {
                response.put("result", "OK");
            } else {
                response.put("result", "KO");
            }

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
    @PutMapping("/build-boutic")
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
    @PatchMapping("/radress-boutic")
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
    private void createDefaultParameters(Integer customid, HttpSession session) {
        List<Parametre> parametres = Arrays.asList(
                new Parametre(customid, "isHTML_mail", "1", "HTML activé pour l'envoi de mail"),
                new Parametre(customid, "Subject_mail", "Commande Praticboutic", "Sujet du courriel pour l'envoi de mail"),
                new Parametre(customid, "VALIDATION_SMS", (String) session.getAttribute("confboutic_validsms"), "Commande validée par sms ?"),
                new Parametre(customid, "VerifCP", "0", "Activation de la verification des codes postaux"),
                new Parametre(customid, "Choix_Paiement", (String) session.getAttribute("confboutic_chxpaie"), "COMPTANT ou LIVRAISON ou TOUS"),
                new Parametre(customid, "MP_Comptant", "Par carte bancaire", "Texte du paiement comptant"),
                new Parametre(customid, "MP_Livraison", "Moyens conventionnels", "Texte du paiement à la livraison"),
                new Parametre(customid, "Choix_Method", (String) session.getAttribute("confboutic_chxmethode"), "TOUS ou EMPORTER ou LIVRER"),
                new Parametre(customid, "CM_Livrer", "Vente avec livraison", "Texte de la vente à la livraison"),
                new Parametre(customid, "CM_Emporter", "Vente avec passage à la caisse", "Texte de la vente à emporter"),
                new Parametre(customid, "MntCmdMini", (String) session.getAttribute("confboutic_mntmincmd"), "Montant commande minimal"),
                new Parametre(customid, "SIZE_IMG", "smallimg", "bigimg ou smallimg"),
                new Parametre(customid, "CMPT_CMD", "0", "Compteur des références des commandes"),
                new Parametre(customid, "MONEY_SYSTEM", "STRIPE MARKETPLACE", ""),
                new Parametre(customid, "STRIPE_ACCOUNT_ID", "", "ID Compte connecté Stripe"),
                new Parametre(customid, "NEW_ORDER", "0", "Nombre de nouvelle(s) commande(s)")
        );

        parametreRepository.saveAll(parametres);
    }

    /**
     * Méthode pour créer les statuts de commande par défaut
     */
    private void createDefaultOrderStatuses(Integer customid) {
        List<StatutCmd> statuts = Arrays.asList(
                new StatutCmd(customid, "Commande à faire", "#E2001A",
                        "Bonjour, votre commande à été transmise. %boutic% vous remercie et vous tiendra informé de son avancé. ", true, true),
                new StatutCmd(customid, "En cours de préparation", "#EB690B",
                        "Votre commande est en cours de préparation. ", false, true),
                new StatutCmd(customid, "En cours de livraison", "#E2007A",
                        "Votre commande est en cours de livraison, ", false, true),
                new StatutCmd(customid, "Commande à disposition", "#009EE0",
                        "Votre commande est à disposition", false, true),
                new StatutCmd(customid, "Commande terminée", "#009036",
                        "%boutic% vous remercie pour votre commande. À très bientôt. ", false, true),
                new StatutCmd(customid, "Commande anulée", "#1A171B",
                        "Nous ne pouvons donner suite à votre commande. Pour plus d'informations, merci de nous contacter. ", false, true)
        );

        statutCmdRepository.saveAll(statuts);
    }

}
