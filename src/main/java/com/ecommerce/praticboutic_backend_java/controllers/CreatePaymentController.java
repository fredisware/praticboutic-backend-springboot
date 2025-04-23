package com.ecommerce.praticboutic_backend_java.controllers;

import com.ecommerce.praticboutic_backend_java.requests.CreatePaymentRequest;

import com.ecommerce.praticboutic_backend_java.requests.Item;
import com.ecommerce.praticboutic_backend_java.responses.CreatePaymentResponse;
import com.ecommerce.praticboutic_backend_java.services.SessionService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CreatePaymentController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${session.max.lifetime}")
    private Long sessionMaxLifetime;

    @PostMapping("/create")
    public ResponseEntity<?> createPaymentIntent(@RequestBody CreatePaymentRequest request) {
        try {
            // Vérifier si customer est défini dans la session
            if (!sessionService.hasAttribute("customer")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Pas de boutic"));
            }

            String customer = (String) sessionService.getAttribute("customer");
            String method = (String) sessionService.getAttribute("method");
            String table = (String) sessionService.getAttribute("table");

            // Vérifier si le courriel est défini
            String mailKey = customer + "_mail";
            if (!sessionService.hasAttribute(mailKey)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Pas de courriel"));
            }

            // Vérifier si le courriel a déjà été envoyé
            if ("oui".equals(sessionService.getAttribute(mailKey))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Courriel déjà envoyé"));
            }

            // Récupérer le nom de la boutique depuis la requête
            String boutic = request.getBoutic();
            Integer customid;
            try {
                // Obtenir l'ID du client
                customid = jdbcTemplate.queryForObject(
                        "SELECT customid FROM customer WHERE customer = ?",
                        Integer.class,
                        boutic
                );
            }
            catch (EmptyResultDataAccessException e) {
                customid = null;
            }

            if (customid == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Boutique non trouvée"));
            }

            // Récupérer le compte Stripe connecté
            String stripeAccountId = getValeurParam("STRIPE_ACCOUNT_ID", customid);

            // Initialiser Stripe
            Stripe.apiKey = stripeSecretKey;

            // Calculer le montant de la commande
            int amount = calculateOrderAmount(
                    request.getItems(),
                    customid,
                    request.getModel(),
                    request.getFraislivr(),
                    request.getCodepromo()
            );

            // Créer l'intention de paiement Stripe
            PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                    .setAmount((long) amount)
                    .setCurrency("eur")
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    );

            // Configurer les options de requête pour le compte connecté
            RequestOptions requestOptions = RequestOptions.builder()
                    .setStripeAccount(stripeAccountId)
                    .build();

            // Créer l'intention de paiement
            PaymentIntent paymentIntent = PaymentIntent.create(paramsBuilder.build(), requestOptions);

            // Renvoyer la clé client
            CreatePaymentResponse response = new CreatePaymentResponse(paymentIntent.getClientSecret());
            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new ErrorResponse("Erreur Stripe: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    private int calculateOrderAmount(List<Item> items, Integer customid, String model, Double fraisLivr, String codePromo) throws Exception {
        if (items == null || items.isEmpty()) {
            throw new Exception("Panier Vide");
        }

        double price = 0.0;

        // Calcul du coût des lignes de commandes
        for (Item item : items) {
            String id = item.getId();
            String type = item.getType();
            Double prixServeur;

            if ("article".equals(type)) {
                prixServeur = jdbcTemplate.queryForObject(
                        "SELECT prix FROM article WHERE customid = ? AND artid = ?",
                        Double.class,
                        customid,
                        id
                );
            } else if ("option".equals(type)) {
                prixServeur = jdbcTemplate.queryForObject(
                        "SELECT surcout FROM `option` WHERE customid = ? AND optid = ?",
                        Double.class,
                        customid,
                        id
                );
            } else {
                throw new Exception("Type d'article inconnu");
            }

            if (Math.abs(prixServeur - item.getPrix()) < 0.001) {
                price += item.getPrix() * item.getQt();
            } else {
                throw new Exception("Prix invalide");
            }
        }

        double surcout = 0.0;

        // Calcul du coût de livraison
        if ("LIVRER".equals(model)) {
            Double fraisCalcules = jdbcTemplate.queryForObject(
                    "SELECT surcout FROM barlivr WHERE customid = ? AND valminin <= ? " +
                            "AND (valmaxex > ? OR valminin >= valmaxex) AND actif = 1",
                    Double.class,
                    customid,
                    price,
                    price
            );

            // Si pas de frais trouvés, on utilise 0
            surcout = (fraisCalcules != null) ? fraisCalcules : 0.0;

            if (Math.abs(surcout - fraisLivr) > 0.001) {
                throw new Exception("Erreur Frais de livraison");
            }
        }

        // Recherche du code promo
        Double taux = 0.0;
        if (codePromo != null && !codePromo.isEmpty()) {
            Double tauxPromo = 0.0;
            try {
                 tauxPromo = jdbcTemplate.queryForObject(
                    "SELECT taux FROM promotion WHERE customid = ? AND code = ? AND actif = 1",
                    Double.class,
                    customid,
                    codePromo
                 );
            }
            catch (EmptyResultDataAccessException e) {
                tauxPromo = 0.0;
            }
            taux = (tauxPromo != null) ? tauxPromo : 0.0;
        }

        double remise = price * -(taux / 100);
        double total = price + remise + surcout;

        // Conversion en centimes pour Stripe
        return (int) Math.round(total * 100);
    }

    private String getValeurParam(String paramName, Integer customid) {
        return jdbcTemplate.queryForObject(
                "SELECT valeur FROM parametre WHERE nom = ? AND customid = ?",
                String.class,
                paramName,
                customid
        );
    }

    // Classe interne pour les réponses d'erreur
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