package org.example.textanalyzer.entity;

import lombok.ToString;
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
@ToString(exclude = {"words", "errors"})
public class AnalysisEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String directory;
    public int minWordLength;
    public int topCount;
    public String mode;
    public int threads;

    @Enumerated(EnumType.STRING)
    public AnalysisStatus status;

    public long executionTimeMs;
    public int processedFiles;

    public String username;
    public Instant createdAt;

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<WordCountEntity> words = new ArrayList<>();

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ErrorEntity> errors = new ArrayList<>();
}
