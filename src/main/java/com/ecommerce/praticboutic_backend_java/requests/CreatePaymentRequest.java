package com.ecommerce.praticboutic_backend_java.requests;



import com.ecommerce.praticboutic_backend_java.Item;

import java.util.List;

public class CreatePaymentRequest {
    private String sessionId;
    private String boutic;
    private List<Item> items;
    private String model;
    private Double fraisLivr;
    private String codePromo;

    // Getters et Setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getBoutic() {
        return boutic;
    }

    public void setBoutic(String boutic) {
        this.boutic = boutic;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Double getFraisLivr() {
        return fraisLivr;
    }

    public void setFraisLivr(Double fraisLivr) {
        this.fraisLivr = fraisLivr;
    }

    public String getCodePromo() {
        return codePromo;
    }

    public void setCodePromo(String codePromo) {
        this.codePromo = codePromo;
    }
}