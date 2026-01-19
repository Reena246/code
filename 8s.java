package com.company.badgemate.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "access_card")
@Data
public class AccessCard {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id")
    private Long cardId;
    
    @Column(name = "company_id")
    private Long companyId;
    
    @Column(name = "provider_id")
    private Long providerId;
    
    @Column(name = "employee_pk")
    private Long employeePk;
    
    @Column(name = "card_uid", length = 40)
    private String cardUid;
    
    @Column(name = "card_number", length = 40)
    private String cardNumber;
    
    @Column(name = "issued_at")
    private LocalDateTime issuedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    @Column(name = "created")
    private LocalDateTime created;
    
    @Column(name = "updated")
    private LocalDateTime updated;
    
    @Column(name = "created_by", length = 20)
    private String createdBy;
    
    @Column(name = "updated_by", length = 20)
    private String updatedBy;
}
