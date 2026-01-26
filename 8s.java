package com.accesscontrol.repository;

import com.accesscontrol.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByCompanyIdAndIsActive(Long companyId, Boolean isActive);
}
