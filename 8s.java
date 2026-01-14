package com.project.badgemate.repository;

import com.project.badgemate.entity.AccessGroupDoor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessGroupDoorRepository extends JpaRepository<AccessGroupDoor, AccessGroupDoor.AccessGroupDoorId> {
    List<AccessGroupDoor> findByAccessGroupIdAndDoorIdAndIsActiveTrue(Long accessGroupId, Long doorId);
    List<AccessGroupDoor> findByAccessGroupIdAndIsActiveTrue(Long accessGroupId);
}
