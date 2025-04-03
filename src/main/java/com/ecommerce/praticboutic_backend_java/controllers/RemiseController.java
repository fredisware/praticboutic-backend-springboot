package com.ecommerce.praticboutic_backend_java.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RemiseController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${session.max.lifetime}")
    private int sessionMaxLifetime;

    static class RemiseRequest {
        public String sessionid;
        public String customer;
        public double sstotal;
        public String code;
    }

    @PostMapping("/calcul-remise")
    public ResponseEntity<?> calculateRemise(@RequestBody RemiseRequest input,
                                             HttpSession session) {
        try {
            // Vérification de la session
            Long lastActivity = (Long) session.getAttribute("last_activity");
            if (lastActivity == null ||
                    (Instant.now().getEpochSecond() - lastActivity > sessionMaxLifetime)) {
                throw new RuntimeException("Session expirée");
            }

            // Mise à jour du timestamp de dernière activité
            session.setAttribute("last_activity", Instant.now().getEpochSecond());

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

            // Récupération du taux de promotion
            Double taux = jdbcTemplate.queryForObject(
                    "SELECT taux FROM promotion WHERE customid = ? AND BINARY code = ? AND actif = 1",
                    Double.class,
                    bouticId,
                    input.code
            );

            // Calcul de la remise
            double remise = 0.0;
            if (taux != null) {
                remise = input.sstotal * (taux / 100.0);
            }

            return ResponseEntity.ok(remise);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error);
        }
    }
}
