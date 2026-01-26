package com.accesscontrol.repository;

import com.accesscontrol.entity.Reader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReaderRepository extends JpaRepository<Reader, Long> {
    
    Optional<Reader> findByReaderUuidAndIsActive(String readerUuid, Boolean isActive);
    
    List<Reader> findByControllerIdAndIsActive(Long controllerId, Boolean isActive);
    
    @Query("SELECT r FROM Reader r WHERE r.readerUuid = :readerUuid " +
           "AND r.controllerId = :controllerId AND r.isActive = true")
    Optional<Reader> findByReaderUuidAndControllerIdAndIsActive(
            @Param("readerUuid") String readerUuid, 
            @Param("controllerId") Long controllerId);
}
