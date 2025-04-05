package com.ecommerce.praticboutic_backend_java.services;

import com.ecommerce.praticboutic_backend_java.entities.Option;
import com.ecommerce.praticboutic_backend_java.repositories.OptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Service gérant les opérations liées aux options
 */
@Service
@Transactional
public class OptionService {

    @Autowired
    private OptionRepository optionRepository;

    /**
     * Récupère les options pour un groupe d'options donné
     * 
     * @param grpOptId L'identifiant du groupe d'options
     * @return Liste des options sous forme de tableaux d'objets
     */
    public List<List<Object>> getOptionsByGroupe(Integer grpOptId) {
        List<Option> options = optionRepository.findByGrpoptid(grpOptId);
        List<List<Object>> result = new ArrayList<>();
        
        for (Option option : options) {
            List<Object> optionArray = Arrays.asList(
                option.getOptId(),
                option.getGroupeOptionId(),
                option.getNom(),
                option.getSurcout()
            );
            result.add(optionArray);
        }
        
        return result;
    }
    
    /**
     * Trouve une option par son identifiant
     * 
     * @param optId L'identifiant de l'option
     * @return L'option ou null si non trouvée
     */
    public Option findById(Integer optId) {
        return optionRepository.findById(optId).orElse(null);
    }
    
    /**
     * Sauvegarde une option
     * 
     * @param option L'option à sauvegarder
     * @return L'option sauvegardée
     */
    public Option save(Option option) {
        return optionRepository.save(option);
    }
    
    /**
     * Supprime une option
     * 
     * @param optId L'identifiant de l'option à supprimer
     */
    public void delete(Integer optId) {
        optionRepository.deleteById(optId);
    }
    
    /**
     * Récupère toutes les options pour un groupe d'options
     * 
     * @param grpOptId L'identifiant du groupe d'options
     * @return Liste des options
     */
    public List<Option> getAllOptionsByGroupe(Integer grpOptId) {
        return optionRepository.findByGrpoptid(grpOptId);
    }
    
    public List<?> getOptions(Integer bouticid, Integer grpoptid) {
        return null;
    }



}