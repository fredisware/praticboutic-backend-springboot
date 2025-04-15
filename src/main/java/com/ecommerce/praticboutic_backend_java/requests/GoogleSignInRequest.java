package com.ecommerce.praticboutic_backend_java.requests;

// Classe pour la désérialisation de la requête
public class GoogleSignInRequest {
    private String email;
    private String sessionid;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSessionid() {
        return sessionid;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }
}
