package com.project.badgemate.repository;

import com.project.badgemate.entity.AccessCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccessCardRepository extends JpaRepository<AccessCard, Long> {
    Optional<AccessCard> findByCardUidAndIsActiveTrue(String cardUid);
    Optional<AccessCard> findByCardNumberAndIsActiveTrue(String cardNumber);
}
