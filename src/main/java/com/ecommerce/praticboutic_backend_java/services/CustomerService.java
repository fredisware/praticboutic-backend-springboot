package com.ecommerce.praticboutic_backend_java.services;

import com.ecommerce.praticboutic_backend_java.entities.Customer;
import com.ecommerce.praticboutic_backend_java.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * Récupère la liste des boutiques actives avec leurs informations Stripe associées
     *
     * @return liste des informations des boutiques actives
     */
    public List<Customer> findActiveCustomersWithStripeInfo() {
        return customerRepository.findActiveCustomersWithStripeInfo();
    }
    
    // Autres méthodes du service...
}