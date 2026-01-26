package com.accesscontrol.entity;

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
    @Column(name = "lock_type")
    private LockType lockType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private LockStatus status;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created")
    private LocalDateTime created;

    @Column(name = "updated")
    private LocalDateTime updated;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    public enum LockType {
        MAGNETIC, STRIKE
    }

    public enum LockStatus {
        LOCKED, UNLOCKED, FORCED
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
