package com.renewable.ai.repository;

import com.renewable.ai.entity.FleetOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FleetOrderRepository extends JpaRepository<FleetOrder, Long>, JpaSpecificationExecutor<FleetOrder> {
    
    Page<FleetOrder> findByFleetId(Long fleetId, Pageable pageable);
    
    Page<FleetOrder> findByFleetIdAndDriverId(Long fleetId, Long driverId, Pageable pageable);
    
    Page<FleetOrder> findByFleetIdAndStatus(Long fleetId, FleetOrder.OrderStatus status, Pageable pageable);
    
    Page<FleetOrder> findByFleetIdAndIsPooled(Long fleetId, Boolean isPooled, Pageable pageable);
    
    Optional<FleetOrder> findByIdAndFleetId(Long id, Long fleetId);
    
    @Query("SELECT o FROM FleetOrder o WHERE o.fleetId = :fleetId " +
           "AND (:driverName IS NULL OR o.driverName LIKE %:driverName%) " +
           "AND (:status IS NULL OR o.status = :status) " +
           "AND (:projectName IS NULL OR o.projectName LIKE %:projectName%)")
    Page<FleetOrder> findByFleetIdWithFilters(
            @Param("fleetId") Long fleetId,
            @Param("driverName") String driverName,
            @Param("status") FleetOrder.OrderStatus status,
            @Param("projectName") String projectName,
            Pageable pageable);
    
    @Query("SELECT COUNT(o) FROM FleetOrder o WHERE o.driverId = :driverId " +
           "AND o.status NOT IN :excludeStatuses")
    long countByDriverIdAndStatusNotIn(
            @Param("driverId") Long driverId,
            @Param("excludeStatuses") List<FleetOrder.OrderStatus> excludeStatuses);
    
    @Query("SELECT COUNT(o) FROM FleetOrder o WHERE o.fleetId = :fleetId " +
           "AND o.status = :status")
    long countByFleetIdAndStatus(
            @Param("fleetId") Long fleetId,
            @Param("status") FleetOrder.OrderStatus status);
    
    @Query("SELECT COUNT(o) FROM FleetOrder o WHERE o.projectId = :projectId " +
           "AND o.status NOT IN :excludeStatuses")
    long countByProjectIdAndStatusNotIn(
            @Param("projectId") Long projectId,
            @Param("excludeStatuses") List<FleetOrder.OrderStatus> excludeStatuses);
    
    List<FleetOrder> findByIdInAndFleetId(List<Long> ids, Long fleetId);
    
    @Query("SELECT o FROM FleetOrder o WHERE o.fleetId = :fleetId " +
           "AND o.status IN :statuses " +
           "ORDER BY CASE o.priority WHEN 'URGENT' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'NORMAL' THEN 3 ELSE 4 END, " +
           "o.scheduledPickupTime ASC")
    List<FleetOrder> findPendingOrdersForAssignment(
            @Param("fleetId") Long fleetId,
            @Param("statuses") List<FleetOrder.OrderStatus> statuses,
            Pageable pageable);
}
