package com.ecommerce.praticboutic_backend_java.entities;

import com.ecommerce.praticboutic_backend_java.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "promotion")
public class Promotion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promoid")
    private Integer promoid;

    @Column(name = "customid", nullable = false)
    private Integer customid;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "taux", nullable = false)
    private Double taux;

    @Column(name = "actif", nullable = false, columnDefinition = "TINYINT UNSIGNED DEFAULT 1")
    private Boolean actif = true;

}