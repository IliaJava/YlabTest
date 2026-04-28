package org.example.textanalyzer.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.textanalyzer.model.AnalysisResult;
import org.example.textanalyzer.service.TextAnalysisService;
import org.example.textanalyzer.util.ArgumentParser;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.io.File;
import java.util.Optional;

/**
 * Класс, который запускается после старта Spring Boot
 * и обрабатывает аргументы командной строки.
 */
@Component
public class TextAnalyzerRunner implements CommandLineRunner {

    private static final Logger log = (Logger) LoggerFactory.getLogger(TextAnalyzerRunner.class);

    private final TextAnalysisService textAnalysisService;
    private final ObjectMapper objectMapper;

    public TextAnalyzerRunner(TextAnalysisService textAnalysisService,
                              ObjectMapper objectMapper) {
        this.textAnalysisService = textAnalysisService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
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

        // Запускаем анализ
        //Вызываем на бине которой реализует интерфейс TextAnalysisService
        AnalysisResult result = textAnalysisService.analyze(
                Path.of(parsed.getDir()),
                parsed.getMinLength(),
                parsed.getTop(),
                parsed.getStopwordsPath().map(Path::of)
        );

        // Если указан output — пишем JSON в файл, иначе выводим в консоль
        Optional<String> outputPathOpt = parsed.getOutputPath();
        if (outputPathOpt.isPresent()) {
            File outFile = Path.of(outputPathOpt.get()).toFile();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(outFile, result);
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
        var words = result.getWords();
        for (int i = 0; i < words.size(); i++) {
            var wc = words.get(i);
            System.out.printf("%d. %s - %d%n", i + 1, wc.getWord(), wc.getCount());
        }

        if (!result.getErrors().isEmpty()) {
            System.out.println();
            System.out.println("Ошибки при обработке файлов:");
            result.getErrors().forEach(e ->
                    System.out.printf("file=%s, message=%s%n", e.getFile(), e.getMessage())
            );
        }
    }
}
