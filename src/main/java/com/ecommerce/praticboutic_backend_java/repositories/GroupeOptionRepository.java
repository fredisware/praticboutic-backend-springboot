package com.ecommerce.praticboutic_backend_java.repositories;

import com.ecommerce.praticboutic_backend_java.entities.GroupeOpt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Interface de repository pour les opérations de base de données sur les entités GroupeOption
 */
@Repository
public interface GroupeOptionRepository extends JpaRepository<GroupeOpt, Integer> {

    /**
     * Recherche les groupes d'options par article et les trie par ordre
     *
     * @param artid L'identifiant de l'article
     * @return Liste des groupes d'options triés
     */
    List<GroupeOpt> findByArtid(Integer artid);

    /**
     * Compte le nombre de groupes d'options pour un article donné
     *
     * @param artid L'identifiant de l'article
     * @return Le nombre de groupes d'options
     */
    long countByArtid(Integer artid);

    /**
     * Supprime tous les groupes d'options liés à un article
     *
     * @param artid L'identifiant de l'article
     */
    void deleteByArtid(Integer artid);
    
    /**
     * Vérifie si un article possède au moins un groupe d'options
     *
     * @param artid L'identifiant de l'article
     * @return true si l'article a au moins un groupe d'options, false sinon
     */
    boolean existsByArtid(Integer artid);
    
    /**
     * Recherche les groupes d'options par article et caractère obligatoire
     *
     * @param artid L'identifiant de l'article
     * @param obligatoire Indique si les groupes d'options sont obligatoires
     * @return Liste des groupes d'options correspondants triés
     */
    List<GroupeOpt> findByArtidAndObligatoire(Integer artid, boolean obligatoire);
}