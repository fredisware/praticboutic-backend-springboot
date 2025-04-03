package com.ecommerce.praticboutic_backend_java.requests;

public class ChargeRequest {
    private String sessionId;
    private Integer bouticId;

    // Getters et Setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getBouticId() {
        return bouticId;
    }

    public void setBouticId(Integer bouticId) {
        this.bouticId = bouticId;
    }
}