package com.ecommerce.praticboutic_backend_java.requests;

public class LoginLinkRequest {
    private String sessionid;
    private Integer bouticid;
    private String platform;

    // Getters and setters
    public String getSessionid() {
        return sessionid;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }

    public Integer getBouticid() {
        return bouticid;
    }

    public void setBouticid(Integer bouticid) {
        this.bouticid = bouticid;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
