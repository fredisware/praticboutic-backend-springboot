package com.ecommerce.praticboutic_backend_java.controllers;

import com.ecommerce.praticboutic_backend_java.entities.Client;
import com.ecommerce.praticboutic_backend_java.entities.Customer;
import com.ecommerce.praticboutic_backend_java.exceptions.SessionExpiredException;
import com.ecommerce.praticboutic_backend_java.repositories.ClientRepository;
import com.ecommerce.praticboutic_backend_java.repositories.CustomerRepository;
import com.ecommerce.praticboutic_backend_java.requests.DepartCommandeRequest;
import com.ecommerce.praticboutic_backend_java.services.*;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.Integer.parseInt;


@RestController
@RequestMapping("/api")
public class DepartCommandeController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private ParameterService paramService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private NotificationService notificationService;

    @Value("${application.url}")
    private String applicationUrl;

    @Value("${mail.sender.address}")
    private String sendmail;

    @Autowired
    private DepartCommandeService departCommandeService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private StripeService stripeService;

    @Autowired
    private SmsService smsService;

    // Déclarez le logger en tant que champ statique en haut de votre classe
    private static final Logger logger = LoggerFactory.getLogger(DepartCommandeController.class);

    @PostMapping("/depart-commande")
    public ResponseEntity<List<String>> creerDepartCommande(@RequestBody Map<String, Object> input, HttpSession session) {
        Customer customerInfo;
        try {
            // Check if session is active
            if (session.getAttribute("last_activity") != null) {
                long lastActivity = (Long) session.getAttribute("last_activity");
                int maxLifetime = session.getMaxInactiveInterval();
                if (System.currentTimeMillis() / 1000 - lastActivity > maxLifetime) {
                    throw new SessionExpiredException("Session expired");
                } else {
                    // Update last activity timestamp
                    session.setAttribute("last_activity", System.currentTimeMillis() / 1000);
                }
            } else {
                throw new SessionExpiredException("Session expired");
            }
            // Check if customer exists in session
            String customer = (String) session.getAttribute("customer");
            if (customer == null || customer.isEmpty()) {
                throw new IllegalStateException("No boutic");
            }
            String method = (String) session.getAttribute("method");
            String table = (String) session.getAttribute("table");
            // Check if email already sent
            String emailStatus = (String) session.getAttribute(customer + "_mail");
            if (emailStatus == null) {
                throw new IllegalStateException("No email");
            }
            if ("oui".equals(emailStatus)) {
                throw new IllegalStateException("Email already sent");
            }
            // Get customer information
            customerInfo = customerRepository.findByCustomer(customer);
            if (customerInfo == null) {
                throw new IllegalStateException("Could not find customer info");
            }
            // Get client device information
            Optional<Client> clientInfo = clientRepository.findClientById(customerInfo.getCltid());
            System.out.println("Client retourné : " + clientInfo); // Pour voir si l'objet est null ou vide
            System.out.println("ID recherché : " + customerInfo.getCltid());
            if (clientInfo.isEmpty()) {
                throw new IllegalStateException("Could not find client info");
            }
            Integer compteur = parseInt(paramService.getParameterValue("CMPT_CMD", customerInfo.getCustomId()));
            String compteurCommande = String.format("%010d", compteur);
            paramService.setValeurParam("CMPT_CMD", customerInfo.getCustomId(), (++compteur).toString());
            String subject = paramService.getParameterValue("Subject_mail", customerInfo.getCustomId());
            Double[] sum = new Double[]{0.0};
            // Send email
            departCommandeService.sendEmail(customerInfo.getCourriel(), subject, compteurCommande, input, sum, session);
            // enregistre la commande
            Integer cmdId = departCommandeService.enregistreCommande(compteurCommande, input, sum, session);
            Integer cmptneworder = Integer.valueOf(paramService.getParameterValue("NEW_ORDER", customerInfo.getCustomId()));
            paramService.setValeurParam("NEW_ORDER", customerInfo.getCustomId(), (++cmptneworder).toString());
            // Envoyer la notification
            notificationService.sendPushNotification(clientInfo.get().getDeviceId(),
                    "Nouvelle(s) commande(s) dans votre Praticboutic",
                    "Commande(s) en attente de validation");
            // Comptabiliser la tansaction sur stripe
            boolean usageRecordCreated = stripeService.recordSubscriptionUsage(
                    customerInfo.getCustomId(), sum[0], Double.parseDouble(input.get("remise").toString()), Double.parseDouble(input.get("fraislivr").toString()));
            // Vérification du résultat
            if (usageRecordCreated) {
                logger.info("L'enregistrement d'utilisation a été créé avec succès.");
            } else {
                logger.info("Aucun enregistrement d'utilisation n'a été créé.");
            }
            // Envoyer sms si nécessaire
            // Get SMS validation parameter
            String validSms = paramService.getParameterValue("VALIDATION_SMS", customerInfo.getCustomId());
            smsService.sendOrderSms(validSms, cmdId, customerInfo.getCustomId(), input.get("telephone").toString());
            session.setAttribute(customer + "_mail", "oui");
        } catch (SessionExpiredException e) {
            logger.error("Erreur d'initialisation: {}", e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(Collections.singletonList("OK"));
    }
}

