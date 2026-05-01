package org.example.textanalyzer.service;

import org.example.textanalyzer.model.AnalysisResult;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Интерфейс сервиса анализа текстов.
 * Позволяет легко подменить реализацию (SOLID: DIP).
 */
public interface TextAnalysisService {

    /**
     * Анализирует все .txt файлы в указанной директории.
     *
     * @param directory     путь к директории
     * @param minWordLength минимальная длина слова
     * @param topN          количество наиболее частых слов
     * @param stopWordsFile путь к файлу со стоп-словами (опционально)
     * @return результат анализа
     */
    // В многопоточном варианте threads можно игнорировать (в single) или использовать (в multi)
    AnalysisResult analyze(Path directory,
                           int minWordLength,
                           int topN,
                           Optional<Path> stopWordsFile,
                           int threads);
}
