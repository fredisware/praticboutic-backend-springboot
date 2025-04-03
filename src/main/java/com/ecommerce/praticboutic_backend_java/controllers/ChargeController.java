package com.ecommerce.praticboutic_backend_java.controllers;

import com.ecommerce.praticboutic_backend_java.requests.ChargeRequest;
import com.ecommerce.praticboutic_backend_java.services.ParameterService;
import com.ecommerce.praticboutic_backend_java.services.SessionService;
import com.stripe.Stripe;
import com.stripe.model.Account;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.Duration;

@RestController
@RequestMapping("/api")
public class ChargeController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private ParameterService parameterService;

    @Value("${stripe.secret.key}")
    private String stripeApiKey;

    @Value("${session.max.lifetime}")
    private Long sessionMaxLifetime;

    @PostMapping("/check-stripe-account")
    public ResponseEntity<?> checkStripeAccount(@RequestBody ChargeRequest request, HttpSession session) {
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

            // Vérifier l'authentification
            if (!sessionService.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Non authentifié"));
            }

            // Récupérer l'ID de boutique
            Integer bouticId = request.getBouticId();
            if (bouticId == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("ID de boutique manquant"));
            }

            // Récupérer l'ID du compte Stripe
            String stripeAccountId = parameterService.getParameterValue("STRIPE_ACCOUNT_ID", bouticId);
            if (stripeAccountId == null || stripeAccountId.isEmpty()) {
                return ResponseEntity.ok("KO");
            }

            // Configurer Stripe
            Stripe.apiKey = stripeApiKey;

            // Vérifier si le compte Stripe est activé pour les paiements
            Account account = Account.retrieve(stripeAccountId);
            String result = account.getChargesEnabled() ? "OK" : "KO";

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // Classe pour représenter les réponses d'erreur
    private static class ErrorResponse {
        private String error;

        public ErrorResponse(String error) {
            this.error = error;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}