package com.project.badgemate.entity;

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
    
    @Column(name = "floor_id")
    private Long floorId;
    
    @Column(name = "door_code", length = 30)
    private String doorCode;
    
    @Column(name = "door_number")
    private Short doorNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "lock_type", length = 8)
    private LockType lockType;
    
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
}
