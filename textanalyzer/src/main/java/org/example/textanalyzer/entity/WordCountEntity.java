package org.example.textanalyzer.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

/**
 * Слово и его частота, привязанные к конкретному анализу.
 */
@Entity
@Table(name = "analysis_words")
@Data
@ToString(exclude = "analysis")
public class WordCountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String word;
    public long count;

    @ManyToOne(fetch = FetchType.LAZY)
    public AnalysisEntity analysis;
}
