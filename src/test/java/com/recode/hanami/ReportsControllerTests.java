package com.recode.hanami;

import com.recode.hanami.service.RelatorioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class ReportsControllerTests {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private RelatorioService relatorioService;

    @Test
    void financialMetrics_returnsOk() {
        Map<String, Double> metrics = Map.of("receita_liquida", 12345.67);
        when(relatorioService.gerarMetricasFinanceirasMap()).thenReturn(Mono.just(metrics));

        webTestClient.get()
                .uri("/hanami/reports/financial-metrics")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.receita_liquida").isEqualTo(12345.67);
    }

    @Test
    void productAnalysis_returnsOk() {
        List<Map<String, Object>> list = List.of(Map.of("nome_produto", "P1", "quantidade_vendida", 10, "total_arrecadado", 100.0));
        when(relatorioService.gerarAnaliseProdutosOrdenada("nome")).thenReturn(Mono.just(list));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/hanami/reports/product-analysis").queryParam("sort_by", "nome").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].nome_produto").isEqualTo("P1");
    }
}
