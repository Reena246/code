package com.company.badgemate.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "door_lock")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoorLock {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lock_id")
    private Long lockId;
    
    @Column(name = "door_id", nullable = false)
    private Long doorId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "lock_type", nullable = false)
    private LockType lockType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LockStatus status = LockStatus.LOCKED;
    
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
    
    public enum LockType {
        MAGNETIC, STRIKE
    }
    
    public enum LockStatus {
        LOCKED, UNLOCKED, FORCED
    }
}
