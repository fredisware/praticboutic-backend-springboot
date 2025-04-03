package com.ecommerce.praticboutic_backend_java.repositories;

import com.ecommerce.praticboutic_backend_java.entities.Client;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


/**
 * Repository pour l'entité Client
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {
    @Query("SELECT COUNT(c) FROM Client c WHERE c.email = :email")
    Long countByEmail(String email);

    @Modifying
    @Transactional
    @Query("UPDATE Client c SET c.email = :email WHERE c.id = :id")
    void updateEmailById(@Param("email") String email, @Param("id") Integer id);

    Client findByEmailAndActif(String email, boolean actif);

    Client findByEmailAndActifTrue(String email);

    Optional<Client> findByEmail(String email);
}
