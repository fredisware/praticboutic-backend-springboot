package com.ecommerce.praticboutic_backend_java.controllers;

import com.ecommerce.praticboutic_backend_java.entities.Client;
import com.ecommerce.praticboutic_backend_java.entities.Customer;
import com.ecommerce.praticboutic_backend_java.services.JwtService;
import com.ecommerce.praticboutic_backend_java.services.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.lang.Integer.parseInt;

public class NotifPushController {

    private NotificationService notificationService;

    // Déclarez le logger en tant que champ statique en haut de votre classe
    private static final Logger logger = LoggerFactory.getLogger(NotifPushController.class);

    @PostMapping("/send-push-notif")
    public ResponseEntity<?> creerDepartCommande(@RequestBody Map<String, Object> input, @RequestHeader("Authorization") String authHeader) {
        Customer customerInfo;
        try {
            logger.info("==== Début de traitement /send-push-notif ====");
            logger.info("Données reçues : {}", input);
            String token = authHeader.replace("Bearer ", "");
            Map <java.lang.String, java.lang.Object> payload = JwtService.parseToken(token).getClaims();

            String device_id = input.get("deviceid").toString();
            String subject = input.get("subject").toString();
            String msg = input.get("msg").toString();

            logger.info("Envoi de notification push au device : {}", device_id);
            notificationService.sendPushNotification(device_id, subject, msg);

            logger.info("==== Fin de traitement /send-push-notif ====");
            String jwt = JwtService.generateToken(payload, "" );
            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            return ResponseEntity.ok(response);


        } catch (Exception e) {
            logger.error("Erreur lors de la création de la commande : {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }

    }
}
