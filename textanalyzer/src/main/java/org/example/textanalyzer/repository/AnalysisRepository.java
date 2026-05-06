package org.example.textanalyzer.repository;


import org.example.textanalyzer.entity.AnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Репозиторий для сущности AnalysisEntity.
 */
public interface AnalysisRepository extends JpaRepository<AnalysisEntity, Long> {
    List<AnalysisEntity> findAllByUsername(String username);
}
