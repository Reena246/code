package com.demo.accesscontrolsystem.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "access_card")
public class AccessCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cardId;

    private Long companyId;
    private Long providerId;
    private Long employeePk;

    @Column(length = 40)
    private String cardUid;

    @Column(length = 40)
    private String cardNumber;

    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;

    private Boolean isActive;

    private LocalDateTime created;
    private LocalDateTime updated;

    private String createdBy;
    private String updatedBy;
}
