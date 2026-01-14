package com.project.badgemate.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "site")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Site {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "site_id")
    private Long siteId;
    
    @Column(name = "company_id")
    private Long companyId;
    
    @Column(name = "site_name", length = 80)
    private String siteName;
    
    @Column(name = "address_line1", length = 120)
    private String addressLine1;
    
    @Column(name = "city", length = 60)
    private String city;
    
    @Column(name = "state", length = 40)
    private String state;
    
    @Column(name = "country", length = 40)
    private String country;
    
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
