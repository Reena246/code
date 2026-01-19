package com.company.badgemate.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "access_group_door")
@Data
public class AccessGroupDoor {
    
    @EmbeddedId
    private AccessGroupDoorId id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", columnDefinition = "ENUM('ALLOW','DENY')")
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
    
    @Embeddable
    @Data
    public static class AccessGroupDoorId implements java.io.Serializable {
        @Column(name = "access_group_id")
        private Long accessGroupId;
        
        @Column(name = "door_id")
        private Long doorId;
    }
}
