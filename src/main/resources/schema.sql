CREATE TABLE IF NOT EXISTS clientes (
    cliente_id VARCHAR(255) NOT NULL,
    nome_cliente VARCHAR(255),
    idade_cliente INT,
    genero_cliente VARCHAR(10),
    cidade_cliente VARCHAR(255),
    estado_cliente VARCHAR(2),
    renda_estimada DOUBLE,
    PRIMARY KEY (cliente_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS produtos (
    produto_id VARCHAR(255) NOT NULL,
    nome_produto VARCHAR(255),
    categoria VARCHAR(255),
    marca VARCHAR(255),
    preco_unitario DOUBLE,
    quantidade INT,
    margem_lucro DOUBLE,
    PRIMARY KEY (produto_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS vendedores (
    vendedor_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (vendedor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS vendas (
    id_transacao VARCHAR(255) NOT NULL,
    data_venda DATE,
    valor_final DOUBLE,
    subtotal DOUBLE,
    desconto_percent DOUBLE,
    canal_venda VARCHAR(255),
    forma_pagamento VARCHAR(255),
    quantidade INT,
    regiao VARCHAR(255),
    status_entrega VARCHAR(255),
    tempo_entrega_dias INT,
    cliente_id VARCHAR(255),
    produto_id VARCHAR(255),
    vendedor_id VARCHAR(255),
    PRIMARY KEY (id_transacao),
    CONSTRAINT fk_vendas_clientes FOREIGN KEY (cliente_id) REFERENCES clientes (cliente_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_vendas_produtos FOREIGN KEY (produto_id) REFERENCES produtos (produto_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_vendas_vendedores FOREIGN KEY (vendedor_id) REFERENCES vendedores (vendedor_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- CREATE INDEX idx_vendas_cliente_id ON vendas (cliente_id);
-- CREATE INDEX idx_vendas_produto_id ON vendas (produto_id);
-- CREATE INDEX idx_vendas_vendedor_id ON vendas (vendedor_id);
-- CREATE INDEX idx_vendas_data_venda ON vendas (data_venda);

