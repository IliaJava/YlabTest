package org.example.textanalyzer.dto;

import org.example.textanalyzer.model.AnalysisResult;
import org.example.textanalyzer.util.AnalysisStatus;
import lombok.Data;

/**
 * Ответ для GET /api/results/{id} и /api/results.
 */
@Data
public class AnalysisStatusResponse {
    private Long id;
    private AnalysisStatus status;
    private AnalysisResult result; // null, если анализ ещё не завершён
}