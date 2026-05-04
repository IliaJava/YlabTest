package org.example.textanalyzer.entity;


import jakarta.persistence.*;
import lombok.Data;

/**
 * Слово и его частота, привязанные к конкретному анализу.
 */
@Entity
@Table(name = "analysis_words")
@Data
public class WordCountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String word;
    private long count;

    @ManyToOne(fetch = FetchType.LAZY)
    private AnalysisEntity analysis;
}
