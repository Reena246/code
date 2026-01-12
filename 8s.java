package com.company.badgemate.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Audit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;
    
    @Column(name = "company_id")
    private Long companyId;
    
    @Column(name = "employee_pk")
    private Long employeePk;
    
    @Column(name = "card_id")
    private Long cardId;
    
    @Column(name = "door_id")
    private Long doorId;
    
    @Column(name = "reader_id")
    private Long readerId;
    
    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;
    
    @Column(name = "opened_at")
    private LocalDateTime openedAt;
    
    @Column(name = "closed_at")
    private LocalDateTime closedAt;
    
    @Column(name = "open_seconds")
    private Integer openSeconds;
    
    @Column(name = "avg_open_seconds", precision = 10, scale = 2)
    private java.math.BigDecimal avgOpenSeconds;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false)
    private AuditResult result;
    
    @Column(name = "reason", length = 100)
    private String reason;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created", nullable = false, updatable = false)
    private LocalDateTime created;
    
    @Column(name = "updated")
    private LocalDateTime updated;
    
    @Column(name = "created_by", length = 20)
    private String createdBy;
    
    @Column(name = "updated_by", length = 20)
    private String updatedBy;
    
    @PrePersist
    protected void onCreate() {
        created = LocalDateTime.now();
        updated = LocalDateTime.now();
        if (eventTime == null) {
            eventTime = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
    }
    
    public enum AuditResult {
        SUCCESS, DENIED
    }
}
