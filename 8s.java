package com.company.badgemate.repository;

import com.company.badgemate.entity.AccessGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessGroupRepository extends JpaRepository<AccessGroup, Long> {
}
