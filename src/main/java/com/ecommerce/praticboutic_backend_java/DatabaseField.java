package com.ecommerce.praticboutic_backend_java;

public class DatabaseField {
    private String name;       // Nom du champ (colonne en base de données)
    private String type;       // Type du champ (exemple : "pk", "column", "fk", etc.)
    private Integer order;         // Ordre dans lequel le champ est trié (0 si pas de tri)
    private String orderDirection; // Direction du tri ("A" pour ASC, "D" pour DESC)

    // Constructeur
    public DatabaseField(String name, String type, int order, String orderDirection) {
        this.name = name;
        this.type = type;
        this.order = order;
        this.orderDirection = orderDirection;
    }

    // Getters et Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getOrderDirection() {
        return orderDirection;
    }

    public void setOrderDirection(String orderDirection) {
        this.orderDirection = orderDirection;
    }

    // Méthode toString (optionnelle, pour le débogage)
    @Override
    public String toString() {
        return "DatabaseField{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", order=" + order +
                ", orderDirection='" + orderDirection + '\'' +
                '}';
    }
}