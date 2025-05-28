package com.ecommerce.praticboutic_backend_java.controllers;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionCollection;
import com.stripe.param.SubscriptionListParams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class StripeWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(StripeWebhookController.class);

    @Value("${stripe.secret.key}")
    private String stripeApiKey;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        // Initialize Stripe configuration

        Stripe.apiKey = stripeApiKey;


        // For sample support and debugging (not required for production)
        Stripe.setAppInfo(
                "pratic-boutic/registration",
                "0.0.2",
                "https://praticboutic.fr"
        );
    }

    @PostMapping("/stripe")
    public ResponseEntity<?> handleStripeWebhook(@RequestBody String payload) {
        try {
            // Parse JSON
            Event event = Event.GSON.fromJson(payload, Event.class);

            // On récupère les infos directement du payload comme en PHP
            if ("customer.subscription.deleted".equals(event.getType()) ||
                    "customer.subscription.created".equals(event.getType())) {

                Optional<?> objectOptional = event.getDataObjectDeserializer().getObject();

                if (objectOptional.isPresent() && objectOptional.get() instanceof Subscription) {
                    Subscription subscription = (Subscription) objectOptional.get();
                    updateCustomerSubscription(subscription);
                } else {
                    logger.warn("Impossible de désérialiser l'objet du webhook pour l'événement : {}", event.getType());
                }
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            logger.error("Erreur traitement webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    private void updateCustomerSubscription(Subscription subscription) {
        try {
            String customerId = subscription.getCustomer();

            // Check active subscriptions
            SubscriptionListParams params = SubscriptionListParams.builder()
                    .setCustomer(customerId)
                    .setStatus(SubscriptionListParams.Status.ACTIVE)
                    .build();

            SubscriptionCollection subscriptions = Subscription.list(params);
            Integer bouticId;
            try
            {

                // Get the boutic ID from the database
                bouticId = jdbcTemplate.queryForObject(
                        "SELECT customer.customid FROM customer, client WHERE client.stripe_customer_id = ? AND customer.cltid = client.cltid",
                        Integer.class,
                        customerId
                );
            }
            catch (EmptyResultDataAccessException e) {
                bouticId = null;
            }

            if (bouticId == null) {
                logger.warn("No customer found for Stripe customer ID: {}", customerId);
                return;
            }

            // Update customer active status based on subscription status
            boolean hasActiveSubscription = !subscriptions.getData().isEmpty();
            String query = "UPDATE customer SET actif = ? WHERE customid = ?";

            int updated = jdbcTemplate.update(query, hasActiveSubscription ? 1 : 0, bouticId);

            if (updated == 0) {
                logger.warn("No customer record updated for bouticId: {}", bouticId);
            }

        } catch (Exception e) {
            logger.error("Error updating subscription for customer: {}", subscription.getId(), e);
        }
    }
}
