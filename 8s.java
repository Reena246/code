package com.project.badgemate.repository;

import com.project.badgemate.entity.Door;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoorRepository extends JpaRepository<Door, Long> {
    Optional<Door> findByDoorIdAndIsActiveTrue(Long doorId);
}
