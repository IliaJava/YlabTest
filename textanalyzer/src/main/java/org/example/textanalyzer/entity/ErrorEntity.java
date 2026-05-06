package org.example.textanalyzer.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

/**
 * Ошибка, возникшая при обработке файла в рамках анализа.
 */
@Entity
@Table(name = "analysis_errors")
@Data
@ToString(exclude = "analysis")
public class ErrorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String file;
    public String message;

    @ManyToOne(fetch = FetchType.LAZY)
    public AnalysisEntity analysis;
}
