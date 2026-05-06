package org.example.textanalyzer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация бинов приложения.
 */
@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Красивый вывод JSON (можно и через writerWithDefaultPrettyPrinter)
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }
}