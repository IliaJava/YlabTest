package org.example.textanalyzer.model;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Информация о параметрах анализа.
 */
public class AnalysisInfo {
    private String directory;
    private int minWordLength;
    private int topCount;
    private final String mode;
    private final int threads;
    private int processedFiles;
    private final long executionTimeMs;


    public AnalysisInfo(String directory, int minWordLength,
                        int topCount, String mode, int threads,
                        int processedFiles, long executionTimeMs) {
        this.directory = directory;
        this.minWordLength = minWordLength;
        this.topCount = topCount;
        this.mode = mode;
        this.threads = threads;
        this.processedFiles = processedFiles;
        this.executionTimeMs = executionTimeMs;
    }


    public String getMode() {
        return mode;
    }

    public int getThreads() {
        return threads;
    }

    public int getProcessedFiles() {
        return processedFiles;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public String getDirectory() {
        return directory;
    }

    public int getMinWordLength() {
        return minWordLength;
    }

    public int getTopCount() {
        return topCount;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public void setMinWordLength(int minWordLength) {
        this.minWordLength = minWordLength;
    }

    public void setTopCount(int topCount) {
        this.topCount = topCount;
    }
}