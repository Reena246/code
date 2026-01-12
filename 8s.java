package com.company.badgemate.repository;

import com.company.badgemate.entity.AccessGroupDoor;
import com.badgemate.entity.AccessGroupDoorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessGroupDoorRepository extends JpaRepository<AccessGroupDoor, AccessGroupDoorId> {
}
