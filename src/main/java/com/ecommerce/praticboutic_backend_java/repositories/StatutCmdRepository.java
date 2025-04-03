package com.ecommerce.praticboutic_backend_java.repositories;

import com.ecommerce.praticboutic_backend_java.entities.StatutCmd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository pour l'entité StatutCmd
 */
@Repository
public interface StatutCmdRepository extends JpaRepository<StatutCmd, Integer> {


}