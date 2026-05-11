package com.renewable.ai.repository;

import com.renewable.ai.entity.FleetProject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface FleetProjectRepository extends JpaRepository<FleetProject, Long>, JpaSpecificationExecutor<FleetProject> {
    
    Page<FleetProject> findByFleetId(Long fleetId, Pageable pageable);
    
    Page<FleetProject> findByFleetIdAndStatus(Long fleetId, FleetProject.ProjectStatus status, Pageable pageable);
    
    Optional<FleetProject> findByIdAndFleetId(Long id, Long fleetId);
    
    @Query("SELECT p FROM FleetProject p WHERE p.fleetId = :fleetId " +
           "AND (:projectName IS NULL OR p.projectName LIKE %:projectName%) " +
           "AND (:status IS NULL OR p.status = :status) " +
           "AND (:customerName IS NULL OR p.customerName LIKE %:customerName%)")
    Page<FleetProject> findByFleetIdWithFilters(
            @Param("fleetId") Long fleetId,
            @Param("projectName") String projectName,
            @Param("status") FleetProject.ProjectStatus status,
            @Param("customerName") String customerName,
            Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM FleetProject p WHERE p.fleetId = :fleetId AND p.status = :status")
    long countByFleetIdAndStatus(@Param("fleetId") Long fleetId, @Param("status") FleetProject.ProjectStatus status);
    
    @Query("SELECT COALESCE(SUM(p.totalRevenue), 0) FROM FleetProject p WHERE p.fleetId = :fleetId")
    BigDecimal sumTotalRevenueByFleetId(@Param("fleetId") Long fleetId);
    
    @Query("SELECT COALESCE(SUM(p.totalWeight), 0) FROM FleetProject p WHERE p.fleetId = :fleetId")
    BigDecimal sumTotalWeightByFleetId(@Param("fleetId") Long fleetId);
}
