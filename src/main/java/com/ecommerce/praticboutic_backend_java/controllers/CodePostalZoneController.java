package com.ecommerce.praticboutic_backend_java.controllers;

import com.ecommerce.praticboutic_backend_java.requests.CpZoneRequest;
import com.ecommerce.praticboutic_backend_java.services.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/api")
public class CodePostalZoneController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${session.max.lifetime}")
    private Long sessionMaxLifetime;

    @PostMapping("/check-codepostal")
    public ResponseEntity<?> checkCpZone(@RequestBody CpZoneRequest request) {
        try {
            // Vérifier si une session ID a été fournie et la définir
            if (request.getSessionId() != null && !request.getSessionId().isEmpty()) {
                sessionService.setSessionId(request.getSessionId());
            }

            // Vérifier si la session est active
            if (!sessionService.hasAttribute("last_activity")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Session expirée"));
            }

            // Vérifier si la session a expiré
            Long lastActivity = (Long) sessionService.getAttribute("last_activity");
            if (Duration.between(Instant.ofEpochSecond(lastActivity), Instant.now())
                    .getSeconds() > sessionMaxLifetime) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Session expirée"));
            }

            // Mettre à jour l'horodatage de la dernière activité
            sessionService.setAttribute("last_activity", Instant.now().getEpochSecond());

            // Vérifier si customer est défini dans la session
            if (!sessionService.hasAttribute("customer")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Pas de boutic"));
            }

            String customer = (String) sessionService.getAttribute("customer");
            String method = (String) sessionService.getAttribute("method");
            String table = (String) sessionService.getAttribute("table");

            // Vérifier si le courriel est défini
            String mailKey = customer + "_mail";
            if (!sessionService.hasAttribute(mailKey)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Pas de courriel"));
            }

            // Vérifier si le courriel a déjà été envoyé
            if ("oui".equals(sessionService.getAttribute(mailKey))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Courriel déjà envoyé"));
            }

            // Récupérer customer de la requête et échapper les caractères spéciaux
            String requestCustomer = request.getCustomer();

            // Obtenir l'ID du client
            Integer customid = jdbcTemplate.queryForObject(
                "SELECT customid FROM customer WHERE customer = ?", 
                Integer.class, 
                requestCustomer
            );

            if (customid == null) {
                return ResponseEntity.ok("ko");
            }

            // Vérifier si le code postal est dans une zone active pour ce client
            String cp = request.getCp();
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cpzone WHERE customid = ? AND codepostal = ? AND actif = 1",
                Integer.class,
                customid,
                cp
            );

            String result = (count != null && count > 0) ? "ok" : "ko";

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // Classe pour représenter les réponses d'erreur
    private static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}