package com.company.badgemate.repository;

import com.company.badgemate.entity.AccessGroupDoor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AccessGroupDoorRepository extends JpaRepository<AccessGroupDoor, AccessGroupDoor.AccessGroupDoorId> {
    List<AccessGroupDoor> findByIdAccessGroupIdAndIdDoorIdAndIsActiveTrue(Long accessGroupId, Long doorId);
    List<AccessGroupDoor> findByIdAccessGroupIdAndIsActiveTrue(Long accessGroupId);
}
