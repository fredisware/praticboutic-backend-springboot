package com.ecommerce.praticboutic_backend_java.controllers;

import com.ecommerce.praticboutic_backend_java.entities.Customer;
import com.ecommerce.praticboutic_backend_java.services.CustomerService;
import com.ecommerce.praticboutic_backend_java.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequestMapping("/api")
public class GeneralQueryController {

    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private ClientService clientService;

    @PostMapping("/genquery")
    public ResponseEntity<?> processGenQuery(@RequestBody(required = false) Map<String, Object> input, HttpSession session) {
        try {
            // Gestion de la session
            if (input.containsKey("sessionid")) {
                // Dans Spring, la session est gérée automatiquement
                // Nous n'avons pas besoin de définir manuellement l'ID de session
                // Cette partie est donc simplifiée par rapport au code PHP
            }
            

            // Vérification de l'expiration de la session (commenté comme dans le code PHP)
            /*long maxLifetime = session.getMaxInactiveInterval();
            if (session.getAttribute("last_activity") != null) {
                long lastActivity = (long) session.getAttribute("last_activity");
                if (System.currentTimeMillis() - lastActivity > maxLifetime * 1000) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Session expirée"));
                } else {
                    session.setAttribute("last_activity", System.currentTimeMillis());
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Session expirée"));
            }*/

            
            String action = (String) input.get("action");
            
            if ("listcustomer".equals(action)) {
                List<List<Object>> result = new ArrayList<>();
                
                // Récupération des clients actifs avec leurs informations Stripe
                List<Customer> customers = customerService.findActiveCustomersWithStripeInfo();

                return ResponseEntity.ok(customers);
            }
            
            return ResponseEntity.ok(new ArrayList<>());
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}