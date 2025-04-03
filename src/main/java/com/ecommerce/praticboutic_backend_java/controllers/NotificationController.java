package com.ecommerce.praticboutic_backend_java.controllers;

import com.ecommerce.praticboutic_backend_java.services.EmailSendingService;
import com.ecommerce.praticboutic_backend_java.services.NotificationService;

import com.ecommerce.praticboutic_backend_java.services.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", 
    allowedHeaders = {"Content-Type", "Authorization", "Accept", "Accept-Language", "X-Authorization"}, 
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.PATCH})
public class NotificationController {

    @Autowired
    private SessionService sessionService;
    
    @Autowired
    private EmailSendingService emailService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Value("${app.email.sender.address}")
    private String emailSender;
    
    @Value("${sms.sender}")
    private String smsSender;
    
    @Value("${sms.token}")
    private String smsToken;
    
    @Value("${session.max.lifetime}")
    private Long sessionMaxLifetime;

    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody NotificationRequest request) {
        try {
            // Vérifier si une session ID a été fournie et la définir
            if (request.getSessionId() != null && !request.getSessionId().isEmpty()) {
                sessionService.setSessionId(request.getSessionId());
            }

            // Vérifier si la session est active
            if (!sessionService.hasAttribute("last_activity")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Session expirée"));
            }

            // Vérifier si la session a expiré
            Long lastActivity = (Long) sessionService.getAttribute("last_activity");
            if (Duration.between(Instant.ofEpochSecond(lastActivity), Instant.now())
                    .getSeconds() > sessionMaxLifetime) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("Session expirée"));
            }

            // Mettre à jour l'horodatage de la dernière activité
            sessionService.setAttribute("last_activity", Instant.now().getEpochSecond());

            // Récupérer les informations de la commande
            Map<String, Object> orderDetails = getOrderDetails(request.getOrderId(), request.getCustomerId());
            
            // Si demandé, envoyer un e-mail de notification
            if (Boolean.TRUE.equals(request.getSendEmail())) {
                sendEmailNotification(orderDetails);
            }
            
            // Si demandé, envoyer une notification push
            if (Boolean.TRUE.equals(request.getSendPush()) && request.getDeviceId() != null) {
                sendPushNotification(orderDetails, request.getDeviceId(), request.getDeviceType());
            }
            
            // Si demandé, envoyer un SMS
            if (Boolean.TRUE.equals(request.getSendSms())) {
                sendSmsNotification(orderDetails);
            }
            
            // Mettre à jour les informations de facturation Stripe si nécessaire
            if (orderDetails != null && orderDetails.containsKey("customid")) {
                updateStripeUsage(orderDetails);
            }

            // Marquer l'email comme envoyé dans la session
            String customerKey = orderDetails.get("customid") + "_mail";
            sessionService.setAttribute(customerKey, "oui");
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "OK");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    private Map<String, Object> getOrderDetails(Long orderId, Long customerId) {
        String query = "SELECT commande.*, statutcmd.message, statutcmd.etat, customer.nom AS boutic_nom " +
                "FROM commande " +
                "INNER JOIN statutcmd ON commande.statid = statutcmd.statid " +
                "INNER JOIN customer ON commande.customid = statutcmd.customid " +
                "WHERE statutcmd.defaut = 1 AND commande.cmdid = ? " +
                "AND commande.customid = ? AND statutcmd.customid = ? " +
                "AND customer.customid = ? ORDER BY commande.cmdid LIMIT 1";

        return jdbcTemplate.queryForMap(query, orderId, customerId, customerId, customerId);
    }

    private void sendEmailNotification(Map<String, Object> orderDetails) {
        // Construire le contenu de l'e-mail en remplaçant les variables
        String emailTemplate = getEmailTemplate((Long) orderDetails.get("customid"));
        String content = replaceTemplateVariables(emailTemplate, orderDetails);
        
        String recipientEmail = (String) orderDetails.get("email");
        String subject = "Commande #" + orderDetails.get("numref");
        
        emailService.sendEmail(emailSender, recipientEmail, subject, content);
    }
    
    private void sendPushNotification(Map<String, Object> orderDetails, String deviceId, Integer deviceType) {
        String title = "Commande #" + orderDetails.get("numref");
        String body = "Votre commande a été " + orderDetails.get("etat");
        String icon = (String) orderDetails.getOrDefault("icon", "");
        
        String ret = notificationService.sendPushNotification(deviceId, title, body, null);
    }
    
    private void sendSmsNotification(Map<String, Object> orderDetails) {
        String phoneNumber = (String) orderDetails.get("telephone");
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return;
        }
        
        String messageTemplate = (String) orderDetails.get("message");
        String content = replaceTemplateVariables(messageTemplate, orderDetails);
        
        notificationService.sendSms(phoneNumber, content, smsSender, smsToken);
    }
    
    private void updateStripeUsage(Map<String, Object> orderDetails) {
        Long customerId = (Long) orderDetails.get("customid");
        Double total = (Double) orderDetails.get("total");
        Double remise = (Double) orderDetails.getOrDefault("remise", 0.0);
        Double fraisLivraison = (Double) orderDetails.getOrDefault("fraislivraison", 0.0);
        
        Double usageQuantity = total - remise + fraisLivraison;
        
        // Récupérer l'abonnement Stripe
        String query = "SELECT aboid, stripe_subscription_id FROM abonnement WHERE bouticid = ?";
        jdbcTemplate.query(query, (rs) -> {
            String subscriptionId = rs.getString("stripe_subscription_id");
            try {
                notificationService.reportStripeUsage(subscriptionId, usageQuantity.intValue(), UUID.randomUUID().toString());
            } catch (Exception e) {
                // Log error but continue processing
                System.err.println("Error reporting Stripe usage: " + e.getMessage());
            }
        }, customerId);
    }
    
    private String getEmailTemplate(Long customerId) {
        String query = "SELECT template FROM email_templates WHERE customid = ? LIMIT 1";
        return jdbcTemplate.queryForObject(query, String.class, customerId);
    }
    
    private String replaceTemplateVariables(String template, Map<String, Object> data) {
        if (template == null) {
            return "";
        }
        
        // Remplacer toutes les variables de modèle
        String content = template;
        content = content.replace("%boutic%", String.valueOf(data.getOrDefault("boutic_nom", "")));
        content = content.replace("%telephone%", String.valueOf(data.getOrDefault("telephone", "")));
        content = content.replace("%numref%", String.valueOf(data.getOrDefault("numref", "")));
        content = content.replace("%nom%", String.valueOf(data.getOrDefault("nom", "")));
        content = content.replace("%prenom%", String.valueOf(data.getOrDefault("prenom", "")));
        content = content.replace("%adresse1%", String.valueOf(data.getOrDefault("adresse1", "")));
        content = content.replace("%adresse2%", String.valueOf(data.getOrDefault("adresse2", "")));
        content = content.replace("%codepostal%", String.valueOf(data.getOrDefault("codepostal", "")));
        content = content.replace("%ville%", String.valueOf(data.getOrDefault("ville", "")));
        content = content.replace("%vente%", String.valueOf(data.getOrDefault("vente", "")));
        content = content.replace("%paiement%", String.valueOf(data.getOrDefault("paiement", "")));
        
        // Formater correctement les montants
        Double sstotal = (Double) data.getOrDefault("sstotal", 0.0);
        Double fraisLivraison = (Double) data.getOrDefault("fraislivraison", 0.0);
        Double total = (Double) data.getOrDefault("total", 0.0);
        
        content = content.replace("%sstotal%", String.format("%.2f", sstotal).replace(".", ","));
        content = content.replace("%fraislivraison%", String.format("%.2f", fraisLivraison).replace(".", ","));
        content = content.replace("%total%", String.format("%.2f", total).replace(".", ","));
        
        content = content.replace("%commentaire%", String.valueOf(data.getOrDefault("commentaire", "")));
        content = content.replace("%etat%", String.valueOf(data.getOrDefault("etat", "")));
        
        return content;
    }

    private Map<String, String> createErrorResponse(String errorMessage) {
        Map<String, String> error = new HashMap<>();
        error.put("error", errorMessage);
        return error;
    }
    
    // Classe pour la requête
    public static class NotificationRequest {
        private String sessionId;
        private Long orderId;
        private Long customerId;
        private Boolean sendEmail;
        private Boolean sendSms;
        private Boolean sendPush;
        private String deviceId;
        private Integer deviceType;

        // Getters et setters
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        
        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        
        public Boolean getSendEmail() { return sendEmail; }
        public void setSendEmail(Boolean sendEmail) { this.sendEmail = sendEmail; }
        
        public Boolean getSendSms() { return sendSms; }
        public void setSendSms(Boolean sendSms) { this.sendSms = sendSms; }
        
        public Boolean getSendPush() { return sendPush; }
        public void setSendPush(Boolean sendPush) { this.sendPush = sendPush; }
        
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        
        public Integer getDeviceType() { return deviceType; }
        public void setDeviceType(Integer deviceType) { this.deviceType = deviceType; }
    }
}