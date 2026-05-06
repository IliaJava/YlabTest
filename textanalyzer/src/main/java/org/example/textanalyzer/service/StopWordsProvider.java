package org.example.textanalyzer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;


/**
 * Отдельный компонент для загрузки стоп-слов.
 * Можно расширять (кэширование, разные форматы и т.д.).
 */
@Component
public class StopWordsProvider {

    private static final Logger log = LoggerFactory.getLogger(StopWordsProvider.class);

    /**
     * Загружает стоп-слова из файла, по одному слову в строке.
     */
    public Set<String> loadStopWords(Path path) {
        Set<String> result = new HashSet<>();
        if (!Files.exists(path)) {
            log.warn("Stopwords file does not exist: {}", path);
            return result;
        }

        try {
            Files.lines(path, StandardCharsets.UTF_8)
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .map(String::toLowerCase)
                    .forEach(result::add);
        } catch (IOException e) {
            log.warn("Failed to read stopwords file {}: {}", path, e.getMessage());
        }

        return result;
    }
}