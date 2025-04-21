package com.ecommerce.praticboutic_backend_java.controllers;

import com.ecommerce.praticboutic_backend_java.entities.Client;
import com.ecommerce.praticboutic_backend_java.repositories.ClientRepository;
import com.ecommerce.praticboutic_backend_java.repositories.CustomerRepository;
import com.ecommerce.praticboutic_backend_java.requests.LoginLinkRequest;
import com.ecommerce.praticboutic_backend_java.services.ParameterService;
import com.ecommerce.praticboutic_backend_java.services.SessionService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.model.LoginLink;
import com.stripe.model.SubscriptionCollection;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.stripe.param.LoginLinkCreateOnAccountParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class LoginLinkController {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${app.protocol:http}")
    private String protocol;
    
    @Value("${app.version:0.0.2}")
    private String appVersion;

    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private SessionService sessionService;
    
    @Autowired
    private ParameterService paramService;

    @Autowired
    private ClientRepository clientRepository;

    @PostMapping("/login-link")
    public ResponseEntity<?> createLoginLink(@RequestBody LoginLinkRequest request) {
        try {
            // Vérifier si la session est valide
            if (!sessionService.isSessionValid(request.getSessionid())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Session expirée"));
            }
            
            // Vérifier l'authentification
            if (!sessionService.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Non authentifié"));
            }

            // Configurer Stripe
            Stripe.apiKey = stripeSecretKey;
            Stripe.setAppInfo(
                "pratic-boutic/registration",
                appVersion,
                "https://praticboutic.fr"
            );
            
            // Récupérer l'ID client Stripe
            String userEmail = sessionService.getUserEmail();
            Optional<Client> client = clientRepository.findByEmailAndActif(userEmail, 1);
            if (client.isEmpty())
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Pas de client avec courriel " + userEmail));

            String stripeCustomerId = client.get().getStripeCustomerId();
            if (stripeCustomerId == null || stripeCustomerId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Id compte stripe client manquant"));
            }

            // Vérifier que le client a un abonnement actif
            Map<String, Object> subscriptionParams = new HashMap<>();
            subscriptionParams.put("customer", stripeCustomerId);
            subscriptionParams.put("status", "active");
            SubscriptionCollection subscriptions = com.stripe.model.Subscription.list(subscriptionParams);
            
            if (subscriptions.getData().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Pas d'abonnement actif"));
            }
            
            // Récupérer ou créer un compte Stripe Connect
            String stripeAccountId = paramService.getValeur("STRIPE_ACCOUNT_ID", request.getBouticid());
            String url;

            if (stripeAccountId != null && !stripeAccountId.isEmpty()) {
                // Le compte existe déjà
                Account account = Account.retrieve(stripeAccountId);
                
                if (account.getDetailsSubmitted()) {
                    LoginLinkCreateOnAccountParams params = LoginLinkCreateOnAccountParams.builder().build();
                    LoginLink loginLink = LoginLink.createOnAccount(stripeAccountId, params);
                    url = loginLink.getUrl();
                } else {
                    // Compte incomplet, créer un lien d'onboarding
                    url = createInscription(request.getSessionid(), request.getBouticid(), request.getPlatform());
                }
            } else {
                // Aucun compte, créer un nouveau
                url = createInscription(request.getSessionid(), request.getBouticid(), request.getPlatform());
            }
            
            return ResponseEntity.ok(Map.of("result", url));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
    
    private String createInscription(String sessionId, Integer bouticId, String platform) throws StripeException {
        String serverName = getServerName();
        
        // Créer un compte Stripe Connect Express
        AccountCreateParams accountParams = AccountCreateParams.builder()
                .setType(AccountCreateParams.Type.EXPRESS)
                .setCountry("FR")
                .setEmail(sessionService.getUserEmail())
                .setCapabilities(
                        AccountCreateParams.Capabilities.builder()
                                .setCardPayments(AccountCreateParams.Capabilities.CardPayments.builder().setRequested(true).build())
                                .setTransfers(AccountCreateParams.Capabilities.Transfers.builder().setRequested(true).build())
                                .build()
                )
                .build();
                
        Account account = Account.create(accountParams);
        
        // Enregistrer l'ID du compte Stripe
        paramService.setValeur("STRIPE_ACCOUNT_ID", account.getId(), bouticId);
        
        // Stocker l'ID de boutique dans la session
        sessionService.setBoId(bouticId);
        
        // Créer un lien d'onboarding
        String refreshUrl = protocol + "://" + serverName + "/api/redirect-handler?sessionid=" + sessionId + "&platform=" + platform;
        String returnUrl = protocol + "://" + serverName + "/api/redirect-handler?sessionid=" + sessionId + "&platform=" + platform;
        
        AccountLinkCreateParams linkParams = AccountLinkCreateParams.builder()
                .setAccount(account.getId())
                .setRefreshUrl(refreshUrl)
                .setReturnUrl(returnUrl)
                .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                .build();
                
        AccountLink accountLink = AccountLink.create(linkParams);
        
        return accountLink.getUrl();
    }
    
    private String getServerName() {
        // Dans un environnement Spring, vous pouvez obtenir cette information autrement
        // Ceci est une implémentation simple
        return "praticboutic.fr";
    }
}