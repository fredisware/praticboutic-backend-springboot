package com.ecommerce.praticboutic_backend_java.services;

import com.ecommerce.praticboutic_backend_java.utils.Utils;
import com.ecommerce.praticboutic_backend_java.entities.Client;
import com.ecommerce.praticboutic_backend_java.entities.Customer;
import com.ecommerce.praticboutic_backend_java.exceptions.DatabaseException;
import com.ecommerce.praticboutic_backend_java.repositories.ClientRepository;
import com.ecommerce.praticboutic_backend_java.repositories.CustomerRepository;
import com.ecommerce.praticboutic_backend_java.requests.BuildBouticRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
@Transactional
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SessionService sessionService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Déclarez le logger en tant que champ statique en haut de votre classe
    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);



    public Client authenticate(String email, String password) {
        Optional<Client> optionalClient = clientRepository.findByEmail(email);
        if (optionalClient.isPresent()) {
            Client client = optionalClient.get();
            if (passwordEncoder.matches(password, client.getPass())) {
                return client;
            }
        }
        return null;
    }
    

    public Optional<Client> findById(Integer clientId) {
        return clientRepository.findClientById(clientId);
    }
    

    public Client findByEmail(String email) {
        return clientRepository.findByEmail(email).orElse(null);
    }
    

    public Client save(Client client, boolean encodePassword) {
        if (encodePassword && client.getPass() != null) {
            client.setPass(passwordEncoder.encode(client.getPass()));
        }
        return clientRepository.save(client);
    }
    

    public boolean emailExists(String email) {
        return clientRepository.findByEmail(email).isPresent();
    }
    

    public Client updateClient(Integer clientId, Client updatedClient) throws Exception {
        Optional<Client> client = clientRepository.findClientById(clientId);
        if (client.isEmpty())
            throw new Exception ("Le client n'existe pas");

            
            if (updatedClient.getNom() != null) {
                client.get().setNom(updatedClient.getNom());
            }
            if (updatedClient.getPrenom() != null) {
                client.get().setPrenom(updatedClient.getPrenom());
            }
            if (updatedClient.getAdr1() != null) {
                client.get().setAdr1(updatedClient.getAdr1());
            }
            if (updatedClient.getAdr2() != null) {
                client.get().setAdr2(updatedClient.getAdr2());
            }
            if (updatedClient.getCp() != null) {
                client.get().setCp(updatedClient.getCp());
            }
            if (updatedClient.getVille() != null) {
                client.get().setVille(updatedClient.getVille());
            }
            if (updatedClient.getTel() != null) {
                client.get().setTel(updatedClient.getTel());
            }
            
            return clientRepository.save(client.get());


    }

    public List<?> getClientInfo(String strCustomer) throws Exception {
        Customer customer = customerRepository.findByCustomer(strCustomer);
        Optional <Client> client = clientRepository.findClientById(customer.getCltid());
        if (!client.isPresent())
            throw new Exception ("Le client n'existe pas");

        return List.of(customer.getCustomId(),customer.getNom(), customer.getNom() + " " + client.get().getAdr1() + " " + client.get().getAdr2() + " " + client.get().getCp() + " " + client.get().getVille(), customer.getLogo()  );

    }


    public Client createAndSaveClient(HttpSession session, BuildBouticRequest input)
            throws DatabaseException.EmailAlreadyExistsException, DataAccessException {
        // Récupération et validation de l'email
        String verifyEmail = sessionService.getSessionAttributeAsString(session, "verify_email");
        if (StringUtils.isEmpty(verifyEmail)) {
            throw new DatabaseException.InvalidSessionDataException("L'email ne peut pas être vide");
        }

        // Vérification de l'unicité de l'email
        Long existingClientCount = clientRepository.countByEmail(verifyEmail);
        if (existingClientCount > 0) {
            throw new DatabaseException.EmailAlreadyExistsException("Ce courriel est déjà utilisé: " + verifyEmail);
        }

        // Création du client avec hachage sécurisé du mot de passe
        Client client = new Client();
        client.setEmail(verifyEmail);

        String password = sessionService.getSessionAttributeAsString(session, "registration_pass");
        if (StringUtils.isEmpty(password)) {
            throw new DatabaseException.InvalidSessionDataException("Le mot de passe ne peut pas être vide");
        }

        client.setPass(passwordEncoder.encode(password));

        // Remplissage des autres informations client
        client.setQualite(sessionService.getSessionAttributeAsString(session, "registration_qualite"));
        client.setNom(sessionService.getSessionAttributeAsString(session, "registration_nom"));
        client.setPrenom(sessionService.getSessionAttributeAsString(session, "registration_prenom"));
        client.setAdr1(sessionService.getSessionAttributeAsString(session, "registration_adr1"));
        client.setAdr2(sessionService.getSessionAttributeAsString(session, "registration_adr2"));
        client.setCp(sessionService.getSessionAttributeAsString(session, "registration_cp"));
        client.setVille(sessionService.getSessionAttributeAsString(session, "registration_ville"));
        client.setTel(sessionService.getSessionAttributeAsString(session, "registration_tel"));
        client.setStripeCustomerId(sessionService.getSessionAttributeAsString(session, "registration_stripe_customer_id"));
        client.setActif(1);
        client.setDeviceId(Utils.sanitizeInput(input.getDeviceId()));
        client.setDeviceType(Utils.sanitizeInput(input.getDeviceType().toString()));

        try {
            return clientRepository.save(client);
        } catch (DataAccessException e) {
            logger.error("Erreur lors de la sauvegarde du client", e);
            throw new DataAccessException("Erreur lors de la sauvegarde du client", e) {};
        }
    }
}