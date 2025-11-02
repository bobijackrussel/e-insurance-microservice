package com.einsurance.policy.repository;

import com.einsurance.policy.entity.CustomerPolicy;
import com.einsurance.policy.entity.CustomerPolicy.PolicyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerPolicyRepository extends JpaRepository<CustomerPolicy, UUID> {

    Optional<CustomerPolicy> findByPolicyNumber(String policyNumber);

    List<CustomerPolicy> findByUserId(UUID userId);

    Page<CustomerPolicy> findByUserId(UUID userId, Pageable pageable);

    List<CustomerPolicy> findByUserIdAndStatus(UUID userId, PolicyStatus status);

    @Query("SELECT cp FROM CustomerPolicy cp WHERE cp.userId = :userId " +
           "AND cp.status = 'ACTIVE' AND cp.expiryDate >= CURRENT_DATE")
    List<CustomerPolicy> findActiveByUserId(@Param("userId") UUID userId);

    @Query("SELECT cp FROM CustomerPolicy cp WHERE cp.status = 'ACTIVE' " +
           "AND cp.expiryDate BETWEEN CURRENT_DATE AND :expiryDate")
    List<CustomerPolicy> findExpiringSoon(@Param("expiryDate") LocalDate expiryDate);

    @Query("SELECT cp FROM CustomerPolicy cp WHERE cp.status = 'ACTIVE' " +
           "AND cp.expiryDate < CURRENT_DATE")
    List<CustomerPolicy> findExpiredPolicies();

    long countByUserId(UUID userId);

    @Query("SELECT COUNT(cp) FROM CustomerPolicy cp WHERE cp.userId = :userId " +
           "AND cp.status = 'ACTIVE' AND cp.expiryDate >= CURRENT_DATE")
    long countActiveByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(cp) > 0 FROM CustomerPolicy cp " +
           "WHERE cp.userId = :userId " +
           "AND cp.policyTemplate.id = :templateId " +
           "AND cp.status = 'ACTIVE' " +
           "AND cp.expiryDate >= CURRENT_DATE")
    boolean hasActivePolicyForTemplate(@Param("userId") UUID userId,
                                       @Param("templateId") UUID templateId);

    Page<CustomerPolicy> findByStatus(PolicyStatus status, Pageable pageable);

    @Query("SELECT cp.status as status, COUNT(cp) as count FROM CustomerPolicy cp GROUP BY cp.status")
    List<Object[]> getPolicyStatistics();
}
