package com.ecommerce.praticboutic_backend_java.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class ParameterService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

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
            String query = "SELECT valeur FROM param WHERE nom = ? AND customid = ?";

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

    public List<?> getParam(String param, Integer bouticid) {
        return null;
    }
}