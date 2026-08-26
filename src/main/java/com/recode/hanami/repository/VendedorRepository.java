package com.recode.hanami.repository;

import com.recode.hanami.entities.Vendedor;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendedorRepository extends R2dbcRepository<Vendedor, String> {
}