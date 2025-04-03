package com.ecommerce.praticboutic_backend_java.requests;

public class LoginRequest {

    private String email;
    private String password;
    private String sessionid;

    // Getters and Setters
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getSessionid() {
        return sessionid;
    }
    
    public void setSessionId(String sessionid) {
        this.sessionid = this.sessionid;
    }
}