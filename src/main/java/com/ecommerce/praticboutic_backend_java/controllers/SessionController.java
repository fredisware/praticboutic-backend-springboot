package com.ecommerce.praticboutic_backend_java.controllers;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class SessionController {
	
    @PostMapping("/upsession")
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
    
    @PostMapping("/actsession")
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
    
    

}
