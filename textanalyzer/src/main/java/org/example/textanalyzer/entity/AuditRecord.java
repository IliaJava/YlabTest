package org.example.textanalyzer.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.Instant;

/**
 * Запись аудита: кто, когда и что сделал.
 */
@Entity
@Table(name = "audit_log")
@Data
@ToString
public class AuditRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String username;
    public String action;
    public Long analysisId;
    public Instant timestamp;
    public String details;
}
