package com.company.badgemate.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_pk")
    private Long employeePk;
    
    @Column(name = "employee_code", length = 20, nullable = false, unique = true)
    private String employeeCode;
    
    @Column(name = "company_id", nullable = false)
    private Long companyId;
    
    @Column(name = "full_name", length = 80, nullable = false)
    private String fullName;
    
    @Column(name = "email", length = 120)
    private String email;
    
    @Column(name = "job_title_id")
    private Long jobTitleId;
    
    @Column(name = "access_group_id")
    private Long accessGroupId;
    
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
}
