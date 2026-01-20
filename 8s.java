package com.company.badgemate.repository;

import com.company.badgemate.entity.Audit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {
    @Query("SELECT AVG(a.openSeconds) FROM Audit a WHERE a.doorId = :doorId AND a.openSeconds IS NOT NULL")
    Double findAverageOpenSecondsByDoorId(@Param("doorId") Long doorId);
    
    List<Audit> findByCardIdAndDoorIdOrderByEventTimeDesc(Long cardId, Long doorId);
}
