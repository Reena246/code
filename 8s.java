package com.project.badgemate.repository;

import com.project.badgemate.entity.AccessGroupDoor;
import com.project.badgemate.entity.AccessGroupDoorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccessGroupDoorRepository extends JpaRepository<AccessGroupDoor, AccessGroupDoorId> {
    
    @Query("SELECT agd FROM AccessGroupDoor agd WHERE agd.accessGroupId = :accessGroupId AND agd.doorId = :doorId AND agd.isActive = true")
    Optional<AccessGroupDoor> findByAccessGroupIdAndDoorId(@Param("accessGroupId") Long accessGroupId, 
                                                           @Param("doorId") Long doorId);
    
    @Query("SELECT agd FROM AccessGroupDoor agd WHERE agd.doorId = :doorId AND agd.isActive = true")
    List<AccessGroupDoor> findByDoorId(@Param("doorId") Long doorId);
}
