package com.ecommerce.praticboutic_backend_java.repositories;

import com.ecommerce.praticboutic_backend_java.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité Customer
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    Optional<Customer> findByCustomid(Integer customid);

    /**
     * Sauvegarde ou met à jour un client
     *
     * @param customer le client à sauvegarder
     * @return le client sauvegardé avec son identifiant généré si c'est une création
     */
    Customer save(Customer customer);

    @Query("SELECT c FROM Customer c WHERE c.actif = true")
    List<Customer> findActiveCustomersWithStripeInfo();

    // Supprimez la méthode annotée avec @Query et utilisez celle-ci à la place
    List<Customer> findByActifIsTrue();

    @Query("SELECT c FROM Customer c WHERE c.actif = true")
    List<Customer> findActiveCustomers();

    Optional<Customer> findByCourrielAndActifIsTrue(String userEmail);

    Optional<Customer> findByCustomerIgnoreCase(String alias);

    //boolean isPresent();

    // To check if any customer exists
    //boolean existsBy();






}
