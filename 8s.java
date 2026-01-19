package com.company.badgemate.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "access_group")
@Data
public class AccessGroup {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "access_group_id")
    private Long accessGroupId;
    
    @Column(name = "company_id")
    private Long companyId;
    
    @Column(name = "group_name", length = 50)
    private String groupName;
    
    @Column(name = "description", length = 120)
    private String description;
    
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
