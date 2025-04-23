package com.ecommerce.praticboutic_backend_java.controllers;

import com.ecommerce.praticboutic_backend_java.services.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LogoutController {

    @Autowired
    private SessionService sessionService;

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession currentSession) {
        try {
            // Réinitialisation des attributs de session (équivalent à ce qui est fait dans le PHP)
            sessionService.setAttribute("active", 0);
            sessionService.setAttribute("last_activity", Instant.now().getEpochSecond());
            sessionService.setAttribute("bo_stripe_customer_id", "");
            sessionService.setAttribute("bo_id", 0);
            sessionService.setAttribute("bo_email", "");
            sessionService.setAttribute("bo_auth", "non");
            sessionService.setAttribute("bo_init", "oui");

            // Option alternative: invalider complètement la session
            // currentSession.invalidate();

            Map<String, String> response = new HashMap<>();
            response.put("status", "OK");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}