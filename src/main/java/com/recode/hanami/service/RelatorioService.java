package com.recode.hanami.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.recode.hanami.dto.*;
import com.recode.hanami.entities.Venda;
import com.recode.hanami.repository.VendaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RelatorioService {

    private static final Logger logger = LoggerFactory.getLogger(RelatorioService.class);

    private final VendaRepository vendaRepository;
    private final CalculadoraMetricasService calculadoraService;
    private final CalculosDemografiaRegiao calculosDemografiaRegiao;
    private final PdfService pdfService;
    private final ObjectMapper objectMapper;

    public RelatorioService(VendaRepository vendaRepository,
                            CalculadoraMetricasService calculadoraService,
                            CalculosDemografiaRegiao calculosDemografiaRegiao,
                            PdfService pdfService) {
        this.vendaRepository = vendaRepository;
        this.calculadoraService = calculadoraService;
        this.calculosDemografiaRegiao = calculosDemografiaRegiao;
        this.pdfService = pdfService;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public byte[] gerarRelatorioJsonBytes(RelatorioCompletoDTO relatorio) {
        try {
            logger.debug("Serializando relatório para JSON");
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(relatorio);
        } catch (Exception e) {
            logger.error("Erro ao serializar relatório para JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao serializar JSON: " + e.getMessage(), e);
        }
    }

    public byte[] gerarRelatorioPdfBytes(RelatorioCompletoDTO relatorio) {
        try {
            logger.debug("Gerando relatório PDF");
            return pdfService.gerarRelatorioPdf(relatorio);
        } catch (Exception e) {
            logger.error("Erro ao gerar PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar PDF: " + e.getMessage(), e);
        }
    }

    public Mono<RelatorioCompletoDTO> gerarRelatorioCompleto() {
        logger.info("Gerando relatório completo");
        return vendaRepository.findAll().collectList().map(todasVendas -> {
            List<Venda> vendasComRelacoes = todasVendas;
            List<AnaliseProdutoDTO> analiseProdutos = gerarAnaliseProdutos(vendasComRelacoes);
            ResumoVendasDTO resumoVendas = gerarResumoVendas(todasVendas);
            Map<String, MetricasRegiaoDTO> desempenhoRegional = calculosDemografiaRegiao.calcularMetricasPorRegiao(todasVendas);
            return new RelatorioCompletoDTO(LocalDateTime.now(), gerarMetricasFinanceiras(todasVendas), analiseProdutos, resumoVendas, desempenhoRegional);
        });
    }

    public Mono<Map<String, Double>> gerarMetricasFinanceirasMap() {
        logger.debug("Gerando métricas financeiras");
        return vendaRepository.findAll().collectList().map(vendas -> {
            Double receitaLiquida = calculadoraService.calcularTotalVendas(vendas);
            Double custoTotal = calculadoraService.calcularCustoTotalGeral(vendas);
            Double lucroBruto = calculadoraService.calcularLucroBrutoGeral(vendas);

            Map<String, Double> metrics = new LinkedHashMap<>();
            metrics.put("receita_liquida", receitaLiquida);
            metrics.put("custo_total", custoTotal);
            metrics.put("lucro_bruto", lucroBruto);

            return metrics;
        });
    }

    public Mono<List<Map<String, Object>>> gerarAnaliseProdutosOrdenada(String sortBy) {
        logger.debug("Gerando análise de produtos ordenada por: {}", sortBy);
        return vendaRepository.findAll().collectList().map(vendasComRelacoes -> {
            Map<String, Map<String, Object>> produtosMap = new LinkedHashMap<>();

            for (Venda venda : vendasComRelacoes) {
                String nomeProduto = "Produto_" + venda.getProdutoId();
                Integer quantidade = venda.getQuantidade() != null ? venda.getQuantidade() : 0;
                Double receita = venda.getValorFinal() != null ? venda.getValorFinal() : 0.0;

                if (produtosMap.containsKey(nomeProduto)) {
                    Map<String, Object> produtoExistente = produtosMap.get(nomeProduto);
                    Integer qtdAtual = (Integer) produtoExistente.get("quantidade_vendida");
                    Double totalAtual = (Double) produtoExistente.get("total_arrecadado");

                    produtoExistente.put("quantidade_vendida", qtdAtual + quantidade);
                    produtoExistente.put("total_arrecadado", calculadoraService.arredondar(totalAtual + receita));
                } else {
                    Map<String, Object> novoProduto = new LinkedHashMap<>();
                    novoProduto.put("nome_produto", nomeProduto);
                    novoProduto.put("quantidade_vendida", quantidade);
                    novoProduto.put("total_arrecadado", calculadoraService.arredondar(receita));

                    produtosMap.put(nomeProduto, novoProduto);
                }
            }

            List<Map<String, Object>> relatorio = new ArrayList<>(produtosMap.values());
            aplicarOrdenacao(relatorio, sortBy);

            return relatorio;
        });
    }

    public Mono<Map<String, Object>> gerarResumoVendasMap() {
        return gerarResumoVendasMap(null, null);
    }

    public Mono<Map<String, Object>> gerarResumoVendasMap(LocalDate startDate, LocalDate endDate) {
        logger.debug("Gerando resumo de vendas - Período: {} a {}", startDate, endDate);

        Mono<List<Venda>> vendasMono;
        if (startDate != null && endDate != null) {
            vendasMono = vendaRepository.findByDataVendaBetween(startDate, endDate).collectList().doOnNext(list -> logger.info("Filtrando vendas por período: {} transações encontradas", list.size()));
        } else {
            vendasMono = vendaRepository.findAll().collectList();
        }

        return vendasMono.map(vendas -> {
            Integer numeroTotalVendas = calculadoraService.calcularNumeroTransacoes(vendas);
            Double valorMedioPorTransacao = calculadoraService.calcularMediaPorTransacao(vendas);
            String formaPagamentoMaisUtilizada = calculadoraService.calcularFormaPagamentoMaisUtilizada(vendas);
            String formaPagamentoMenosUtilizada = calculadoraService.calcularFormaPagamentoMenosUtilizada(vendas);
            String canalVendasMaisUtilizado = calculadoraService.calcularCanalVendasMaisUtilizado(vendas);
            String canalVendasMenosUtilizado = calculadoraService.calcularCanalVendasMenosUtilizado(vendas);

            Map<String, Object> resumo = new LinkedHashMap<>();
            resumo.put("numero_total_vendas", numeroTotalVendas);
            resumo.put("valor_medio_por_transacao", valorMedioPorTransacao);
            resumo.put("forma_pagamento_mais_utilizada", formaPagamentoMaisUtilizada);
            resumo.put("forma_pagamento_menos_utilizada", formaPagamentoMenosUtilizada);
            resumo.put("canal_vendas_mais_utilizado", canalVendasMaisUtilizado);
            resumo.put("canal_vendas_menos_utilizado", canalVendasMenosUtilizado);

            return resumo;
        });
    }

    private void aplicarOrdenacao(List<Map<String, Object>> relatorio, String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "quantidade":
                relatorio.sort((a, b) -> ((Integer) b.get("quantidade_vendida"))
                        .compareTo((Integer) a.get("quantidade_vendida")));
                break;
            case "total":
                relatorio.sort((a, b) -> ((Double) b.get("total_arrecadado"))
                        .compareTo((Double) a.get("total_arrecadado")));
                break;
            default:
                relatorio.sort(Comparator.comparing(a -> ((String) a.get("nome_produto"))));
                break;
        }
    }

    private MetricasFinanceirasDTO gerarMetricasFinanceiras(List<Venda> vendas) {
        Double receitaLiquida = calculadoraService.calcularTotalVendas(vendas);
        Double custoTotal = calculadoraService.calcularCustoTotalGeral(vendas);
        Double lucroBruto = calculadoraService.calcularLucroBrutoGeral(vendas);

        return new MetricasFinanceirasDTO(receitaLiquida, custoTotal, lucroBruto);
    }

    private List<AnaliseProdutoDTO> gerarAnaliseProdutos(List<Venda> vendasComRelacoes) {
        Map<String, Map<String, Object>> produtosMap = new LinkedHashMap<>();

        for (Venda venda : vendasComRelacoes) {
            String nomeProduto = "Produto_" + venda.getProdutoId();
            Integer quantidade = venda.getQuantidade() != null ? venda.getQuantidade() : 0;
            Double receita = venda.getValorFinal() != null ? venda.getValorFinal() : 0.0;

            if (produtosMap.containsKey(nomeProduto)) {
                Map<String, Object> produtoExistente = produtosMap.get(nomeProduto);
                Integer qtdAtual = (Integer) produtoExistente.get("quantidade_vendida");
                Double totalAtual = (Double) produtoExistente.get("total_arrecadado");

                produtoExistente.put("quantidade_vendida", qtdAtual + quantidade);
                produtoExistente.put("total_arrecadado", calculadoraService.arredondar(totalAtual + receita));
            } else {
                Map<String, Object> novoProduto = new LinkedHashMap<>();
                novoProduto.put("nome_produto", nomeProduto);
                novoProduto.put("quantidade_vendida", quantidade);
                novoProduto.put("total_arrecadado", calculadoraService.arredondar(receita));

                produtosMap.put(nomeProduto, novoProduto);
            }
        }

        return produtosMap.values().stream()
                .map(map -> new AnaliseProdutoDTO(
                        (String) map.get("nome_produto"),
                        (Integer) map.get("quantidade_vendida"),
                        (Double) map.get("total_arrecadado")
                ))
                .sorted((a, b) -> b.totalArrecadado().compareTo(a.totalArrecadado()))
                .collect(Collectors.toList());
    }

    private ResumoVendasDTO gerarResumoVendas(List<Venda> vendas) {
        Integer numeroTotalVendas = calculadoraService.calcularNumeroTransacoes(vendas);
        Double valorMedioPorTransacao = calculadoraService.calcularMediaPorTransacao(vendas);
        String formaPagamentoMaisUtilizada = calculadoraService.calcularFormaPagamentoMaisUtilizada(vendas);
        String formaPagamentoMenosUtilizada = calculadoraService.calcularFormaPagamentoMenosUtilizada(vendas);
        String canalVendasMaisUtilizado = calculadoraService.calcularCanalVendasMaisUtilizado(vendas);
        String canalVendasMenosUtilizado = calculadoraService.calcularCanalVendasMenosUtilizado(vendas);

        return new ResumoVendasDTO(
                numeroTotalVendas,
                valorMedioPorTransacao,
                formaPagamentoMaisUtilizada,
                formaPagamentoMenosUtilizada,
                canalVendasMaisUtilizado,
                canalVendasMenosUtilizado
        );
    }
}
