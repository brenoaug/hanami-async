package com.recode.hanami.validation;

import com.recode.hanami.exception.ArquivoInvalidoException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.codec.multipart.FilePart;

@Component
public class UploadArquivoValidator {

    private static final String CSV_EXTENSION = ".csv";

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ArquivoInvalidoException("Nenhum arquivo foi enviado ou o arquivo está vazio.");
        }

        if (!hasValidExtension(file)) {
            throw new ArquivoInvalidoException("O arquivo deve ter a extensão .csv");
        }
    }

    // Overload para suportar FilePart do WebFlux (reativo)
    public void validate(FilePart file) {
        if (file == null) {
            throw new ArquivoInvalidoException("Nenhum arquivo foi enviado ou o arquivo está vazio.");
        }

        String filename = file.filename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new ArquivoInvalidoException("Nome do arquivo inválido.");
        }

        if (!filename.toLowerCase().endsWith(CSV_EXTENSION)) {
            throw new ArquivoInvalidoException("O arquivo deve ter a extensão .csv");
        }
    }

    private boolean hasValidExtension(MultipartFile file) {
        if (file.getOriginalFilename() == null) {
            return false;
        }
        return file.getOriginalFilename().toLowerCase().endsWith(CSV_EXTENSION);
    }
}
