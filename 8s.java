package com.accesscontrol.repository;

import com.accesscontrol.entity.Controller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ControllerRepository extends JpaRepository<Controller, Long> {
    Optional<Controller> findByControllerMacAndIsActive(String controllerMac, Boolean isActive);
}
