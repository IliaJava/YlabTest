package org.example.textanalyzer.service;

import org.example.textanalyzer.entity.AuditRecord;
import org.example.textanalyzer.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Сервис аудита действий пользователей.
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditRepository repo;

    public void log(String username, String action, Long analysisId, String details) {
        AuditRecord r = new AuditRecord();
        r.setUsername(username);
        r.setAction(action);
        r.setAnalysisId(analysisId);
        r.setTimestamp(Instant.now());
        r.setDetails(details);
        repo.save(r);
    }
}
