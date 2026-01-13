package com.demo.accesscontrolsystem.repository;

import com.demo.accesscontrolsystem.entity.Audit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<Audit, Long> {}
