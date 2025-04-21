package com.ecommerce.praticboutic_backend_java.requests;


public class LiensRequest {
    private String action;
    private String login;
    private String sessionid;

    // Getters
    public String getAction() {
        return action;
    }

    public String getLogin() {
        return login;
    }

    public String getSessionid() {
        return sessionid;
    }

    // Setters
    public void setAction(String action) {
        this.action = action;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }
}
