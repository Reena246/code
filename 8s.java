package com.project.badgemate.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "access_group_door")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(AccessGroupDoor.AccessGroupDoorId.class)
public class AccessGroupDoor {
    
    @Id
    @Column(name = "access_group_id")
    private Long accessGroupId;
    
    @Id
    @Column(name = "door_id")
    private Long doorId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", length = 5)
    private AccessType accessType;
    
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
    
    public enum AccessType {
        ALLOW, DENY
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccessGroupDoorId implements Serializable {
        private Long accessGroupId;
        private Long doorId;
    }
}
