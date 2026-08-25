package com.recode.hanami.controller;

import com.recode.hanami.controller.docs.CsvControllerOpenApi;
import com.recode.hanami.dto.ImportacaoResponseDTO;
import com.recode.hanami.service.CsvService;
import com.recode.hanami.service.ProcessamentoVendasService;
import com.recode.hanami.validation.UploadArquivoValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/hanami")
public class CsvController implements CsvControllerOpenApi {

    private static final Logger logger = LoggerFactory.getLogger(CsvController.class);

    private final CsvService csvService;
    private final ProcessamentoVendasService processamentoVendasService;
    private final UploadArquivoValidator uploadArquivoValidator;

    public CsvController(CsvService csvService,
                         ProcessamentoVendasService processamentoVendasService,
                         UploadArquivoValidator uploadArquivoValidator) {
        this.csvService = csvService;
        this.processamentoVendasService = processamentoVendasService;
        this.uploadArquivoValidator = uploadArquivoValidator;
    }

    @PostMapping(value = "/upload-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Override
    public Mono<ResponseEntity<ImportacaoResponseDTO>> uploadCsv(@RequestPart("file") FilePart file) {
        logger.info("Iniciando processamento de upload de arquivo: {}", file != null ? file.filename() : "null");

        try {
            uploadArquivoValidator.validate(file);
        } catch (Exception e) {
            logger.warn("Validação do arquivo falhou: {}", e.getMessage());
            ImportacaoResponseDTO resp = new ImportacaoResponseDTO("erro", 0);
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp));
        }

        return processamentoVendasService.salvarDadosDoArquivo(csvService.conversorCsvParaJson(file))
                .map(count -> {
                    logger.info("Dados salvos com sucesso. Total de registros: {}", count);
                    ImportacaoResponseDTO resposta = new ImportacaoResponseDTO("sucesso", count.intValue());
                    return ResponseEntity.ok(resposta);
                })
                .onErrorResume(ex -> {
                    logger.error("Erro ao processar upload: {}", ex.getMessage(), ex);
                    ImportacaoResponseDTO resp = new ImportacaoResponseDTO("erro_processamento", 0);
                    return Mono.just(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(resp));
                });
    }
}

