package com.recode.hanami.controller.docs;

import com.recode.hanami.dto.ImportacaoResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

/**
 * Documentação OpenAPI para o CsvController.
 * Todas as anotações do Swagger estão centralizadas nesta interface.
 */
@Tag(name = "Upload de Arquivos CSV", description = "Endpoints para processamento e importação de dados de vendas via CSV")
public interface CsvControllerOpenApi {

    @Operation(
            summary = "Upload de arquivo CSV com dados de vendas",
            description = "Processa um arquivo CSV contendo dados de vendas, clientes, produtos e vendedores. " +
                    "O arquivo deve seguir o formato específico com todas as colunas obrigatórias. " +
                    "Os dados são validados e persistidos no banco de dados."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Arquivo processado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ImportacaoResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "Sucesso",
                                    value = """
                                            {
                                              "status": "sucesso",
                                              "linhas_processadas": 150
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Nenhum arquivo enviado ou arquivo vazio",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Arquivo não enviado",
                                    value = """
                                            {
                                              "status": "erro",
                                              "mensagem": "Nenhum arquivo foi enviado ou o arquivo está vazio."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Erro de validação dos dados do arquivo",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Dados inválidos",
                                    value = """
                                            {
                                              "status": "erro_processamento",
                                              "mensagem": "Layout do arquivo inválido. Verifique se todas as colunas obrigatórias estão presentes."
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Erro interno",
                                    value = """
                                            {
                                              "status": "erro_interno",
                                              "mensagem": "Ocorreu um erro inesperado: Falha na conexão com o banco de dados"
                                            }
                                            """
                            )
                    )
            )
    })
    Mono<ResponseEntity<ImportacaoResponseDTO>> uploadCsv(
            @Parameter(
                    description = "Arquivo CSV contendo dados de vendas. Formato esperado: " +
                            "id_transacao,data_venda,valor_final,subtotal,desconto_percent,canal_venda,forma_pagamento," +
                            "cliente_id,nome_cliente,idade_cliente,genero_cliente,cidade_cliente,estado_cliente,renda_estimada," +
                            "produto_id,nome_produto,categoria,marca,preco_unitario,quantidade,margem_lucro," +
                            "regiao,status_entrega,tempo_entrega_dias,vendedor_id",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            FilePart file
    );
}
