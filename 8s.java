package com.accesscontrol.repository;

import com.accesscontrol.entity.CardProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardProviderRepository extends JpaRepository<CardProvider, Long> {
}
