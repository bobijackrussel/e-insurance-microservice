package com.einsurance.policy.repository;

import com.einsurance.policy.entity.PolicyTemplate;
import com.einsurance.policy.entity.PolicyTemplate.PolicyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PolicyTemplateRepository extends JpaRepository<PolicyTemplate, UUID> {

    List<PolicyTemplate> findByIsActiveTrue();

    Page<PolicyTemplate> findByIsActiveTrue(Pageable pageable);

    List<PolicyTemplate> findByType(PolicyType type);

    List<PolicyTemplate> findByTypeAndIsActiveTrue(PolicyType type);

    Page<PolicyTemplate> findByTypeAndIsActiveTrue(PolicyType type, Pageable pageable);

    long countByIsActiveTrue();

    long countByType(PolicyType type);
}
