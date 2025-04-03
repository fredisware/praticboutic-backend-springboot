package com.ecommerce.praticboutic_backend_java.requests;

public class RemplirOptionTableRequest {
    private String table;
    private Long bouticid;
    private String selcol;
    private Long selid;
    private Integer limit;
    private Integer offset;

    // Getters et Setters
    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public Long getBouticid() {
        return bouticid;
    }

    public void setBouticid(Long bouticid) {
        this.bouticid = bouticid;
    }

    public String getSelcol() {
        return selcol;
    }

    public void setSelcol(String selcol) {
        this.selcol = selcol;
    }

    public Long getSelid() {
        return selid;
    }

    public void setSelid(Long selid) {
        this.selid = selid;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }
}