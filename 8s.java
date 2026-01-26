package com.accesscontrol.repository;

import com.accesscontrol.entity.DoorLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoorLockRepository extends JpaRepository<DoorLock, Long> {
    Optional<DoorLock> findByDoorIdAndIsActive(Long doorId, Boolean isActive);
}
