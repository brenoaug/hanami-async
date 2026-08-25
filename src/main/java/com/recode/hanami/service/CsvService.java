package com.recode.hanami.service;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.recode.hanami.dto.DadosArquivoDTO;
import com.recode.hanami.exception.ArquivoInvalidoException;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class CsvService {

    // Mantém o método síncrono existente para compatibilidade
    public List<DadosArquivoDTO> conversorCsvParaJson(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            CsvMapper csvMapper = new CsvMapper();

            csvMapper.registerModule(new JavaTimeModule());

            CsvSchema schema = csvMapper.schemaFor(DadosArquivoDTO.class)
                    .withHeader()
                    .withColumnReordering(true)
                    .withStrictHeaders(true);

            try (MappingIterator<DadosArquivoDTO> it = csvMapper
                    .readerFor(DadosArquivoDTO.class)
                    .with(schema)
                    .readValues(inputStream)) {

                return it.readAll();
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro de leitura do arquivo: " + e.getMessage());
        } catch (RuntimeException e) {

            if (e.getMessage() != null && (e.getMessage().contains("Missing header") || e.getMessage().contains("Too many entries"))) {

                throw new ArquivoInvalidoException("Layout do arquivo inválido. Verifique se todas as colunas obrigatórias estão presentes. Detalhe técnico: " + e.getMessage());
            }
            throw e;
        }
    }

    // Nova versão reativa: recebe FilePart e retorna Flux<DadosArquivoDTO]
    public Flux<DadosArquivoDTO> conversorCsvParaJson(FilePart filePart) {
        return DataBufferUtils.join(filePart.content())
                .map(DataBuffer::asByteBuffer)
                .map(bb -> {
                    byte[] bytes = new byte[bb.remaining()];
                    bb.get(bytes);
                    return bytes;
                })
                .flatMapMany(bytes -> Flux.defer(() -> {
                    try (InputStream is = new ByteArrayInputStream(bytes)) {
                        CsvMapper csvMapper = new CsvMapper();
                        csvMapper.registerModule(new JavaTimeModule());
                        CsvSchema schema = csvMapper.schemaFor(DadosArquivoDTO.class)
                                .withHeader()
                                .withColumnReordering(true)
                                .withStrictHeaders(true);

                        try (MappingIterator<DadosArquivoDTO> it = csvMapper
                                .readerFor(DadosArquivoDTO.class)
                                .with(schema)
                                .readValues(is)) {

                            List<DadosArquivoDTO> list = it.readAll();
                            return Flux.fromIterable(list);
                        }
                    } catch (IOException e) {
                        return Flux.error(new RuntimeException("Erro de leitura do arquivo: " + e.getMessage(), e));
                    } catch (RuntimeException e) {
                        if (e.getMessage() != null && (e.getMessage().contains("Missing header") || e.getMessage().contains("Too many entries"))) {
                            return Flux.error(new ArquivoInvalidoException("Layout do arquivo inválido. Verifique se todas as colunas obrigatórias estão presentes. Detalhe técnico: " + e.getMessage()));
                        }
                        return Flux.error(e);
                    }
                }).subscribeOn(Schedulers.boundedElastic()));
    }
}