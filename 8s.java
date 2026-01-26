package com.accesscontrol.repository;

import com.accesscontrol.entity.AccessCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccessCardRepository extends JpaRepository<AccessCard, Long> {
    
    Optional<AccessCard> findByCardHexAndIsActive(String cardHex, Boolean isActive);
    
    @Query("SELECT ac FROM AccessCard ac WHERE ac.cardHex = :cardHex " +
           "AND ac.isActive = true " +
           "AND (ac.expiresAt IS NULL OR ac.expiresAt > :currentTime)")
    Optional<AccessCard> findValidCardByCardHex(@Param("cardHex") String cardHex, 
                                                 @Param("currentTime") LocalDateTime currentTime);
    
    List<AccessCard> findByEmployeePkAndIsActive(Long employeePk, Boolean isActive);
}
