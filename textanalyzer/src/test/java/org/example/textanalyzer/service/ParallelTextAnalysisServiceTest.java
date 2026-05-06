package org.example.textanalyzer.service;

import org.example.textanalyzer.model.AnalysisResult;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Юнит-тест для ParallelTextAnalysisService.
 */
class ParallelTextAnalysisServiceTest {

    @Test
    void analyze_shouldCountWordsInMultipleFiles() throws Exception {
        // Подготовка временной директории с файлами
        Path dir = Files.createTempDirectory("texts");
        Files.writeString(dir.resolve("a.txt"), "hello world hello");
        Files.writeString(dir.resolve("b.txt"), "world test world");

        StopWordsProvider stopWordsProvider = new StopWordsProvider() {
            @Override
            public Set<String> loadStopWords(Path file) {
                return Set.of(); // без стоп-слов
            }
        };

        ParallelTextAnalysisService service = new ParallelTextAnalysisService(stopWordsProvider);

        AnalysisResult result = service.analyze(
                dir,
                1,
                10,
                Optional.empty(),
                4
        );

        assertThat(result.getWords())
                .extracting("word")
                .contains("hello", "world", "test");

        assertThat(result.getAnalysisInfo().getProcessedFiles()).isEqualTo(2);
    }
}
