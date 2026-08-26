package com.recode.hanami.repository;

import com.recode.hanami.entities.Venda;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface VendaRepository extends R2dbcRepository<Venda, String> {
    
    @Query("""
        SELECT v.* FROM vendas v
        LEFT JOIN clientes c ON v.cliente_id = c.cliente_id
        LEFT JOIN produtos p ON v.produto_id = p.produto_id
        LEFT JOIN vendedores vd ON v.vendedor_id = vd.vendedor_id
        """)
    Flux<Venda> findAllWithRelations();

    @Query("""
        SELECT v.* FROM vendas v
        LEFT JOIN clientes c ON v.cliente_id = c.cliente_id
        WHERE UPPER(c.estado_cliente) = UPPER(:estado)
        """)
    Flux<Venda> findByClienteEstado(@Param("estado") String estado);

    @Query("""
        SELECT v.* FROM vendas v
        WHERE v.data_venda BETWEEN :startDate AND :endDate
        """)
    Flux<Venda> findByDataVendaBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Deprecated(since = "Fase2", forRemoval = true)
    default List<Venda> findAllBlocking() {
        return findAll().collectList().block();
    }

    @Deprecated(since = "Fase2", forRemoval = true)
    default List<Venda> findByClienteEstadoBlocking(String estado) {
        return findByClienteEstado(estado).collectList().block();
    }

    @Deprecated(since = "Fase2", forRemoval = true)
    default List<Venda> findByDataVendaBetweenBlocking(LocalDate startDate, LocalDate endDate) {
        return findByDataVendaBetween(startDate, endDate).collectList().block();
    }
}