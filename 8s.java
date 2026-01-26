package com.accesscontrol.repository;

import com.accesscontrol.entity.AccessGroupDoor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccessGroupDoorRepository extends JpaRepository<AccessGroupDoor, AccessGroupDoor.AccessGroupDoorId> {
    
    @Query("SELECT agd FROM AccessGroupDoor agd WHERE agd.accessGroupId = :accessGroupId " +
           "AND agd.doorId = :doorId AND agd.isActive = true")
    Optional<AccessGroupDoor> findByAccessGroupIdAndDoorIdAndIsActive(
            @Param("accessGroupId") Long accessGroupId, 
            @Param("doorId") Long doorId);
    
    List<AccessGroupDoor> findByAccessGroupIdAndIsActive(Long accessGroupId, Boolean isActive);
}
