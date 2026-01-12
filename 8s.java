package com.company.badgemate.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "door")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Door {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "door_id")
    private Long doorId;
    
    @Column(name = "floor_id", nullable = false)
    private Long floorId;
    
    @Column(name = "door_code", length = 30, nullable = false)
    private String doorCode;
    
    @Column(name = "door_number", nullable = false)
    private Short doorNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "lock_type", nullable = false)
    private LockType lockType;
    
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
}
