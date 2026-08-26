package com.recode.hanami.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDate;

@Table(name = "vendas")
public class Venda {

    @Id
    @Column("id_transacao")
    private String id;

    @Column("data_venda")
    private LocalDate dataVenda;

    @Column("valor_final")
    private Double valorFinal;

    @Column("subtotal")
    private Double subtotal;

    @Column("desconto_percent")
    private Double descontoPercent;

    @Column("canal_venda")
    private String canalVenda;

    @Column("forma_pagamento")
    private String formaPagamento;

    @Column("quantidade")
    private Integer quantidade;

    @Column("regiao")
    private String regiao;

    @Column("status_entrega")
    private String statusEntrega;

    @Column("tempo_entrega_dias")
    private Integer tempoEntregaDias;

    // IDs dos relacionamentos (sem objetos decorados)
    @Column("cliente_id")
    private String clienteId;

    @Column("produto_id")
    private String produtoId;

    @Column("vendedor_id")
    private String vendedorId;

    public Venda() {
    }


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDate getDataVenda() { return dataVenda; }
    public void setDataVenda(LocalDate dataVenda) { this.dataVenda = dataVenda; }

    public Double getValorFinal() { return valorFinal; }
    public void setValorFinal(Double valorFinal) { this.valorFinal = valorFinal; }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }

    public Double getDescontoPercent() { return descontoPercent; }
    public void setDescontoPercent(Double descontoPercent) { this.descontoPercent = descontoPercent; }

    public String getCanalVenda() { return canalVenda; }
    public void setCanalVenda(String canalVenda) { this.canalVenda = canalVenda; }

    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }

    // --- GETTERS E SETTERS DOS NOVOS CAMPOS (ESSENCIAIS PARA O ERRO SUMIR) ---

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

    public String getRegiao() { return regiao; }
    public void setRegiao(String regiao) { this.regiao = regiao; }

    public String getStatusEntrega() { return statusEntrega; }
    public void setStatusEntrega(String statusEntrega) { this.statusEntrega = statusEntrega; }

    public Integer getTempoEntregaDias() { return tempoEntregaDias; }
    public void setTempoEntregaDias(Integer tempoEntregaDias) { this.tempoEntregaDias = tempoEntregaDias; }

    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }

    public String getProdutoId() { return produtoId; }
    public void setProdutoId(String produtoId) { this.produtoId = produtoId; }

    public String getVendedorId() { return vendedorId; }
    public void setVendedorId(String vendedorId) { this.vendedorId = vendedorId; }

    // ⚠️ FASE 3: Métodos stub para compatibilidade temporária
    // Em R2DBC, os relacionamentos não são carregados automaticamente
    // A Fase 3 refatorará esses acessos para usar IDs e repositórios
    @Deprecated(since = "Fase2", forRemoval = true)
    public Cliente getCliente() {
        // Retorna null - será refatorado na Fase 3
        return null;
    }

    @Deprecated(since = "Fase2", forRemoval = true)
    public void setCliente(Cliente cliente) {
        // Ignorado - use setClienteId() em vez disso
    }

    @Deprecated(since = "Fase2", forRemoval = true)
    public Produto getProduto() {
        // Retorna null - será refatorado na Fase 3
        return null;
    }

    @Deprecated(since = "Fase2", forRemoval = true)
    public void setProduto(Produto produto) {
        // Ignorado - use setProdutoId() em vez disso
    }

    @Deprecated(since = "Fase2", forRemoval = true)
    public Vendedor getVendedor() {
        // Retorna null - será refatorado na Fase 3
        return null;
    }

    @Deprecated(since = "Fase2", forRemoval = true)
    public void setVendedor(Vendedor vendedor) {
        // Ignorado - use setVendedorId() em vez disso
    }
}