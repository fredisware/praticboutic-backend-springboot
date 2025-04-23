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
import java.util.Map;

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
            return ResponseEntity.ok(Map.of("result", result));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
}