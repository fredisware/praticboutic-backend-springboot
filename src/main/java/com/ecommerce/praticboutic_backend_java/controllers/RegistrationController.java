package com.ecommerce.praticboutic_backend_java.controllers;

import com.ecommerce.praticboutic_backend_java.requests.RegistrationRequest;
import com.stripe.Stripe;
import com.stripe.model.Customer;
import com.stripe.param.CustomerCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RegistrationController {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${session.max.lifetime}")
    private int sessionMaxLifetime;



    @PostMapping("/registration")
    public ResponseEntity<?> registerMobile(@RequestBody RegistrationRequest input,
                                            HttpSession session) {
        try {
            // Vérification de la session
            Long lastActivity = (Long) session.getAttribute("last_activity");
            if (lastActivity == null ||
                    (Instant.now().getEpochSecond() - lastActivity > sessionMaxLifetime)) {
                throw new Exception("Session expirée");
            }

            // Mise à jour du timestamp de dernière activité
            session.setAttribute("last_activity", Instant.now().getEpochSecond());

            // Vérification de l'email
            String verifyEmail = (String) session.getAttribute("verify_email");
            if (verifyEmail == null || verifyEmail.isEmpty()) {
                throw new Exception("Courriel non vérifié");
            }

            // Enregistrement des données dans la session
            session.setAttribute("registration_pass", input.pass);
            session.setAttribute("registration_qualite", input.qualite);
            session.setAttribute("registration_nom", input.nom);
            session.setAttribute("registration_prenom", input.prenom);
            session.setAttribute("registration_adr1", input.adr1);
            session.setAttribute("registration_adr2", input.adr2);
            session.setAttribute("registration_cp", input.cp);
            session.setAttribute("registration_ville", input.ville);
            session.setAttribute("registration_tel", input.tel);

            // Configuration Stripe
            Stripe.apiKey = stripeSecretKey;

            // Création du client Stripe
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setAddress(
                            CustomerCreateParams.Address.builder()
                                    .setCity(input.ville)
                                    .setCountry("FRANCE")
                                    .setLine1(input.adr1)
                                    .setLine2(input.adr2)
                                    .setPostalCode(input.cp)
                                    .build()
                    )
                    .setEmail(verifyEmail)
                    .setName(input.nom)
                    .setPhone(input.tel)
                    .build();

            Customer customer = Customer.create(params);
            session.setAttribute("registration_stripe_customer_id", customer.getId());

            return ResponseEntity.ok(Map.of("result","OK"));

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(error);
        }
    }
}