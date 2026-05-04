package org.example.textanalyzer.repository;


import org.example.textanalyzer.entity.AuditRecord;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий для аудита.
 */
public interface AuditRepository extends JpaRepository<AuditRecord, Long> {
}

