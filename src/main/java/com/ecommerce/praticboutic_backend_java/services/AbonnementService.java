package com.ecommerce.praticboutic_backend_java.services;

import com.ecommerce.praticboutic_backend_java.entities.Abonnement;
import com.ecommerce.praticboutic_backend_java.repositories.AbonnementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service gérant les opérations liées aux abonnements
 */
@Service
@Transactional
public class AbonnementService {

    @Autowired
    private AbonnementRepository abonnementRepository;


    /**
     * Trouve un abonnement par son identifiant
     * 
     * @param abonnementId L'identifiant de l'abonnement
     * @return L'abonnement ou null si non trouvé
     */
    public Abonnement findById(Integer abonnementId) {
        return abonnementRepository.findById(abonnementId).orElse(null);
    }
    
    /**
     * Sauvegarde un abonnement
     * 
     * @param abonnement L'abonnement à sauvegarder
     * @return L'abonnement sauvegardé
     */
    public Abonnement save(Abonnement abonnement) {
        return abonnementRepository.save(abonnement);
    }

    /**
     * Crée un nouvel abonnement
     * 
     * @param bouticId L'identifiant de la boutique
     * @param typePlan Type de plan d'abonnement
     * @param dureeMonths Durée en mois
     * @return Le nouvel abonnement
     */
    public Abonnement createSubscription(Integer bouticId, String typePlan, int dureeMonths) {
        LocalDate dateDebut = LocalDate.now();
        LocalDate dateFin = dateDebut.plusMonths(dureeMonths);
        
        Abonnement abonnement = new Abonnement();
        abonnement.setBouticId(bouticId);
        abonnement.setTypePlan(typePlan);
        abonnement.setDateDebut(dateDebut);
        abonnement.setDateFin(dateFin);
        
        return abonnementRepository.save(abonnement);
    }

    public String getStripeCustomerId(Integer bouticid) {
        return null;
    }
}