package com.company.badgemate.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "door_lock")
@Data
public class DoorLock {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lock_id")
    private Long lockId;
    
    @Column(name = "door_id")
    private Long doorId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "lock_type", columnDefinition = "ENUM('MAGNETIC','STRIKE')")
    private LockType lockType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('LOCKED','UNLOCKED','FORCED')")
    private Status status;
    
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
    
    public enum Status {
        LOCKED, UNLOCKED, FORCED
    }
}
