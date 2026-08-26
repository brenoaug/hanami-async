package com.recode.hanami.controller.docs;

import com.recode.hanami.dto.DistribuicaoClientesDTO;
import com.recode.hanami.dto.MetricasRegiaoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Documentação OpenAPI para o ReportsController.
 * Centraliza todas as anotações do Swagger para manter o controller limpo.
 */
@Tag(name = "Relatórios e Análises", description = "Endpoints para geração de relatórios e análises de vendas")
public interface ReportsControllerOpenApi {

    @Operation(
            summary = "Métricas financeiras globais",
            description = "Retorna um resumo consolidado das principais métricas financeiras da empresa: " +
                    "receita líquida total, custo total operacional e lucro bruto. " +
                    "Este endpoint processa todas as transações de vendas e calcula os indicadores-chave de desempenho (KPIs) financeiros."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Métricas calculadas com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Exemplo de resposta",
                                    description = "Resumo financeiro consolidado de todas as transações",
                                    value = """
                                            {
                                              "receita_liquida": 458900.75,
                                              "custo_total": 321230.50,
                                              "lucro_bruto": 137670.25
                                            }
                                            """
                            )
                    )
            )
    })
    Mono<ResponseEntity<Map<String, Double>>> getFinancialMetrics();

    @Operation(
            summary = "Análise detalhada por produto",
            description = """
                    Retorna uma análise agregada das vendas agrupadas por produto.
                    
                    **Campos retornados:**
                    - `nome_produto`: Nome do produto
                    - `quantidade_vendida`: Total de unidades vendidas do produto
                    - `total_arrecadado`: Valor total de receita gerado pelo produto
                    
                    **Parâmetros de ordenação disponíveis:**
                    - `nome` (padrão) - Por nome do produto (alfabético)
                    - `quantidade` - Por quantidade vendida (maior → menor)
                    - `total` - Por total arrecadado (maior → menor)
                    - `qualquer outro valor` - Usará o padrão `nome`
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Análise realizada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Exemplo de resposta",
                                    description = "Lista de produtos com totais agregados",
                                    value = """
                                            [
                                              {
                                                "nome_produto": "Notebook Dell Inspiron",
                                                "quantidade_vendida": 125,
                                                "total_arrecadado": 400000.00
                                              }
                                            ]
                                            """
                            )
                    )
            )
    })
    Mono<ResponseEntity<List<Map<String, Object>>>> analisarLucros(
            @Parameter(
                    description = """
                            Critério de ordenação dos resultados. **Opções disponíveis:**
                            
                            - `nome` (padrão) - Ordena por nome do produto (alfabético)
                            - `quantidade` - Ordena por quantidade vendida (maior para menor)
                            - `total` - Ordena por total arrecadado (maior para menor)
                            
                            **Exemplo de uso:** `/hanami/reports/product-analysis?sort_by=total`""",
                    example = "total"
            )
            String sortBy
    );

    @Operation(
            summary = "Resumo financeiro das vendas",
            description = """
                    Retorna um resumo detalhado das vendas com métricas de transações,
                    formas de pagamento e canais de venda mais e menos utilizados.
                    
                    **Filtros disponíveis:**
                    - `start_date` (opcional) - Data inicial do período no formato YYYY-MM-DD
                    - `end_date` (opcional) - Data final do período no formato YYYY-MM-DD
                    
                    Quando nenhum filtro é aplicado, retorna dados de todas as vendas.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Resumo calculado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Exemplo de resposta",
                                    value = """
                                            {
                                              "numero_total_vendas": 356,
                                              "valor_medio_por_transacao": 690.45,
                                              "forma_pagamento_mais_utilizada": "Cartão de Crédito"
                                            }
                                            """
                            )
                    )
            )
    })
    Mono<ResponseEntity<Map<String, Object>>> resumoFinanceiro(
            @Parameter(
                    description = """
                            Data inicial do período para filtrar as vendas (formato: YYYY-MM-DD).
                            
                            **Exemplo:** `2024-01-01`
                            
                            Deve ser usado em conjunto com `end_date`.
                            """,
                    example = "2024-01-01"
            )
            LocalDate startDate,
            @Parameter(
                    description = """
                            Data final do período para filtrar as vendas (formato: YYYY-MM-DD).
                            
                            **Exemplo:** `2024-12-31`
                            
                            Deve ser usado em conjunto com `start_date`.
                            """,
                    example = "2024-12-31"
            )
            LocalDate endDate
    );

    @Operation(
            summary = "Desempenho por região",
            description = """
                    Retorna métricas de vendas agrupadas por região geográfica.
                    
                    **Filtros disponíveis:**
                    - `estado` (opcional) - Filtra os resultados por estado (UF). Exemplo: `SP`, `RJ`, `MG`
                    
                    Quando nenhum filtro é aplicado, retorna dados de todas as regiões.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Métricas por região calculadas com sucesso"
            )
    })
    Mono<ResponseEntity<Map<String, MetricasRegiaoDTO>>> getRegionalPerformance(
            @Parameter(
                    description = """
                            Sigla do estado (UF) para filtrar os resultados.
                            
                            **Exemplos:** `SP`, `RJ`, `MG`, `RS`
                            
                            Quando não especificado, retorna métricas de todos os estados.
                            """,
                    example = "SP"
            )
            String estado
    );

    @Operation(
            summary = "Perfil demográfico dos clientes",
            description = "Retorna a distribuição dos clientes por gênero, faixa etária e cidade."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil demográfico calculado com sucesso"
            )
    })
    Mono<ResponseEntity<DistribuicaoClientesDTO>> getCustomerProfile();

    @Operation(
            summary = "Download de relatório completo",
            description = """
                    Faz o download de um relatório completo de análise de vendas nos formatos JSON ou PDF.
                    
                    **Formatos disponíveis:**
                    - `json` - Relatório em formato JSON para download
                    - `pdf` - Relatório em formato PDF com tabelas e gráficos
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Formato inválido"),
            @ApiResponse(responseCode = "500", description = "Erro ao gerar relatório")
    })
    Mono<ResponseEntity<byte[]>> downloadRelatorio(
            @Parameter(
                    description = "Formato do relatório: 'json' ou 'pdf'",
                    example = "pdf",
                    required = true
            )
            String format
    );
}
