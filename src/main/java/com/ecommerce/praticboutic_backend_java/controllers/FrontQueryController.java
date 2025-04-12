package com.ecommerce.praticboutic_backend_java.controllers;

import com.ecommerce.praticboutic_backend_java.services.*;
import com.ecommerce.praticboutic_backend_java.requests.*;
import com.ecommerce.praticboutic_backend_java.exceptions.SessionExpiredException;
import com.ecommerce.praticboutic_backend_java.configurations.StripeConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import com.stripe.model.SubscriptionCollection;
import com.stripe.param.SubscriptionListParams;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequestMapping("/api")
public class FrontQueryController {

    @Autowired
    private CategorieService categorieService;
    
    @Autowired
    private ArticleService articleService;
    
    @Autowired
    private RelGrpOptArtService relGrpOptArtService;
    
    @Autowired
    private OptionService optionService;
    
    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private ClientService clientService;
    
    @Autowired
    private ImageService imageService;
    
    @Autowired
    private AbonnementService abonnementService;
    
    @Autowired
    private ParameterService paramService;
    
    @Autowired
    private StripeConfig stripeConfig;
    
    @Value("${session.max.lifetime}")
    private int maxLifetime;


    @PostMapping("/front")
    public ResponseEntity<?> handleRequest(@RequestBody FrontQueryRequest input,
                                          HttpServletRequest request,
                                          HttpServletResponse response) {
        
        try {
            HttpSession session = request.getSession();
            
            // Gestion de la session personnalisée si un sessionId est fourni
            if (input.getSessionid() != null) {
                // Dans un environnement réel, vous devrez implémenter une gestion de session personnalisée
                // car Spring gère automatiquement les sessions
            }
            
            if ("initSession".equals(input.getRequete())) {
                session.setAttribute("last_activity", System.currentTimeMillis());
            }

            // Vérification de l'expiration de la session
            verifierExpirationSession(session);
            
            List<?> result;
            
            // Traitement des différentes requêtes
            switch (input.getRequete()) {
                case "categories":
                    result = categorieService.getCategories(input.getBouticid());
                    break;
                    
                case "articles":
                    result = articleService.getArticles(input.getBouticid(), input.getCatid());
                    break;
                    
                case "groupesoptions":
                    result = relGrpOptArtService.getGroupesOptions(input.getBouticid(), input.getArtid());
                    break;
                    
                case "options":
                    result = optionService.getOptions(input.getGrpoptid());
                    break;
                    
                case "getBouticInfo":
                    result = customerService.getBouticInfo(input.getCustomer());
                    break;
                    
                case "getClientInfo":
                    result = clientService.getClientInfo(input.getCustomer());
                    break;
                    
                case "images":
                    result = imageService.getImages(input.getBouticid(), input.getArtid());
                    break;
                    
                case "aboactif":
                    result = getAbonnementsActifs(input.getBouticid());
                    break;
                    
                case "initSession":
                    result = initSession(session, input);
                    break;
                    
                case "getSession":
                    result = getSession(session);
                    break;
                    
                case "getparam":
                    result = paramService.getParam(input.getParam(), input.getBouticid());
                    break;
                    
                default:
                    return new ResponseEntity<>("Requête non supportée", HttpStatus.BAD_REQUEST);
            }
            
            return new ResponseEntity<>(result, HttpStatus.OK);
            
        } catch (SessionExpiredException e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    private void verifierExpirationSession(HttpSession session) throws SessionExpiredException {
        Long lastActivity = (Long) session.getAttribute("last_activity");
        
        if (lastActivity == null) {
            throw new SessionExpiredException("Session expirée");
        }
        
        if (System.currentTimeMillis() - lastActivity > maxLifetime * 1000) {
            // La session a expiré
            throw new SessionExpiredException("Session expirée");
        } else {
            // La session est toujours active, met à jour le timestamp de la dernière activité
            session.setAttribute("last_activity", System.currentTimeMillis());
        }
    }
    
    private List<?> initSession(HttpSession session, FrontQueryRequest input) {
        session.setAttribute("customer", input.getCustomer());
        session.setAttribute(input.getCustomer() + "_mail", "non");
        session.setAttribute("method", input.getMethod() != null ? input.getMethod() : "3");
        session.setAttribute("table", input.getTable() != null ? input.getTable() : "0");
        
        return Collections.singletonList("OK");
    }
    
    private List<String> getSession(HttpSession session) {
        String customer = (String) session.getAttribute("customer");
        String mail = (String) session.getAttribute(customer + "_mail");
        String method = (String) session.getAttribute("method");
        String table = (String) session.getAttribute("table");
        
        return Arrays.asList(customer, mail, method, table);
    }
    
    private List<?> getAbonnementsActifs(Integer bouticid) throws StripeException {
        String stripeCustomerId = abonnementService.getStripeCustomerId(bouticid);
        
        if (stripeCustomerId == null || stripeCustomerId.isEmpty()) {
            throw new RuntimeException("Impossible de récupérer l'identifiant Stripe de la boutic");
        }
        
        // Configuration Stripe
        Stripe.apiKey = stripeConfig.getSecretKey();
        
        SubscriptionListParams params = SubscriptionListParams.builder()
            .setCustomer(stripeCustomerId)
            .setStatus(SubscriptionListParams.Status.ACTIVE)
            .build();
            
        SubscriptionCollection subscriptions = Subscription.list(params);
        return subscriptions.getData();
    }
}