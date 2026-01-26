package com.accesscontrol.entity;

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

    @Column(name = "controller_mac")
    private String controllerMac;

    @Column(name = "event_time")
    private LocalDateTime eventTime;

    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "open_seconds")
    private Integer openSeconds;

    @Column(name = "avg_open_seconds")
    private Integer avgOpenSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "result")
    private AuditResult result;

    @Column(name = "reason")
    private String reason;

    @Column(name = "request_received_at")
    private LocalDateTime requestReceivedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "response_sent_at")
    private LocalDateTime responseSentAt;

    @Column(name = "controller_received_at")
    private LocalDateTime controllerReceivedAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created")
    private LocalDateTime created;

    @Column(name = "updated")
    private LocalDateTime updated;

    public enum AuditResult {
        SUCCESS, DENIED
    }

    @PrePersist
    protected void onCreate() {
        created = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
    }
}
