package com.company.badgemate.repository;

import com.company.badgemate.entity.DoorLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DoorLockRepository extends JpaRepository<DoorLock, Long> {
    List<DoorLock> findByDoorIdAndIsActiveTrue(Long doorId);
}
