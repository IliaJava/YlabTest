package org.example.textanalyzer.model;

/**
 * Информация о параметрах анализа.
 */
public class AnalysisInfo {
    private String directory;
    private int minWordLength;
    private int topCount;

    public AnalysisInfo() {
    }

    public AnalysisInfo(String directory, int minWordLength, int topCount) {
        this.directory = directory;
        this.minWordLength = minWordLength;
        this.topCount = topCount;
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