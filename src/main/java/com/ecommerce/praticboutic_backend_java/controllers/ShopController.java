package com.ecommerce.praticboutic_backend_java.controllers;

import com.ecommerce.praticboutic_backend_java.configurations.DatabaseConfig;
import com.ecommerce.praticboutic_backend_java.requests.BouticRequest;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ShopController {

    @Autowired
    private DatabaseConfig dbConfig;


    private final JdbcTemplate jdbcTemplate;

    private final List<String> FORBIDDEN_IDS = Arrays.asList("admin", "common", "route", "upload", "vendor");

    public ShopController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @PostMapping("/register-boutic")
    public ResponseEntity<?> checkAliasAvailability(@RequestBody BouticRequest request, HttpSession session) {
        try {
            // Vérifier si la session est expirée
            /*Long lastActivity = (Long) session.getAttribute("last_activity");
            int maxLifetime = session.getMaxInactiveInterval();

            if (lastActivity == null || (System.currentTimeMillis() / 1000 - lastActivity) > maxLifetime) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","Session expirée"));
            } else {
                // Mise à jour du timestamp de la dernière activité
                session.setAttribute("last_activity", System.currentTimeMillis() / 1000);
            }*/

            // Vérifier si l'email est vérifié
            String verifyEmail = (String)session.getAttribute("verify_email");
            if (verifyEmail == null || verifyEmail.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","Courriel non vérifié"));
            }

            // Charger les variables d'environnement
            //Dotenv dotenv = Dotenv.configure().directory(".").load();

            // Valider l'alias
            if (request.getAliasboutic() == null || request.getAliasboutic().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error","Identifiant vide"));
            }

            if (FORBIDDEN_IDS.contains(request.getAliasboutic())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error","Identifiant interdit"));
            }

            // Vérifier si l'alias est déjà utilisé
            String sql = "SELECT count(*) FROM customer cu WHERE cu.customer = ? LIMIT 1";

            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, request.getAliasboutic());

            if (count != null && count > 0) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error","Alias de boutic déjà utilisé"));
            }

            // Enregistrer les informations dans la session
            session.setAttribute("initboutic_aliasboutic", request.getAliasboutic());
            session.setAttribute("initboutic_nom", request.getNom());
            session.setAttribute("initboutic_logo", request.getLogo());
            session.setAttribute("initboutic_email", request.getEmail());

            // Commenté pour correspondre au code PHP commenté
            // session.setAttribute("STRIPE_ACCOUNT_ID", "");

            return ResponseEntity.ok(Map.of("result","OK"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur: " + e.getMessage());
        }
    }
}