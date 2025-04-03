package com.ecommerce.praticboutic_backend_java;

public class DatabaseLink {
    private String sourceTable;  // La table source
    private String sourceField;  // Le champ source
    private String destTable;    // La table cible (destination)
    private String destField;    // Le champ cible
    private String joinType;     // Type de jointure (ex: "lj", "ij")
    private int index;           // Index unique (pour référencer chaque jointure)

    // Constructeur
    public DatabaseLink(String sourceTable, String sourceField, String destTable, String destField, String joinType, int index) {
        this.sourceTable = sourceTable;
        this.sourceField = sourceField;
        this.destTable = destTable;
        this.destField = destField;
        this.joinType = joinType;
        this.index = index;
    }

    // Getters (pour permettre l'accès aux propriétés)
    public String getSourceTable() {
        return sourceTable;
    }

    public String getSourceField() {
        return sourceField;
    }

    public String getDestTable() {
        return destTable;
    }

    public String getDestField() {
        return destField;
    }

    public String getJoinType() {
        return joinType;
    }

    public int getIndex() {
        return index;
    }
}
