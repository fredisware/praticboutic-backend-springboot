package com.ecommerce.praticboutic_backend_java.requests;

public class VerificationRequest {
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

