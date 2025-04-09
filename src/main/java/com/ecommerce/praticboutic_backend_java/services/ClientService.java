package com.ecommerce.praticboutic_backend_java.services;

import com.ecommerce.praticboutic_backend_java.Utils;
import com.ecommerce.praticboutic_backend_java.entities.Client;
import com.ecommerce.praticboutic_backend_java.exceptions.DatabaseException;
import com.ecommerce.praticboutic_backend_java.repositories.ClientRepository;
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

/**
 * Service gérant les opérations liées aux clients
 */
@Service
@Transactional
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private SessionService sessionService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Déclarez le logger en tant que champ statique en haut de votre classe
    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);


    /**
     * Authentifie un client
     * 
     * @param email Email du client
     * @param password Mot de passe à vérifier
     * @return Le client authentifié ou null si échec
     */
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
    
    /**
     * Trouve un client par son identifiant
     * 
     * @param clientId L'identifiant du client
     * @return Le client ou null si non trouvé
     */
    public Client findById(Integer clientId) {
        return clientRepository.findById(clientId).orElse(null);
    }
    
    /**
     * Trouve un client par son email
     * 
     * @param email Email du client
     * @return Le client ou null si non trouvé
     */
    public Client findByEmail(String email) {
        return clientRepository.findByEmail(email).orElse(null);
    }
    
    /**
     * Sauvegarde un client
     * 
     * @param client Le client à sauvegarder
     * @param encodePassword Indique si le mot de passe doit être encodé
     * @return Le client sauvegardé
     */
    public Client save(Client client, boolean encodePassword) {
        if (encodePassword && client.getPass() != null) {
            client.setPass(passwordEncoder.encode(client.getPass()));
        }
        return clientRepository.save(client);
    }
    
    /**
     * Vérifie si un email est déjà utilisé
     * 
     * @param email Email à vérifier
     * @return true si l'email est déjà utilisé, false sinon
     */
    public boolean emailExists(String email) {
        return clientRepository.findByEmail(email).isPresent();
    }
    
    /**
     * Met à jour les informations d'un client
     * 
     * @param clientId L'identifiant du client
     * @param updatedClient Client avec les nouvelles informations
     * @return Le client mis à jour ou null si non trouvé
     */
    public Client updateClient(Integer clientId, Client updatedClient) {
        Optional<Client> optionalClient = clientRepository.findById(clientId);
        if (optionalClient.isPresent()) {
            Client client = optionalClient.get();
            
            if (updatedClient.getNom() != null) {
                client.setNom(updatedClient.getNom());
            }
            if (updatedClient.getPrenom() != null) {
                client.setPrenom(updatedClient.getPrenom());
            }
            if (updatedClient.getAdr1() != null) {
                client.setAdr1(updatedClient.getAdr1());
            }
            if (updatedClient.getAdr2() != null) {
                client.setAdr2(updatedClient.getAdr2());
            }
            if (updatedClient.getCp() != null) {
                client.setCp(updatedClient.getCp());
            }
            if (updatedClient.getVille() != null) {
                client.setVille(updatedClient.getVille());
            }
            if (updatedClient.getTel() != null) {
                client.setTel(updatedClient.getTel());
            }
            
            return clientRepository.save(client);
        }
        return null;
    }

    public List<?> getClientInfo(Integer bouticid) {
        return null;
    }

    /**
     * Crée et sauvegarde un client à partir des données de session
     */
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
        client.setActif(true);
        client.setDeviceId(Utils.sanitizeInput(input.getDeviceId()));
        client.setDeviceType(Utils.sanitizeInput(input.getDeviceType()));

        try {
            return clientRepository.save(client);
        } catch (DataAccessException e) {
            logger.error("Erreur lors de la sauvegarde du client", e);
            throw new DataAccessException("Erreur lors de la sauvegarde du client", e) {};
        }
    }
}