package com.ecommerce.praticboutic_backend_java.controllers;



import com.ecommerce.praticboutic_backend_java.requests.ShopConfigRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ShopSettingsController {

    @PostMapping("/boutic-configure")
    public ResponseEntity<?> configureShop(@RequestBody ShopConfigRequest request, HttpSession session) {
        try {
            // Vérifier si une session ID est fournie
            if (request.getSessionid() != null && !request.getSessionid().isEmpty()) {
                // Dans Spring Boot, changer l'ID de session n'est pas recommandé
                // Cette partie devrait être gérée différemment dans une implémentation réelle
            }

            // Vérifier si la session est expirée
            Long lastActivity = (Long) session.getAttribute("last_activity");
            int maxLifetime = session.getMaxInactiveInterval();

            if (lastActivity == null || (System.currentTimeMillis() / 1000 - lastActivity) > maxLifetime) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","Session expirée"));
            } else {
                // Mise à jour du timestamp de la dernière activité
                session.setAttribute("last_activity", System.currentTimeMillis() / 1000);
            }

            // Vérifier si l'email est vérifié
            String verifyEmail = (String)session.getAttribute("verify_email");
            if (verifyEmail == null || verifyEmail.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","Courriel non vérifié"));
            }

            // Enregistrer les configurations dans la session
            session.setAttribute("confboutic_chxmethode", request.getChxmethode());
            session.setAttribute("confboutic_chxpaie", request.getChxpaie());
            session.setAttribute("confboutic_mntmincmd", request.getMntmincmd());
            session.setAttribute("confboutic_validsms", request.getValidsms());

            return ResponseEntity.ok(Map.of("result","OK"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error","Erreur: " + e.getMessage()));
        }
    }
}