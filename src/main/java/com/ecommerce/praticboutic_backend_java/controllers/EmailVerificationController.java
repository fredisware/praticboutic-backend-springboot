package com.ecommerce.praticboutic_backend_java.controllers;

import com.ecommerce.praticboutic_backend_java.requests.EmailVerificationRequest;
import com.ecommerce.praticboutic_backend_java.responses.ErrorResponse;
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
public class EmailVerificationController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${session.max.lifetime}")
    private Long sessionMaxLifetime;

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody EmailVerificationRequest request) {
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

            // Vérifier si l'email existe déjà dans la base de données
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM client c WHERE c.email = ?",
                    Integer.class,
                    request.getEmail()
            );

            String result;
            if (count != null && count == 0) {
                // L'email n'existe pas encore, on peut l'utiliser
                sessionService.setAttribute("verify_email", request.getEmail());
                result = "OK";
            } else {
                // L'email existe déjà
                result = "KO";
            }
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
}