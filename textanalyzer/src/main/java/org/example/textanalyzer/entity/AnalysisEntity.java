package org.example.textanalyzer.entity;

import org.example.textanalyzer.util.AnalysisStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность анализа в БД.
 */
@Entity
@Table(name = "analyses")
@Data
public class AnalysisEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String directory;
    private int minWordLength;
    private int topCount;
    private String mode;
    private int threads;

    @Enumerated(EnumType.STRING)
    private AnalysisStatus status;

    private long executionTimeMs;
    private int processedFiles;

    private String username;
    private Instant createdAt;

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WordCountEntity> words = new ArrayList<>();

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ErrorEntity> errors = new ArrayList<>();
}
