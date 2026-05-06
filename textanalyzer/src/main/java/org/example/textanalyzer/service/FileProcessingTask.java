package org.example.textanalyzer.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;

import static org.example.textanalyzer.service.TextAnalysisServiceImpl.WORD_PATTERN;

//Каждый файл обрабатывается в отдельном потоке.
//
//Каждый поток возвращает локальную карту частот, чтобы избежать гонок.
public class FileProcessingTask implements Callable<Map<String, Long>> {

    private final Path file;
    private final int minWordLength;
    private final Set<String> stopWords;

    public FileProcessingTask(Path file, int minWordLength, Set<String> stopWords) {
        this.file = file;
        this.minWordLength = minWordLength;
        this.stopWords = stopWords;
    }

    @Override
    public Map<String, Long> call() throws Exception {
        Map<String, Long> localMap = new HashMap<>();

        String content = Files.readString(file);
        Matcher matcher = WORD_PATTERN.matcher(content);

        while (matcher.find()) {
            String word = matcher.group().toLowerCase(Locale.ROOT);

            if (word.length() < minWordLength) continue;
            if (stopWords.contains(word)) continue;

            localMap.merge(word, 1L, Long::sum);
        }

        return localMap;
    }
}