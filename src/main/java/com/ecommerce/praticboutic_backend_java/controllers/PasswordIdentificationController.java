package com.ecommerce.praticboutic_backend_java.controllers;

import com.ecommerce.praticboutic_backend_java.requests.LoginRequest;
import com.ecommerce.praticboutic_backend_java.responses.LoginResponse;
import com.ecommerce.praticboutic_backend_java.services.SessionService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.SubscriptionCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PasswordIdentificationController {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private SessionService sessionService;
    
    @Value("${stripe.secret.key}")
    private String stripeSecretKey;
    
    @Value("${login.max.retry}")
    private int maxRetry;
    
    @Value("${login.retry.interval}")
    private String retryInterval;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            // Vérifier si la session est active
            String sessionId = request.getSessionid();
            if (sessionId != null && !sessionService.isSessionValid(sessionId)) {
                return createErrorResponse("Session expirée", HttpStatus.UNAUTHORIZED);
            }
            
            // Vérifier les tentatives de connexion
            String ip = httpRequest.getRemoteAddr();
            int attemptCount = countLoginAttempts(ip);
            
            if (attemptCount >= maxRetry) {
                return createErrorResponse("Vous êtes autorisé à " + maxRetry + " tentatives en " + retryInterval, 
                                          HttpStatus.TOO_MANY_REQUESTS);
            }
            
            // Vérifier les identifiants
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                "SELECT c.pass, cu.customid, cu.customer, c.stripe_customer_id " +
                "FROM client c, customer cu " +
                "WHERE c.email = ? AND c.cltid = cu.cltid LIMIT 1", 
                request.getEmail());
            
            if (results.isEmpty()) {
                incrementLoginAttempts(ip);
                return createErrorResponse("Mauvais identifiant ou mot de passe !", HttpStatus.UNAUTHORIZED);
            }
            
            Map<String, Object> userData = results.get(0);
            String hashedPassword = (String) userData.get("pass");
            
            // Vérifier le mot de passe
            if (!verifyPassword(request.getPassword(), hashedPassword)) {
                incrementLoginAttempts(ip);
                return createErrorResponse("Mauvais identifiant ou mot de passe !", HttpStatus.UNAUTHORIZED);
            }

            // Créer ou mettre à jour la session
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("last_activity", System.currentTimeMillis() / 1000);
            sessionData.put("bo_stripe_customer_id", userData.get("stripe_customer_id"));
            sessionData.put("bo_id", userData.get("customid"));
            sessionData.put("bo_email", request.getEmail());
            sessionData.put("bo_auth", "oui");
            sessionData.put("bo_init", "non");
            sessionData.put("active", 1);
            
            // Mettre à jour la session ou en créer une nouvelle
            sessionService.updateSession(sessionData);
            
            // Vérifier l'abonnement Stripe
            String stripeCustomerId = (String) userData.get("stripe_customer_id");
            String subscriptionStatus = checkStripeSubscription(stripeCustomerId);
            sessionData.put("bo_abo", subscriptionStatus.equals("OK") ? "oui" : "non");
            
            // Créer la réponse
            LoginResponse response = new LoginResponse();
            response.setCustomerId( (Integer) userData.get("customid"));
            response.setCustomerName((String) userData.get("customer"));
            response.setStripeCustomerId(stripeCustomerId);
            response.setSubscriptionStatus(subscriptionStatus);
            response.setSessionId(sessionId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return createErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    private int countLoginAttempts(String ip) {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM connexion WHERE ip = ? AND ts > (NOW() - INTERVAL " + retryInterval + ")",
            Integer.class, ip);
    }
    
    private void incrementLoginAttempts(String ip) {
        jdbcTemplate.update(
            "INSERT INTO connexion (ip, ts) VALUES (?, CURRENT_TIMESTAMP)",
            ip);
    }
    
    private boolean verifyPassword(String plainPassword, String hashedPassword) {
        return org.springframework.security.crypto.bcrypt.BCrypt.checkpw(plainPassword, hashedPassword);
    }
    
    private String checkStripeSubscription(String customerId) {
        try {
            Stripe.apiKey = stripeSecretKey;
            Map<String, Object> params = new HashMap<>();
            params.put("customer", customerId);
            params.put("status", "active");
            
            SubscriptionCollection subscriptions = com.stripe.model.Subscription.list(params);
            
            return subscriptions.getData().size() > 0 ? "OK" : "KO";
        } catch (StripeException e) {
            return "KO";
        }
    }
    
    private ResponseEntity<Map<String, String>> createErrorResponse(String message, HttpStatus status) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return new ResponseEntity<>(response, status);
    }
}