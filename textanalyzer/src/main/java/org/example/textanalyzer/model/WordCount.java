package org.example.textanalyzer.model;

/**
 * Пара "слово - количество".
 *  Слово и его частота.
 */
public class WordCount {
    private String word;
    private long count;

    public WordCount() {
    }

    public WordCount(String word, long count) {
        this.word = word;
        this.count = count;
    }

    public String getWord() {
        return word;
    }

    public long getCount() {
        return count;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public void setCount(long count) {
        this.count = count;
    }
}