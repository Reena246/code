package com.demo.accesscontrolsystem.repository;

import com.demo.accesscontrolsystem.entity.AccessCard;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AccessCardRepository extends JpaRepository<AccessCard, Long> {
    Optional<AccessCard> findByCardUidAndIsActive(String cardUid, Boolean isActive);
}
