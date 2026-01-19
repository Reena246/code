package com.company.badgemate.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee")
@Data
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_pk")
    private Long employeePk;
    
    @Column(name = "employee_code", length = 20)
    private String employeeCode;
    
    @Column(name = "company_id")
    private Long companyId;
    
    @Column(name = "full_name", length = 80)
    private String fullName;
    
    @Column(name = "email", length = 120)
    private String email;
    
    @Column(name = "job_title_id")
    private Long jobTitleId;
    
    @Column(name = "access_group_id")
    private Long accessGroupId;
    
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
