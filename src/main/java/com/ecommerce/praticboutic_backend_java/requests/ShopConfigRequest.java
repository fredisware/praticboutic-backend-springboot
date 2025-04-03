package com.ecommerce.praticboutic_backend_java.requests;

public class ShopConfigRequest {
    private String sessionid;
    private String chxmethode;
    private String chxpaie;
    private String mntmincmd;
    private Boolean validsms;

    // Getters et Setters
    public String getSessionid() {
        return sessionid;
    }

    public void setSessionid(String sessionid) {
        this.sessionid = sessionid;
    }

    public String getChxmethode() {
        return chxmethode;
    }

    public void setChxmethode(String chxmethode) {
        this.chxmethode = chxmethode;
    }

    public String getChxpaie() {
        return chxpaie;
    }

    public void setChxpaie(String chxpaie) {
        this.chxpaie = chxpaie;
    }

    public String getMntmincmd() {
        return mntmincmd;
    }

    public void setMntmincmd(String mntmincmd) {
        this.mntmincmd = mntmincmd;
    }

    public Boolean getValidsms() {
        return validsms;
    }

    public void setValidsms(Boolean validsms) {
        this.validsms = validsms;
    }
}