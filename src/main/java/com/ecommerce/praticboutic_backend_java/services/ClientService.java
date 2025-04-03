package com.ecommerce.praticboutic_backend_java.services;

import com.ecommerce.praticboutic_backend_java.entities.Client;
import com.ecommerce.praticboutic_backend_java.repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service gérant les opérations liées aux clients
 */
@Service
@Transactional
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

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
}