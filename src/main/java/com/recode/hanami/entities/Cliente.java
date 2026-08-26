package com.recode.hanami.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "clientes")
public class Cliente {

    @Id
    @Column("cliente_id")
    private String id;

    @Column("nome_cliente")
    private String nomeCliente;

    @Column("idade_cliente")
    private Integer idadeCliente;

    @Column("genero_cliente")
    private String generoCliente;

    @Column("cidade_cliente")
    private String cidadeCliente;

    @Column("estado_cliente")
    private String estadoCliente;

    @Column("renda_estimada")
    private double rendaEstimada;

    public Cliente() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public Integer getIdadeCliente() {
        return idadeCliente;
    }

    public void setIdadeCliente(Integer idadeCliente) {
        this.idadeCliente = idadeCliente;
    }

    public String getGeneroCliente() {
        return generoCliente;
    }

    public void setGeneroCliente(String generoCliente) {
        this.generoCliente = generoCliente;
    }

    public String getCidadeCliente() {
        return cidadeCliente;
    }

    public void setCidadeCliente(String cidadeCliente) {
        this.cidadeCliente = cidadeCliente;
    }

    public String getEstadoCliente() {
        return estadoCliente;
    }

    public void setEstadoCliente(String estadoCliente) {
        this.estadoCliente = estadoCliente;
    }

    public double getRendaEstimada() {
        return rendaEstimada;
    }

    public void setRendaEstimada(double rendaEstimada) {
        this.rendaEstimada = rendaEstimada;
    }
}