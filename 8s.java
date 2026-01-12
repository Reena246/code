package com.company.badgemate.repository;

import com.company.badgemate.entity.AccessCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccessCardRepository extends JpaRepository<AccessCard, Long> {
    Optional<AccessCard> findByCardUid(String cardUid);
    Optional<AccessCard> findByCardNumber(String cardNumber);
}
