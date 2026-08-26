package com.recode.hanami.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "vendedores")
public class Vendedor {

    @Id
    @Column("vendedor_id")
    private String id;

    public Vendedor() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}