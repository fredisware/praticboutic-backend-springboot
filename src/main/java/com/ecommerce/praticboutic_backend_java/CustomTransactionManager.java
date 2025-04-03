package com.ecommerce.praticboutic_backend_java;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Gestionnaire de transactions
 */
@Component
public class CustomTransactionManager {
    
    private final PlatformTransactionManager transactionManager = null;
    
    @Autowired
    public void TransactionManager() {
    }
    
    public <T> void executeWithTransaction(TransactionCallback<T> action) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.execute(action);
    }
}