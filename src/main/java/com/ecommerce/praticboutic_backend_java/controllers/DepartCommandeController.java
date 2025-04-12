package com.ecommerce.praticboutic_backend_java.controllers;

//import com.ecommerce.praticboutic_backend_java.configurations.CommonConfig;
import com.ecommerce.praticboutic_backend_java.configurations.CommonConfig;
import com.ecommerce.praticboutic_backend_java.entities.Client;
import com.ecommerce.praticboutic_backend_java.entities.Customer;
import com.ecommerce.praticboutic_backend_java.exceptions.SessionExpiredException;
import com.ecommerce.praticboutic_backend_java.repositories.ClientRepository;
import com.ecommerce.praticboutic_backend_java.repositories.CustomerRepository;
import com.ecommerce.praticboutic_backend_java.requests.DepartCommandeRequest;
import com.ecommerce.praticboutic_backend_java.services.DepartCommandeService;
import com.ecommerce.praticboutic_backend_java.services.EmailService;
import com.ecommerce.praticboutic_backend_java.services.NotificationService;
import com.ecommerce.praticboutic_backend_java.services.ParameterService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.SQLException;
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

    @Value("mail.sender.address")
    private String sendmail;

    @Autowired
    private DepartCommandeService departCommandeService;

    @Autowired
    private CustomerRepository customerRepository;

    // Déclarez le logger en tant que champ statique en haut de votre classe
    private static final Logger logger = LoggerFactory.getLogger(DepartCommandeController.class);




    @PostMapping("/depart")
    public ResponseEntity<String> creerDepartCommande(@RequestBody DepartCommandeRequest input, HttpSession session) {
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
            customerInfo = new Customer();
            customerInfo = customerRepository.findByCustomer(customer);
            if (customerInfo == null) {
                throw new IllegalStateException("Could not find customer info");
            }

            //getCustomerInfo(customer);

            // Get client device information
            Optional<Client> clientInfo = clientRepository.findById(customerInfo.getCltid());

            Integer compteur = parseInt(paramService.getParameterValue("CMPT_CMD", customerInfo.getCustomId()));
            String compteurCommande = String.format("%010d", compteur);
            paramService.setValeurParam("CMPT_CMD", customerInfo.getCustomId(), (++compteur).toString());

            String subject = paramService.getParameterValue("Subject", customerInfo.getCustomId());


            // Send email
            departCommandeService.sendEmail(sendmail, subject, compteurCommande, input);

            // enregistre la commande
            departCommandeService.enregistreCommande(compteurCommande, input);

            // Envoyer la notification

            //try {
            //    FirebaseNotificationSender sender = new FirebaseNotificationSender(
            //            "chemin/vers/credentials.json",
            //"http://votre-domaine.com/");

            //sender.sendNotification(clientInfo.getDeviceId(), clientInfo.getDeviceType()); // 0 pour Web, 1 pour Android, 2 pour iOS

        } catch (SessionExpiredException e) {
            logger.error("Erreur d'initialisation: " + e.getMessage(), e);
        } catch (SQLException | MessagingException ex) {
            throw new RuntimeException(ex);
        }

        // Comptabiliser la tansaction sur stripe

        // Envoyer sms si nécessaire
        // Get SMS validation parameter
        //String validSms = paramService.getParameterValue("VALIDATION_SMS", customerInfo.getCustomId());


        return null;
    }

}

