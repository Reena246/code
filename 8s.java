package com.demo.accesscontrolsystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "audit")
public class Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auditId;

    private Long companyId;
    private Long employeePk;
    private Long cardId;
    private Long doorId;
    private Long readerId;

    private LocalDateTime eventTime;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;

    private Integer openSeconds;
    private Double avgOpenSeconds;

    @Enumerated(EnumType.STRING)
    private ResultType result;

    private String reason;
    private Boolean isActive;
    private LocalDateTime created;
    private LocalDateTime updated;
    private String createdBy;
    private String updatedBy;

    public enum ResultType {
        SUCCESS, DENIED
    }
}
