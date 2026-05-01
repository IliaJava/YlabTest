package org.example.textanalyzer.service;

import org.example.textanalyzer.model.AnalysisInfo;
import org.example.textanalyzer.model.AnalysisResult;
import org.example.textanalyzer.model.ErrorInfo;
import org.example.textanalyzer.model.WordCount;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

//Загружает стоп‑слова.
//Создаёт пул потоков.
//На каждый файл создаёт задачу FileProcessingTask.
//Запускает все задачи параллельно.
//Собирает результаты из Future.
//Объединяет локальные карты в глобальную.
//Сортирует слова.
//Формирует итоговый объект AnalysisResult.
//Возвращает его в Runner.
@Service
@Qualifier("parallel")
public class ParallelTextAnalysisService implements TextAnalysisService {

    private final StopWordsProvider stopWordsProvider;

    public ParallelTextAnalysisService(StopWordsProvider stopWordsProvider) {
        this.stopWordsProvider = stopWordsProvider;
    }

    @Override
    public AnalysisResult analyze(Path directory,
                                  int minWordLength,
                                  int topN,
                                  Optional<Path> stopWordsFile,
                                  int threads) {

        //Фиксируем время начала анализа
        long start = System.currentTimeMillis();

        //errors — список ошибок.
        //globalMap — потокобезопасная карта для объединения результатов.

        List<ErrorInfo> errors = new ArrayList<>();
        Map<String, Long> globalMap = new ConcurrentHashMap<>();

        //Если файл указан → загружаем стоп‑слова.
        //Если нет → используем пустой набор.

        Set<String> stopWords = stopWordsFile
                .map(stopWordsProvider::loadStopWords)
                .orElse(Collections.emptySet());

        //Создаём пул фиксированного размера;
        //threads — значение из --threads или 2 по умолчанию.

        ExecutorService pool = Executors.newFixedThreadPool(threads);

        //Каждый Future будет содержать результат обработки одного файла.
        List<Future<Map<String, Long>>> futures = new ArrayList<>();

        //Files.list() возвращает Stream<Path>;
        //try‑with‑resources автоматически закроет поток.

        try (var paths = Files.list(directory)) {
           //Оставляем только .txt файлы.
            paths.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".txt"))
//            создаём FileProcessingTask;
//            отправляем в пул через submit();
//            сохраняем Future в список.
                    .forEach(path -> {
                        futures.add(pool.submit(new FileProcessingTask(path, minWordLength, stopWords)));
                    });
        } catch (IOException e) {
            errors.add(new ErrorInfo(directory.toString(), e.getMessage()));
        }

        int processedFiles = 0;

        for (Future<Map<String, Long>> f : futures) {
            try {
                //f.get() блокируется, пока задача не завершится
                 //local — карта частот для одного файла.
                Map<String, Long> local = f.get();
                processedFiles++;
                // объединение локальных карт в глобальную
                //                merge() безопасен для многопоточности.
                //                Если слово уже есть → суммируем.
                //                Если нет → добавляем.
                local.forEach((word, count) ->
                        globalMap.merge(word, count, Long::sum));

            } catch (Exception e) {
                errors.add(new ErrorInfo("unknown", e.getMessage()));
            }
        }

        pool.shutdown();

        long time = System.currentTimeMillis() - start;
//        сортируем по убыванию;
//        берём topN;
//        превращаем в список WordCount.
        List<WordCount> topWords = globalMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(topN)
                .map(e -> new WordCount(e.getKey(), e.getValue()))
                .toList();

        AnalysisInfo info = new AnalysisInfo(
                directory.toString(),
                minWordLength,
                topN,
                "multi",
                threads,
                processedFiles,
                time
        );

        return new AnalysisResult(info, topWords, errors);
    }
}