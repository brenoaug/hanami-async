package com.recode.hanami.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "produtos")
public class Produto {

    @Id
    @Column("produto_id")
    private String id;

    @Column("nome_produto")
    private String nomeProduto;

    @Column("categoria")
    private String categoria;

    @Column("marca")
    private String marca;

    @Column("preco_unitario")
    private Double precoUnitario;

    @Column("quantidade")
    private Integer quantidade;

    @Column("margem_lucro")
    private Double margemLucro;

    public Produto() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }

    public void setNomeProduto(String nomeProduto) {
        this.nomeProduto = nomeProduto;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public Double getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(Double precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public Double getMargemLucro() {
        return margemLucro;
    }

    public void setMargemLucro(Double margemLucro) {
        this.margemLucro = margemLucro;
    }
}