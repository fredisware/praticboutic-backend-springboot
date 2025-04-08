package com.ecommerce.praticboutic_backend_java.controllers;


import com.ecommerce.praticboutic_backend_java.requests.SMSRequest;
import com.ecommerce.praticboutic_backend_java.services.ParameterService;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SMSController {

    @Autowired
    private ParameterService paramService;

    @PostMapping("/send-sms")
    public ResponseEntity<?> sendSMS(@RequestBody SMSRequest request) {
        try {
            // Charger les variables d'environnement
            Dotenv dotenv = Dotenv.configure().directory(".").load();
            String dbUrl = dotenv.get("DB_URL");
            String dbUsername = dotenv.get("DB_USERNAME");
            String dbPassword = dotenv.get("DB_PASSWORD");
            String dbName = dotenv.get("DB_NAME");
            String smsToken = dotenv.get("TOKEN_SMS");
            String smsSender = dotenv.get("SENDER_SMS");

            // Établir une connexion à la base de données
            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://" + dbUrl + "/" + dbName,
                    dbUsername,
                    dbPassword)) {

                Integer customid = (Integer) request.getBouticid();

                // Récupérer les paramètres
                String validSms = paramService.getValeurParam("VALIDATION_SMS",  customid, "0" );
                String receiverNom = paramService.getValeurParam("Receivernom_mail", customid, "Ma PraticBoutic" );

                // Vérifier si l'envoi de SMS est activé
                if ("1".equals(validSms)) {
                    String content = request.getMessage();
                    String phoneNumber = request.getTelephone();

                    // Préparer la requête pour SMSFactor
                    RestTemplate restTemplate = new RestTemplate();
                    String apiUrl = "https://api.smsfactor.com/send";

                    // Construire le corps de la requête
                    Map<String, Object> smsBody = new HashMap<>();
                    Map<String, Object> messageMap = new HashMap<>();
                    messageMap.put("text", content);
                    messageMap.put("sender", smsSender);

                    List<Map<String, String>> recipients = new ArrayList<>();
                    Map<String, String> recipient = new HashMap<>();
                    recipient.put("value", phoneNumber);
                    recipients.add(recipient);

                    Map<String, List<Map<String, String>>> recipientsMap = new HashMap<>();
                    recipientsMap.put("gsm", recipients);

                    Map<String, Object> smsMap = new HashMap<>();
                    smsMap.put("message", messageMap);
                    smsMap.put("recipients", recipientsMap);

                    smsBody.put("sms", smsMap);

                    // Configurer les en-têtes HTTP
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.set("Accept", "application/json");
                    headers.set("Authorization", "Bearer " + smsToken);

                    // Envoyer la requête
                    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(smsBody, headers);
                    ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
                }
            }

            return ResponseEntity.ok("SMS OK");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur: " + e.getMessage());
        }
    }
}
