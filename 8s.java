package com.project.badgemate.repository;

import com.project.badgemate.entity.Reader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReaderRepository extends JpaRepository<Reader, Long> {
    
    @Query("SELECT r FROM Reader r WHERE r.doorId = :doorId AND r.isActive = true")
    List<Reader> findByDoorId(@Param("doorId") Long doorId);
    
    Optional<Reader> findByReaderCode(String readerCode);
}
