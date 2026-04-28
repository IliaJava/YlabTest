package org.example.textanalyzer.service;

import org.example.textanalyzer.model.AnalysisInfo;
import org.example.textanalyzer.model.AnalysisResult;
import org.example.textanalyzer.model.ErrorInfo;
import org.example.textanalyzer.model.WordCount;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Реализация сервиса анализа текстов.
 * Отвечает за:
 *  - обход файлов
 *  - чтение содержимого
 *  - извлечение слов через regex
 *  - фильтрацию по длине и стоп-словам
 *  - подсчёт частоты
 */
@Service
public class TextAnalysisServiceImpl implements TextAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(TextAnalysisServiceImpl.class);

    // Регулярное выражение для слов: последовательности букв и цифр
    private static final Pattern WORD_PATTERN = Pattern.compile("[\\p{L}\\p{N}]+");
    //
    private final StopWordsProvider stopWordsProvider;

    public TextAnalysisServiceImpl(StopWordsProvider stopWordsProvider) {
        this.stopWordsProvider = stopWordsProvider;
    }

    @Override
    public AnalysisResult analyze(Path directory,
                                  int minWordLength,
                                  int topN,
                                  Optional<Path> stopWordsFile) {
        //Для ошибок приработе с файлами
        List<ErrorInfo> errors = new ArrayList<>();
        //Частота вхождения слова
        Map<String, Long> frequencyMap = new HashMap<>();

        // Загружаем стоп-слова, если указаны
        Set<String> stopWords = stopWordsFile
                .map(stopWordsProvider::loadStopWords)
                .orElse(Collections.emptySet());

        // Проверяем существование директории
        //Если директория неправильная то формируем ошибку
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            String msg = "Directory does not exist or is not a directory: " + directory;
            log.error(msg);
            errors.add(new ErrorInfo(directory.toString(), msg));
            // Возвращаем пустой результат, но с ошибкой
            return new AnalysisResult(
                    new AnalysisInfo(directory.toString(), minWordLength, topN),
                    List.of(),
                    errors
            );
        }

        try {
            // Обходим только файлы с расширением .txt
            //возвращает ленивый Stream<Path>,
            // содержащий список файлов и подкаталогов, находящихся внутри указанной директории.
            try (var paths = Files.list(directory)) {
                paths.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".txt"))
                        .forEach(path -> processFile(path, minWordLength, stopWords, frequencyMap, errors));
            }
        } catch (IOException e) {
            String msg = "Failed to list files in directory: " + e.getMessage();
            log.error(msg, e);
            errors.add(new ErrorInfo(directory.toString(), msg));
        }

        // Формируем список WordCount, сортируем по убыванию частоты
        List<WordCount> topWords = frequencyMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(topN)
                .map(e -> new WordCount(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        AnalysisInfo info = new AnalysisInfo(directory.toString(), minWordLength, topN);
        return new AnalysisResult(info, topWords, errors);
    }

    /**
     * Обработка одного файла: чтение, извлечение слов, обновление частот.
     */
    private void processFile(Path file,
                             int minWordLength,
                             Set<String> stopWords,
                             Map<String, Long> frequencyMap,
                             List<ErrorInfo> errors) {
        log.debug("Processing file: {}", file);

        String content;
        try {
            content = Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            String msg = "Failed to read file: " + e.getMessage();
            log.warn(msg);
            errors.add(new ErrorInfo(file.getFileName().toString(), msg));
            return;
        }

        if (content.isBlank()) {
            log.info("File is empty: {}", file);
            return;
        }

        Matcher matcher = WORD_PATTERN.matcher(content);
        while (matcher.find()) {
            String word = matcher.group().toLowerCase(Locale.ROOT);

            // Игнорируем короткие слова
            if (word.length() < minWordLength) {
                continue;
            }

            // Игнорируем стоп-слова
            if (stopWords.contains(word)) {
                continue;
            }
            //Если слова нет в карте положить 1, если есть то+1
            frequencyMap.merge(word, 1L, Long::sum);
        }
    }
}