package com.company.badgemate.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "building")
@Data
public class Building {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "building_id")
    private Long buildingId;
    
    @Column(name = "site_id")
    private Long siteId;
    
    @Column(name = "building_name", length = 50)
    private String buildingName;
    
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
