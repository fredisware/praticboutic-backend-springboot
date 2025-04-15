package com.ecommerce.praticboutic_backend_java.controllers;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class SessionController {
	
    @PostMapping("/session-marche")
    public ResponseEntity<Map<String, Object>> handleSession(@RequestBody(required = false) Map<String, Object> input, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Gérer l'identifiant de session fourni dans l'entrée JSON
            if (input != null && input.containsKey("sessionid")) {
                session.setAttribute("sessionid", input.get("sessionid").toString());
            }

            // Stocker l'activité récente dans la session
            session.setAttribute("last_activity", System.currentTimeMillis());

            // Construire une réponse avec l'ID de la session
            //List<String> sessionData = new ArrayList<>();
            //sessionData.add(session.getId());  // Ajoute l'ID de la session active
            response.put("sessionid", session.getId());

        } catch (Exception e) {
            // Gestion des erreurs
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }

        // Retourner une réponse JSON
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/active-session")
    public ResponseEntity<Map<String, Object>> handleActsess(@RequestBody(required = false) Map<String, Object> input, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Vérifie et met à jour l'identifiant de session si fourni
            if (input != null && input.containsKey("sessionid")) {
                session.setAttribute("sessionid", input.get("sessionid").toString());
            }

            // Met à jour l'heure de l'activité récente
            session.setAttribute("last_activity", System.currentTimeMillis());

            // Vérifie l'état "active" de la session et prépare la réponse
            if (session.getAttribute("active") != null && session.getAttribute("active").equals(1)) {
                response.put("status", "OK");
                response.put("email", session.getAttribute("bo_email"));
            } else {
                response.put("status", "KO");
                response.put("email", "");
            }

        } catch (Exception e) {
            // Retourne une erreur en cas de problème
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }

        // Retourne une réponse JSON avec les informations de la session
        return ResponseEntity.ok(response);
    }

    @PostMapping("/exit")
    public ResponseEntity<Map<String, String>> createSession(@RequestBody(required = false) Map<String, Object> input,
                                                             HttpSession session) {
        try {
            // Si une session ID est fournie dans l'input, on pourrait la récupérer ici
            // Mais Spring Boot gère les sessions différemment de PHP
            // Donc cette partie est plutôt informative
            if (input != null && input.containsKey("sessionid")) {
                // Note: Spring n'offre pas directement la possibilité de définir l'ID de session
                // On pourrait utiliser d'autres mécanismes pour maintenir cette logique
                String sessionId = (String) input.get("sessionid");
                // Logique pour gérer l'ID de session si nécessaire
            }

            // Initialisation des attributs de session
            session.setAttribute("active", 0);
            session.setAttribute("last_activity", System.currentTimeMillis() / 1000);
            session.setAttribute("bo_stripe_customer_id", "");
            session.setAttribute("bo_id", 0);
            session.setAttribute("bo_email", "");
            session.setAttribute("bo_auth", "non");
            session.setAttribute("bo_init", "oui");

            Map<String, String> output = new HashMap<>();
            output.put("status", "OK");
            return ResponseEntity.ok(output);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    

}
