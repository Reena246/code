package com.company.badgemate.repository;

import com.company.badgemate.entity.Audit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {
    List<Audit> findByEventTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Audit> findByCardId(Long cardId);
    List<Audit> findByDoorId(Long doorId);
}
