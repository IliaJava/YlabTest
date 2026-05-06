package org.example.textanalyzer.controller;

import org.example.textanalyzer.TextAnalyzerApplication;
import org.example.textanalyzer.dto.AnalysisRequestDto;
import org.example.textanalyzer.util.AnalysisStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционный тест: поднимаем контекст, вызываем REST-методы.
 */
@SpringBootTest(classes = TextAnalyzerApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AnalysisControllerIT {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void fullFlow_analyzeAndGetResult() throws Exception {
        // Подготовка файлов
        Path dir = Path.of("src/test/resources/testdata");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("a.txt"), "hello world hello");
        Files.writeString(dir.resolve("b.txt"), "world test world");
        // Формируем запрос
        AnalysisRequestDto dto = new AnalysisRequestDto();
        dto.setDirectory(dir.toString());
        dto.setMinWordLength(1);
        dto.setTop(10);
        dto.setMode("single");
        dto.setThreads(2);

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("user", "password");
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AnalysisRequestDto> entity = new HttpEntity<>(dto, headers);

        // POST /api/analyze
        ResponseEntity<String> startResp = restTemplate
                .postForEntity("http://localhost:" + port + "/api/analyze", entity, String.class);

        assertThat(startResp.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(startResp.getBody())
                .matches("(?s).*\"status\"\\s*:\\s*\"PENDING\".*");

        // Можно было бы распарсить id из JSON, но для простоты проверим, что /results вообще работает
        ResponseEntity<String> listResp = restTemplate
                .exchange("http://localhost:" + port + "/api/results",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        String.class);

        assertThat(listResp.getStatusCode()).isEqualTo(HttpStatus.OK);
         String body = listResp.getBody();

        assertThat(listResp.getBody()
                .matches("(?s).*\"status\"\\s*:\\s*\"(PENDING|COMPLETED)\".*")
        ).isTrue();
    }
}
