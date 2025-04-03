package com.ecommerce.praticboutic_backend_java.services;

import com.ecommerce.praticboutic_backend_java.entities.Boutic;
import com.ecommerce.praticboutic_backend_java.repositories.BouticRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service gérant les opérations liées aux boutiques
 */
@Service
@Transactional
public class BouticService {

    @Autowired
    private BouticRepository bouticRepository;

    /**
     * Récupère les informations d'une boutique
     * 
     * @param bouticId L'identifiant de la boutique
     * @return Map contenant les informations de la boutique
     */
    public Map<String, Object> getBouticInfo(Integer bouticId) {
        Optional<Boutic> optionalBoutic = bouticRepository.findById(bouticId);
        if (optionalBoutic.isPresent()) {
            Boutic boutic = optionalBoutic.get();
            Map<String, Object> info = Map.of(
                "nom", boutic.getNom(),
                "description", boutic.getDescription(),
                "ouvert", boutic.isOuvert(),
                "livraison", boutic.isLivraison(),
                "emporter", boutic.isEmporter(),
                "surplace", boutic.isSurplace(),
                "delai_livraison", boutic.getDelaiLivraison(),
                "delai_emporter", boutic.getDelaiEmporter()
            );
            return info;
        }
        return null;
    }
    
    /**
     * Trouve une boutique par son identifiant
     * 
     * @param bouticId L'identifiant de la boutique
     * @return La boutique ou null si non trouvée
     */
    public Boutic findById(Integer bouticId) {
        return bouticRepository.findById(bouticId).orElse(null);
    }
    
    /**
     * Sauvegarde une boutique
     * 
     * @param boutic La boutique à sauvegarder
     * @return La boutique sauvegardée
     */
    public Boutic save(Boutic boutic) {
        return bouticRepository.save(boutic);
    }
    
    /**
     * Met à jour l'état d'ouverture d'une boutique
     * 
     * @param bouticId L'identifiant de la boutique
     * @param ouvert Nouvel état d'ouverture
     * @return La boutique mise à jour ou null si non trouvée
     */
    public Boutic updateOuvert(Integer bouticId, boolean ouvert) {
        Optional<Boutic> optionalBoutic = bouticRepository.findById(bouticId);
        if (optionalBoutic.isPresent()) {
            Boutic boutic = optionalBoutic.get();
            boutic.setOuvert(ouvert);
            return bouticRepository.save(boutic);
        }
        return null;
    }
    
    /**
     * Récupère toutes les boutiques
     * 
     * @return Liste des boutiques
     */
    public List<Boutic> findAll() {
        return bouticRepository.findAll();
    }
    
    /**
     * Récupère toutes les boutiques ouvertes
     * 
     * @return Liste des boutiques ouvertes
     */
    public List<Boutic> findAllOpen() {
        return bouticRepository.findByOuvert(true);
    }
    
    /**
     * Récupère les boutiques par code postal
     * 
     * @param codePostal Code postal recherché
     * @return Liste des boutiques correspondantes
     */
    public List<Boutic> findByCodePostal(String codePostal) {
        return bouticRepository.findByCodePostal(codePostal);
    }
}