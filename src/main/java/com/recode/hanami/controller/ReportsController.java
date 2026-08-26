package com.recode.hanami.controller;

import com.recode.hanami.controller.docs.ReportsControllerOpenApi;
import com.recode.hanami.dto.DistribuicaoClientesDTO;
import com.recode.hanami.dto.MetricasRegiaoDTO;
import com.recode.hanami.dto.RelatorioCompletoDTO;
import com.recode.hanami.entities.Venda;
import com.recode.hanami.repository.VendaRepository;
import com.recode.hanami.service.CalculosDemografiaRegiao;
import com.recode.hanami.service.RelatorioService;
import com.recode.hanami.util.DownloadArquivoUtil;
import com.recode.hanami.validation.FormatoRelatorioValidator;
import com.recode.hanami.validation.SortByValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("hanami/reports")
public class ReportsController implements ReportsControllerOpenApi {

    private static final Logger logger = LoggerFactory.getLogger(ReportsController.class);

    private final VendaRepository vendaRepository;
    private final CalculosDemografiaRegiao calculosDemografiaRegiao;
    private final RelatorioService relatorioService;
    private final FormatoRelatorioValidator formatoRelatorioValidator;
    private final SortByValidator sortByValidator;

    public ReportsController(VendaRepository vendaRepository,
                             CalculosDemografiaRegiao calculosDemografiaRegiao,
                             RelatorioService relatorioService,
                             FormatoRelatorioValidator formatoRelatorioValidator,
                             SortByValidator sortByValidator) {
        this.vendaRepository = vendaRepository;
        this.calculosDemografiaRegiao = calculosDemografiaRegiao;
        this.relatorioService = relatorioService;
        this.formatoRelatorioValidator = formatoRelatorioValidator;
        this.sortByValidator = sortByValidator;
    }

    @GetMapping("/financial-metrics")
    @Override
    public ResponseEntity<Map<String, Double>> getFinancialMetrics() {
        logger.debug("Solicitação de métricas financeiras");
        Map<String, Double> metrics = relatorioService.gerarMetricasFinanceirasMap();
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/product-analysis")
    @Override
    public ResponseEntity<List<Map<String, Object>>> analisarLucros(@RequestParam(value = "sort_by", required = false, defaultValue = "nome") String sortBy) {
        logger.debug("Solicitação de análise de produtos com ordenação: {}", sortBy);
        String normalizedSortBy = sortByValidator.normalize(sortBy);
        boolean isValid = sortByValidator.isValid(sortBy);
        if (!isValid) {
            logger.warn("Parâmetro de ordenação inválido: {}. Usando padrão: {}", sortBy, sortByValidator.getDefaultSort());
            normalizedSortBy = sortByValidator.getDefaultSort();
        }
        List<Map<String, Object>> relatorio = relatorioService.gerarAnaliseProdutosOrdenada(normalizedSortBy);
        return ResponseEntity.ok(relatorio);
    }

    @GetMapping("/sales-summary")
    @Override
    public ResponseEntity<Map<String, Object>> resumoFinanceiro(
            @RequestParam(value = "start_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "end_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        logger.debug("Solicitação de resumo de vendas - Período: {} a {}", startDate, endDate);
        Map<String, Object> resumo = relatorioService.gerarResumoVendasMap(startDate, endDate);
        logger.info("Resumo de vendas gerado: {} transações", resumo.get("numero_total_vendas"));
        return ResponseEntity.ok(resumo);
    }

    @GetMapping("/regional-performance")
    @Override
    public ResponseEntity<Map<String, MetricasRegiaoDTO>> getRegionalPerformance(
            @RequestParam(value = "estado", required = false) String estado) {
        logger.debug("Solicitação de desempenho por região - Estado: {}", estado);

        List<Venda> vendas;
        Map<String, MetricasRegiaoDTO> metricas;

        if (estado != null && !estado.trim().isEmpty()) {
            vendas = vendaRepository.findByClienteEstadoBlocking(estado.trim());
            metricas = calculosDemografiaRegiao.calcularMetricasPorEstado(vendas);
            logger.info("Desempenho por estado calculado: {} estado(s) - Filtro: {}",
                        metricas.size(), estado.toUpperCase().trim());
        } else {
            vendas = vendaRepository.findAllBlocking();
            metricas = calculosDemografiaRegiao.calcularMetricasPorRegiao(vendas);
            logger.info("Desempenho regional calculado: {} regiões", metricas.size());
        }

        return ResponseEntity.ok(metricas);
    }

    @GetMapping("/customer-profile")
    @Override
    public ResponseEntity<DistribuicaoClientesDTO> getCustomerProfile() {
        logger.debug("Solicitação de perfil demográfico");
        List<Venda> vendas = vendaRepository.findAllBlocking();
        DistribuicaoClientesDTO distribuicao = calculosDemografiaRegiao.calcularDistribuicaoClientes(vendas);
        logger.info("Perfil demográfico calculado");
        return ResponseEntity.ok(distribuicao);
    }

    @GetMapping("/download")
    @Override
    public ResponseEntity<byte[]> downloadRelatorio(@RequestParam(value = "format") String format) {
        logger.info("Download de relatório solicitado: formato={}", format);
        formatoRelatorioValidator.validate(format);
        RelatorioCompletoDTO relatorio = relatorioService.gerarRelatorioCompleto();

        byte[] conteudo = format.equalsIgnoreCase("json")
            ? relatorioService.gerarRelatorioJsonBytes(relatorio)
            : relatorioService.gerarRelatorioPdfBytes(relatorio);

        logger.info("Relatório {} gerado com sucesso - {} bytes", format.toUpperCase(), conteudo.length);
        return DownloadArquivoUtil.buildDownloadResponse(conteudo, format);
    }
}
