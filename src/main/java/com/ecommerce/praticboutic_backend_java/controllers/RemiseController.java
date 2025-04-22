package com.ecommerce.praticboutic_backend_java.controllers;

import com.ecommerce.praticboutic_backend_java.requests.RemiseRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RemiseController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${session.max.lifetime}")
    private int sessionMaxLifetime;

    @PostMapping("/calcul-remise")
    public ResponseEntity<?> calculateRemise(@RequestBody RemiseRequest input,
                                             HttpSession session) {
        try {
            // Vérification de la session
            /*Long lastActivity = (Long) session.getAttribute("last_activity");
            if (lastActivity == null ||
                    (Instant.now().getEpochSecond() - lastActivity > sessionMaxLifetime)) {
                throw new RuntimeException("Session expirée");
            }

            // Mise à jour du timestamp de dernière activité
            session.setAttribute("last_activity", Instant.now().getEpochSecond());*/

            // Vérification des données de session
            String customer = (String) session.getAttribute("customer");
            if (customer == null || customer.isEmpty()) {
                throw new RuntimeException("Pas de boutic");
            }

            String customerMail = (String) session.getAttribute(customer + "_mail");
            if (customerMail == null || customerMail.isEmpty()) {
                throw new RuntimeException("Pas de courriel");
            }

            if ("oui".equals(customerMail)) {
                throw new RuntimeException("Courriel déjà envoyé");
            }

            // Sécurisation des entrées
            String sanitizedCustomer = input.customer.replaceAll("[^a-zA-Z0-9]", "");

            // Récupération du bouticid
            Integer bouticId = jdbcTemplate.queryForObject(
                    "SELECT customid FROM customer WHERE customer = ?",
                    Integer.class,
                    sanitizedCustomer
            );

            if (bouticId == null) {
                throw new RuntimeException("Customer non trouvé");
            }

            List<Double> tauxList = jdbcTemplate.query(
                    "SELECT taux FROM promotion WHERE customid = ? AND BINARY code = ? AND actif = 1",
                    (rs, rowNum) -> rs.getDouble("taux"),
                    bouticId,
                    input.code
            );

            Double taux = tauxList.isEmpty() ? 0.0 : tauxList.get(0);

            // Calcul de la remise
            double remise = 0.0;
            if (taux != null) {
                remise = input.sstotal * (taux / 100.0);
            }

            return ResponseEntity.ok(Map.of("result", remise));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
