package com.ecommerce.praticboutic_backend_java.services;


import com.ecommerce.praticboutic_backend_java.configurations.StripeConfig;

import com.ecommerce.praticboutic_backend_java.exceptions.DatabaseException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import com.stripe.param.SubscriptionUpdateParams;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Service pour l'intégration avec Stripe
 */
@Service
public class StripeService {

    // Déclarez le logger en tant que champ statique en haut de votre classe
    private static final Logger logger = LoggerFactory.getLogger(StripeService.class);

    private final StripeConfig stripeConfig;

    @Autowired
    public StripeService(StripeConfig stripeConfig) {
        this.stripeConfig = stripeConfig;
        Stripe.apiKey = stripeConfig.getSecretKey();
        Stripe.setAppInfo(
                "pratic-boutic",
                "1.0.0",
                "https://praticboutic.fr"
        );
    }

    /**
     * Met à jour les métadonnées d'un abonnement Stripe
     */
    public void updateSubscriptionMetadata(String subscriptionId, Map<String, String> metadata) throws StripeException {
        try {
            SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                    .setMetadata(metadata)
                    .build();

            Subscription subscription = Subscription.retrieve(subscriptionId);
            subscription.update(params);

            logger.debug("Métadonnées Stripe mises à jour pour l'abonnement: {}", subscriptionId);
        } catch (StripeException e) {
            logger.error("Erreur Stripe lors de la mise à jour des métadonnées: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Mise à jour des métadonnées Stripe pour un abonnement
     */
    public void updateStripeSubscriptionMetadata(String subscriptionId, Integer abonnementId) throws StripeException {
        if (StringUtils.isEmpty(subscriptionId)) {
            throw new DatabaseException.InvalidSessionDataException("L'ID d'abonnement Stripe ne peut pas être vide");
        }

        try {
            Map<String, String> metadata = new HashMap<>();
            metadata.put("pbabonumref", "ABOPB" + StringUtils.leftPad(abonnementId.toString(), 10, "0"));
            metadata.put("abonnement_id", abonnementId.toString());
            metadata.put("creation_date", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            updateSubscriptionMetadata(subscriptionId, metadata);
            logger.debug("Métadonnées Stripe mises à jour pour l'abonnement: {}", subscriptionId);
        } catch (StripeException e) {
            logger.error("Erreur lors de la mise à jour des métadonnées Stripe", e);
            throw e;
        }
    }
}