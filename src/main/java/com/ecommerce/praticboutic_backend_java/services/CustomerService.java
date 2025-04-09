package com.ecommerce.praticboutic_backend_java.services;

import com.ecommerce.praticboutic_backend_java.entities.Client;
import com.ecommerce.praticboutic_backend_java.entities.Customer;
import com.ecommerce.praticboutic_backend_java.exceptions.DatabaseException;
import com.ecommerce.praticboutic_backend_java.repositories.CustomerRepository;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;


    @Autowired
    private SessionService sessionService;

    // Déclarez le logger en tant que champ statique en haut de votre classe
    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);

    /**
     * Récupère la liste des boutiques actives avec leurs informations Stripe associées
     *
     * @return liste des informations des boutiques actives
     */
    public List<Customer> findActiveCustomersWithStripeInfo() {
        return customerRepository.findActiveCustomersWithStripeInfo();
    }

    public Optional<Customer> findByCustomerIgnoreCase(String alias) {
        return customerRepository.findByCustomerIgnoreCase(alias);
    }

    /*public boolean existsBy(){
        return customerRepository.existsBy();
    }*/

    /**
     * Crée et sauvegarde une boutique (customer) à partir des données de session
     */
    public Customer createAndSaveCustomer(HttpSession session, Client client)
            throws DatabaseException.InvalidAliasException, DataAccessException {
        // Validation de l'alias de la boutique
        String aliasBoutic = sessionService.getSessionAttributeAsString(session, "initboutic_aliasboutic");
        if (StringUtils.isEmpty(aliasBoutic)) {
            throw new DatabaseException.InvalidAliasException("L'identifiant de la boutique ne peut pas être vide");
        }

        // Validation du format de l'alias (lettres, chiffres et tirets uniquement)
        if (!aliasBoutic.matches("^[a-zA-Z0-9-]+$")) {
            throw new DatabaseException.InvalidAliasException("L'identifiant ne peut contenir que des lettres, des chiffres et des tirets");
        }

        // Vérification des identifiants interdits
        List<String> forbiddenIds = Arrays.asList("admin", "common", "upload", "vendor", "api", "assets",
                "static", "media", "support", "help", "login", "register", "system");
        if (forbiddenIds.contains(aliasBoutic.toLowerCase())) {
            throw new DatabaseException.InvalidAliasException("Cet identifiant n'est pas autorisé: " + aliasBoutic);
        }

        // Vérification de l'unicité de l'alias (à implémenter dans le repository)
        /*Optional<Customer> existingCustomer = customerRepository.findByCustomerIgnoreCase(aliasBoutic);
        if (existingCustomer.isPresent()) {
            throw new InvalidAliasException("Cet identifiant de boutique est déjà utilisé: " + aliasBoutic);
        }*/

        // Création de la boutique
        Customer customer = new Customer();
        customer.setCltid(client.getCltId());
        customer.setCustomer(aliasBoutic);
        customer.setNom(sessionService.getSessionAttributeAsString(session, "initboutic_nom"));
        customer.setLogo(sessionService.getSessionAttributeAsString(session, "initboutic_logo"));
        customer.setCourriel(sessionService.getSessionAttributeAsString(session, "initboutic_email"));
        customer.setActif(true);

        try {
            return customerRepository.save(customer);
        } catch (DataAccessException e) {
            logger.error("Erreur lors de la sauvegarde de la boutique", e);
            throw new DataAccessException("Erreur lors de la sauvegarde de la boutique", e) {};
        }
    }


    // Autres méthodes du service...
}