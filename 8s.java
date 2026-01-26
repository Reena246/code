package com.accesscontrol.repository;

import com.accesscontrol.entity.Audit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {
    
    List<Audit> findByControllerMacAndEventTimeBetween(
            String controllerMac, 
            LocalDateTime startTime, 
            LocalDateTime endTime);
    
    @Query("SELECT AVG(a.openSeconds) FROM Audit a WHERE a.doorId = :doorId " +
           "AND a.openSeconds IS NOT NULL AND a.eventTime > :sinceTime")
    Double findAverageOpenSecondsByDoorId(@Param("doorId") Long doorId, 
                                           @Param("sinceTime") LocalDateTime sinceTime);
    
    List<Audit> findByEmployeePkOrderByEventTimeDesc(Long employeePk);
}
