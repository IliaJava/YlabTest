package org.example.textanalyzer.dto;

import lombok.Data;

/**
 * Параметры анализа, приходящие в POST /api/analyze.
 */
@Data
public class AnalysisRequestDto {
    private String directory;
    private Integer minWordLength;
    private Integer top;
    private String mode;      // single | multi
    private Integer threads;
    private String stopwords; // путь к файлу стоп-слов (опционально)
}