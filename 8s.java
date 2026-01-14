package com.project.badgemate.entity;

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
    
    @Column(name = "door_id")
    private Long doorId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "lock_type", length = 8)
    private LockType lockType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 8)
    private LockStatus status;
    
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
    
    public enum LockType {
        MAGNETIC, STRIKE
    }
    
    public enum LockStatus {
        LOCKED, UNLOCKED, FORCED
    }
}
