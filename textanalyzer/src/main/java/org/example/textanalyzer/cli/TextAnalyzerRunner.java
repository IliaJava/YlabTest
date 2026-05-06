package org.example.textanalyzer.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.textanalyzer.model.AnalysisResult;
import org.example.textanalyzer.model.ErrorInfo;
import org.example.textanalyzer.model.WordCount;
import org.example.textanalyzer.service.TextAnalysisService;
import org.example.textanalyzer.util.ArgumentParser;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.io.File;
import java.util.Optional;

/**
 * Класс, который запускается после старта Spring Boot
 * и обрабатывает аргументы командной строки.
 */
@Component
@Profile("cli")
public class TextAnalyzerRunner implements CommandLineRunner {

    private static final Logger log = (Logger) LoggerFactory.getLogger(TextAnalyzerRunner.class);

    private final TextAnalysisService singleService;
    private final TextAnalysisService parallelService;
    private final ObjectMapper mapper;

    public TextAnalyzerRunner(
            @Qualifier("single") TextAnalysisService singleService,
            @Qualifier("parallel") TextAnalysisService parallelService,
            ObjectMapper mapper) {

        this.singleService = singleService;
        this.parallelService = parallelService;
        this.mapper = mapper;
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length == 0) {
            log.info("No CLI args — skipping TextAnalyzerRunner");
            return;
        }
        // Парсим аргументы
        var parsed = ArgumentParser.parse(args);

        if (parsed.isHelp()) {
            // Если указан --help, выводим справку и выходим
            ArgumentParser.printHelp();
            return;
        }

        // Валидация обязательных параметров
        if (!parsed.isValid()) {
            log.error("Неверные параметры запуска");
            ArgumentParser.printHelp();
            return;
        }

        log.info("Запуск анализа: dir={}, minLength={}, top={}, stopwords={}, output={}",
                parsed.getDir(), parsed.getMinLength(), parsed.getTop(),
                parsed.getStopwordsPath().orElse(null),
                parsed.getOutputPath().orElse(null));

        String mode = parsed.getMode();
        int threads = parsed.getThreads().orElse(2);

        TextAnalysisService service =
                mode.equalsIgnoreCase("multi") ? parallelService : singleService;

        // Запускаем анализ
        //Вызываем на бине которой реализует интерфейс TextAnalysisService
        AnalysisResult result = service.analyze(
                Path.of(parsed.getDir()),
                parsed.getMinLength(),
                parsed.getTop(),
                parsed.getStopwordsPath().map(Path::of),
                threads
        );

        // Если указан output — пишем JSON в файл, иначе выводим в консоль
        Optional<String> outputPathOpt = parsed.getOutputPath();
        if (outputPathOpt.isPresent()) {
            File outFile = Path.of(outputPathOpt.get()).toFile();
            mapper.writerWithDefaultPrettyPrinter().writeValue(outFile, result);
            log.info("Результат записан в файл: {}", outFile.getAbsolutePath());
        } else {
            printToConsole(result);
        }
    }

    /**
     * Вывод результата в консоль в формате:
     * 1. word — count
     */
    private void printToConsole(AnalysisResult result) {
        var info = result.getAnalysisInfo();

        System.out.printf("Mode: %s (%d workers)%n", info.getMode(), info.getThreads());
        System.out.printf("Processed %d files in %d ms%n",
                info.getProcessedFiles(), info.getExecutionTimeMs());

        int i = 1;
        for (WordCount wc : result.getWords()) {
            System.out.printf("%d. %s — %d%n", i++, wc.getWord(), wc.getCount());
        }

        if (!result.getErrors().isEmpty()) {
            System.out.println("\nErrors:");
            for (ErrorInfo e : result.getErrors()) {
                System.out.printf(" - %s: %s%n", e.getFile(), e.getMessage());
            }
        }
    }
}