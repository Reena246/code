package com.company.badgemate.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "card_provider")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardProvider {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "provider_id")
    private Long providerId;
    
    @Column(name = "company_id", nullable = false)
    private Long companyId;
    
    @Column(name = "provider_name", length = 30, nullable = false)
    private String providerName;
    
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
    }
    
    @PreUpdate
    protected void onUpdate() {
        updated = LocalDateTime.now();
    }
}
