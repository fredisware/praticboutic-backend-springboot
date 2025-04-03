package com.ecommerce.praticboutic_backend_java.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "connexion")
public class Connexion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "ip", nullable = false)
    private String ip;
    
    @Column(name = "ts", nullable = false)
    private LocalDateTime ts;
    
    // Getters et setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getIp() {
        return ip;
    }
    
    public void setIp(String ip) {
        this.ip = ip;
    }
    
    public LocalDateTime getTs() {
        return ts;
    }
    
    public void setTs(LocalDateTime ts) {
        this.ts = ts;
    }
}