package com.accesscontrol.repository;

import com.accesscontrol.entity.Door;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoorRepository extends JpaRepository<Door, Long> {
    Optional<Door> findByDoorIdAndIsActive(Long doorId, Boolean isActive);
}
