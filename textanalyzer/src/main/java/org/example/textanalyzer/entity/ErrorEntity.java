package org.example.textanalyzer.entity;


import jakarta.persistence.*;
import lombok.Data;

/**
 * Ошибка, возникшая при обработке файла в рамках анализа.
 */
@Entity
@Table(name = "analysis_errors")
@Data
public class ErrorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String file;
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    private AnalysisEntity analysis;
}
