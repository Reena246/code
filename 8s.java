package com.project.badgemate.repository;

import com.project.badgemate.entity.Audit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {
    List<Audit> findByEventTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Audit> findByCardId(Long cardId);
    List<Audit> findByDoorId(Long doorId);
    
    @Query("SELECT AVG(a.openSeconds) FROM Audit a WHERE a.doorId = :doorId AND a.openSeconds IS NOT NULL")
    Optional<BigDecimal> findAverageOpenSecondsByDoorId(@Param("doorId") Long doorId);
}
