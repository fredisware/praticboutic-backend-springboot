package com.ecommerce.praticboutic_backend_java.repositories;

import com.ecommerce.praticboutic_backend_java.entities.Boutic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour l'accès aux données des boutiques
 */
@Repository
public interface BouticRepository extends JpaRepository<Boutic, Integer> {
    
    /**
     * Recherche les boutiques par état d'ouverture
     * 
     * @param ouvert État d'ouverture recherché
     * @return Liste des boutiques correspondantes
     */
    List<Boutic> findByOuvert(boolean ouvert);
    
    /**
     * Recherche les boutiques par code postal
     * 
     * @param codePostal Code postal recherché
     * @return Liste des boutiques correspondantes
     */
    List<Boutic> findByCodePostal(String codePostal);
}