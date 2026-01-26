package com.accesscontrol.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "controller")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Controller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "controller_id")
    private Long controllerId;

    @Column(name = "controller_mac", unique = true, nullable = false)
    private String controllerMac;

    @Column(name = "site_id")
    private Long siteId;

    @Column(name = "building_id")
    private Long buildingId;

    @Column(name = "floor_id")
    private Long floorId;

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
