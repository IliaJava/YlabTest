package org.example.textanalyzer.model;

import java.util.List;

/**
 * Итоговый результат анализа для JSON/консоли.
 */
public class AnalysisResult {
    private AnalysisInfo analysisInfo;
    private List<WordCount> words;
    private List<ErrorInfo> errors;

    public AnalysisResult() {
    }

    public AnalysisResult(AnalysisInfo analysisInfo,
                          List<WordCount> words,
                          List<ErrorInfo> errors) {
        this.analysisInfo = analysisInfo;
        this.words = words;
        this.errors = errors;
    }

    public AnalysisInfo getAnalysisInfo() {
        return analysisInfo;
    }

    public List<WordCount> getWords() {
        return words;
    }

    public List<ErrorInfo> getErrors() {
        return errors;
    }

    public void setAnalysisInfo(AnalysisInfo analysisInfo) {
        this.analysisInfo = analysisInfo;
    }

    public void setWords(List<WordCount> words) {
        this.words = words;
    }

    public void setErrors(List<ErrorInfo> errors) {
        this.errors = errors;
    }
}