package org.example.textanalyzer.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

/**
 * Запись аудита: кто, когда и что сделал.
 */
@Entity
@Table(name = "audit_log")
@Data
public class AuditRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String action;
    private Long analysisId;
    private Instant timestamp;
    private String details;
}
