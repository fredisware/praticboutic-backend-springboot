package com.ecommerce.praticboutic_backend_java.services;

import com.ecommerce.praticboutic_backend_java.entities.Parametre;
import com.ecommerce.praticboutic_backend_java.repositories.ParametreRepository;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
public class ParameterService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ParametreRepository parametreRepository;

    // Déclarez le logger en tant que champ statique en haut de votre classe
    private static final Logger logger = LoggerFactory.getLogger(ParameterService.class);

    public String getParameterValue(String paramName, Integer bouticId) {
        String sql = "SELECT valeur FROM parametre WHERE nom = ? AND customid = ?";
        try {
            return jdbcTemplate.queryForObject(sql, String.class, paramName, bouticId);
        } catch (Exception e) {
            return "";
        }
    }

    public String getValeur(String paramName, Integer bouticId) {
        // Implémentation pour récupérer la valeur d'un paramètre
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT valeur FROM parametre WHERE nom = ? AND customid = ?")) {

            stmt.setString(1, paramName);
            stmt.setInt(2, bouticId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("valeur");
                }
                return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    public void setValeur(String paramName, String paramValue, Integer bouticId) {
        // Implémentation pour définir la valeur d'un paramètre
        try (Connection conn = dataSource.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM parametres WHERE nomParam = ? AND bouticid = ?");
             PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO parametres (nomParam, valeur, bouticid) VALUES (?, ?, ?)");
             PreparedStatement updateStmt = conn.prepareStatement("UPDATE parametres SET valeur = ? WHERE nomParam = ? AND bouticid = ?")) {

            checkStmt.setString(1, paramName);
            checkStmt.setInt(2, bouticId);

            try (ResultSet rs = checkStmt.executeQuery()) {
                rs.next();
                int count = rs.getInt(1);

                if (count == 0) {
                    // Insert
                    insertStmt.setString(1, paramName);
                    insertStmt.setString(2, paramValue);
                    insertStmt.setInt(3, bouticId);
                    insertStmt.executeUpdate();
                } else {
                    // Update
                    updateStmt.setString(1, paramValue);
                    updateStmt.setString(2, paramName);
                    updateStmt.setInt(3, bouticId);
                    updateStmt.executeUpdate();
                }
            }
        } catch (Exception e) {
            // Gestion des erreurs
        }
    }


    public String getValeurParam(String param, int bouticId, String defaultValue) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT valeur FROM parametre WHERE nom = ? AND customid = ?";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, param);
                stmt.setInt(2, bouticId);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("valeur");
                    }
                }
            }
        }

        return defaultValue;
    }

    public boolean setValeurParam(String param, int bouticId, String valeur) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            String query = "UPDATE parametre SET valeur = ? WHERE nom = ? AND customid = ?";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, valeur);
                stmt.setString(2, param);
                stmt.setInt(3, bouticId);

                int rowsUpdated = stmt.executeUpdate();
                return rowsUpdated > 0; // Renvoie vrai si au moins une ligne a été mise à jour
            }
        }
    }

    public List<?> getParam(String param, Integer bouticid) {
        return null;
    }

    /**
     * Méthode pour créer les paramètres par défaut d'une boutique
     */
    public void createDefaultParameters(Integer customId, HttpSession session) {

        List<Parametre> parametres = Arrays.asList(
                new Parametre(customId, "isHTML_mail", "1", "HTML activé pour l'envoi de mail"),
                new Parametre(customId, "Subject_mail", "Commande Praticboutic", "Sujet du courriel pour l'envoi de mail"),
                new Parametre(customId, "VALIDATION_SMS", sessionService.getSessionAttributeAsString(session, "confboutic_validsms"), "Commande validée par sms ?"),
                new Parametre(customId, "VerifCP", "0", "Activation de la verification des codes postaux"),
                new Parametre(customId, "Choix_Paiement", sessionService.getSessionAttributeAsString(session, "confboutic_chxpaie"), "COMPTANT ou LIVRAISON ou TOUS"),
                new Parametre(customId, "MP_Comptant", "Par carte bancaire", "Texte du paiement comptant"),
                new Parametre(customId, "MP_Livraison", "Moyens conventionnels", "Texte du paiement à la livraison"),
                new Parametre(customId, "Choix_Method", sessionService.getSessionAttributeAsString(session, "confboutic_chxmethode"), "TOUS ou EMPORTER ou LIVRER"),
                new Parametre(customId, "CM_Livrer", "Vente avec livraison", "Texte de la vente à la livraison"),
                new Parametre(customId, "CM_Emporter", "Vente avec passage à la caisse", "Texte de la vente à emporter"),
                new Parametre(customId, "MntCmdMini", sessionService.getSessionAttributeAsString(session, "confboutic_mntmincmd"), "Montant commande minimal"),
                new Parametre(customId, "SIZE_IMG", "smallimg", "bigimg ou smallimg"),
                new Parametre(customId, "CMPT_CMD", "0", "Compteur des références des commandes"),
                new Parametre(customId, "MONEY_SYSTEM", "STRIPE MARKETPLACE", ""),
                new Parametre(customId, "STRIPE_ACCOUNT_ID", "", "ID Compte connecté Stripe"),
                new Parametre(customId, "NEW_ORDER", "0", "Nombre de nouvelle(s) commande(s)"),
                new Parametre(customId, "DATE_CREATION", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), "Date de création")
        );

        try {
            parametreRepository.saveAll(parametres);
            logger.debug("Paramètres par défaut créés pour la boutique: {}", customId);
        } catch (DataAccessException e) {
            logger.error("Erreur lors de la création des paramètres par défaut", e);
            throw e;
        }
    }
}